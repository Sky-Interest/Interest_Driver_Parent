package com.gec.interest.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.interest.common.constant.RedisConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.model.entity.order.*;
import com.gec.interest.model.enums.OrderStatus;
import com.gec.interest.model.form.order.OrderInfoForm;
import com.gec.interest.model.form.order.StartDriveForm;
import com.gec.interest.model.form.order.UpdateOrderBillForm;
import com.gec.interest.model.form.order.UpdateOrderCartForm;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.order.mapper.*;
import com.gec.interest.order.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.interest.order.service.OrderMonitorService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderStatusLogMapper orderStatusLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderMonitorMapper orderMonitorMapper;

    @Autowired
    private OrderBillMapper orderBillMapper;

    @Autowired
    private OrderProfitsharingMapper orderProfitsharingMapper;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Long saveOrderInfo(OrderInfoForm orderInfoForm) {
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoForm, orderInfo);
        String orderNo = UUID.randomUUID().toString().replaceAll("-","");
        orderInfo.setStatus(OrderStatus.WAITING_ACCEPT.getStatus());
        orderInfo.setOrderNo(orderNo);
        orderInfoMapper.insert(orderInfo);

        //记录日志
        this.log(orderInfo.getId(), orderInfo.getStatus());

        //接单标识，标识不存在了说明不在等待接单状态了
        redisTemplate.opsForValue().set(RedisConstant.ORDER_ACCEPT_MARK, "0", RedisConstant.ORDER_ACCEPT_MARK_EXPIRES_TIME, TimeUnit.MINUTES);
        return orderInfo.getId();
    }

    public void log(Long orderId, Integer status) {
        OrderStatusLog orderStatusLog = new OrderStatusLog();
        orderStatusLog.setOrderId(orderId);
        orderStatusLog.setOrderStatus(status);
        orderStatusLog.setOperateTime(new Date());
        orderStatusLogMapper.insert(orderStatusLog);
    }
    //根据订单id获取订单状态
    @Override
    public Integer getOrderStatus(Long orderId) {
        //sql语句： select status from order_info where id=?
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId,orderId);
        wrapper.select(OrderInfo::getStatus);
        //调用mapper方法
        OrderInfo orderInfo = orderInfoMapper.selectOne(wrapper);
        //订单不存在
        if(orderInfo == null) {
            return OrderStatus.NULL_ORDER.getStatus();
        }

        return orderInfo.getStatus();
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        //抢单成功或取消订单，都会删除该key，redis判断，减少数据库压力
        if(!redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK)) {
            //抢单失败
            throw new interestException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        }

        // 初始化分布式锁，创建一个RLock实例
        RLock lock = redissonClient.getLock(RedisConstant.ROB_NEW_ORDER_LOCK + orderId);
        try {
            /**
             * TryLock是一种非阻塞式的分布式锁，实现原理：Redis的SETNX命令
             * 参数：
             *     waitTime：等待获取锁的时间
             *     leaseTime：加锁的时间
             */
            boolean flag = lock.tryLock(RedisConstant.ROB_NEW_ORDER_LOCK_WAIT_TIME,RedisConstant.ROB_NEW_ORDER_LOCK_LEASE_TIME, TimeUnit.SECONDS);
            //获取到锁
            if (flag){
                //二次判断，防止重复抢单
                if(!redisTemplate.hasKey(RedisConstant.ORDER_ACCEPT_MARK)) {
                    //抢单失败
                    throw new interestException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }

                //修改订单状态
                //update order_info set status = 2, driver_id = #{driverId} where id = #{id}
                //修改字段
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setId(orderId);
                orderInfo.setStatus(OrderStatus.ACCEPTED.getStatus());
                orderInfo.setAcceptTime(new Date());
                orderInfo.setDriverId(driverId);
                int rows = orderInfoMapper.updateById(orderInfo);
                if(rows != 1) {
                    //抢单失败
                    throw new interestException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
                }

                //记录日志
                this.log(orderId, orderInfo.getStatus());

                //删除redis订单标识
                redisTemplate.delete(RedisConstant.ORDER_ACCEPT_MARK);
            }
        } catch (InterruptedException e) {
            //抢单失败
            throw new interestException(ResultCodeEnum.COB_NEW_ORDER_FAIL);
        } finally {
            if(lock.isLocked()) {
                lock.unlock();
            }
        }
        return true;
    }
    @Override
    public CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getCustomerId, customerId);
        //乘客端支付完订单，乘客端主要流程就走完（当前这些节点，乘客端会调整到相应的页面处理逻辑）
        Integer[] statusArray = {
                OrderStatus.ACCEPTED.getStatus(),
                OrderStatus.DRIVER_ARRIVED.getStatus(),
                OrderStatus.UPDATE_CART_INFO.getStatus(),
                OrderStatus.START_SERVICE.getStatus(),
                OrderStatus.END_SERVICE.getStatus(),
                OrderStatus.UNPAID.getStatus()
        };
        queryWrapper.in(OrderInfo::getStatus, statusArray);
        queryWrapper.orderByDesc(OrderInfo::getId);
        queryWrapper.last("limit 1");
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        if(null != orderInfo) {
            currentOrderInfoVo.setStatus(orderInfo.getStatus());
            currentOrderInfoVo.setOrderId(orderInfo.getId());
            currentOrderInfoVo.setIsHasCurrentOrder(true);
        } else {
            currentOrderInfoVo.setIsHasCurrentOrder(false);
        }
        return currentOrderInfoVo;
    }
    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getDriverId, driverId);
        //司机发送完账单，司机端主要流程就走完（当前这些节点，司机端会调整到相应的页面处理逻辑）
        Integer[] statusArray = {
                OrderStatus.ACCEPTED.getStatus(),
                OrderStatus.DRIVER_ARRIVED.getStatus(),
                OrderStatus.UPDATE_CART_INFO.getStatus(),
                OrderStatus.START_SERVICE.getStatus(),
                OrderStatus.END_SERVICE.getStatus()
        };
        queryWrapper.in(OrderInfo::getStatus, statusArray);
        queryWrapper.orderByDesc(OrderInfo::getId);
        queryWrapper.last("limit 1");
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapper);
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        if(null != orderInfo) {
            currentOrderInfoVo.setStatus(orderInfo.getStatus());
            currentOrderInfoVo.setOrderId(orderInfo.getId());
            currentOrderInfoVo.setIsHasCurrentOrder(true);
        } else {
            currentOrderInfoVo.setIsHasCurrentOrder(false);
        }
        return currentOrderInfoVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, orderId);
        queryWrapper.eq(OrderInfo::getDriverId, driverId);
    //更新的对象数据信息
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setStatus(OrderStatus.DRIVER_ARRIVED.getStatus());
        updateOrderInfo.setArriveTime(new Date());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(orderId, OrderStatus.DRIVER_ARRIVED.getStatus());
        } else {
            throw new interestException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, updateOrderCartForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, updateOrderCartForm.getDriverId());

        OrderInfo updateOrderInfo = new OrderInfo();
        BeanUtils.copyProperties(updateOrderCartForm, updateOrderInfo);
        updateOrderInfo.setStatus(OrderStatus.UPDATE_CART_INFO.getStatus());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(updateOrderCartForm.getOrderId(), OrderStatus.UPDATE_CART_INFO.getStatus());
        } else {
            throw new interestException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, startDriveForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, startDriveForm.getDriverId());

        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setStatus(OrderStatus.START_SERVICE.getStatus());
        updateOrderInfo.setStartServiceTime(new Date());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(startDriveForm.getOrderId(), OrderStatus.START_SERVICE.getStatus());
        } else {
            throw new interestException(ResultCodeEnum.UPDATE_ERROR);
        }

        //初始化订单监控统计数据
        OrderMonitor orderMonitor = new OrderMonitor();
        orderMonitor.setOrderId(startDriveForm.getOrderId());
//        orderMonitorService.saveOrderMonitor(orderMonitor);
        orderMonitorMapper.insert(orderMonitor);
        return true;
    }
    @Override
    public Long getOrderNumByTime(String startTime, String endTime) {
        //查询条件的构建
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(OrderInfo::getStartServiceTime, startTime);
        queryWrapper.lt(OrderInfo::getEndServiceTime, endTime);
        //查询
        Long count = orderInfoMapper.selectCount(queryWrapper);
        return count;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean endDrive(UpdateOrderBillForm updateOrderBillForm) {
        //更新订单信息
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getId, updateOrderBillForm.getOrderId());
        queryWrapper.eq(OrderInfo::getDriverId, updateOrderBillForm.getDriverId());
        //更新字段
        OrderInfo updateOrderInfo = new OrderInfo();
        updateOrderInfo.setStatus(OrderStatus.END_SERVICE.getStatus());
        updateOrderInfo.setRealAmount(updateOrderBillForm.getTotalAmount());
        updateOrderInfo.setFavourFee(updateOrderBillForm.getFavourFee());
        updateOrderInfo.setEndServiceTime(new Date());
        updateOrderInfo.setRealDistance(updateOrderBillForm.getRealDistance());
        //只能更新自己的订单
        int row = orderInfoMapper.update(updateOrderInfo, queryWrapper);
        if(row == 1) {
            //记录日志
            this.log(updateOrderBillForm.getOrderId(), OrderStatus.END_SERVICE.getStatus());

            //插入实际账单数据
            OrderBill orderBill = new OrderBill();
            BeanUtils.copyProperties(updateOrderBillForm, orderBill);
            orderBill.setOrderId(updateOrderBillForm.getOrderId());
            orderBill.setPayAmount(orderBill.getTotalAmount());
            orderBillMapper.insert(orderBill);

            //插入分账信息数据
            OrderProfitsharing orderProfitsharing = new OrderProfitsharing();
            BeanUtils.copyProperties(updateOrderBillForm, orderProfitsharing);
            orderProfitsharing.setOrderId(updateOrderBillForm.getOrderId());
            orderProfitsharing.setRuleId(updateOrderBillForm.getProfitsharingRuleId());
            orderProfitsharing.setStatus(1);
            orderProfitsharingMapper.insert(orderProfitsharing);
        } else {
            throw new interestException(ResultCodeEnum.UPDATE_ERROR);
        }
        return true;
    }




}
