package com.gec.interest.driver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gec.interest.common.constant.SystemConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.common.util.LocationUtil;
import com.gec.interest.dispatch.client.NewOrderFeignClient;
import com.gec.interest.driver.service.OrderService;
import com.gec.interest.map.client.LocationFeignClient;
import com.gec.interest.map.client.MapFeignClient;
import com.gec.interest.model.entity.order.OrderInfo;
import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.form.order.OrderFeeForm;
import com.gec.interest.model.form.order.StartDriveForm;
import com.gec.interest.model.form.order.UpdateOrderBillForm;
import com.gec.interest.model.form.order.UpdateOrderCartForm;
import com.gec.interest.model.form.rules.FeeRuleRequestForm;
import com.gec.interest.model.form.rules.ProfitsharingRuleRequestForm;
import com.gec.interest.model.form.rules.RewardRuleRequestForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.map.DrivingLineVo;
import com.gec.interest.model.vo.map.OrderLocationVo;
import com.gec.interest.model.vo.map.OrderServiceLastLocationVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import com.gec.interest.model.vo.rules.FeeRuleResponseVo;
import com.gec.interest.model.vo.rules.ProfitsharingRuleResponseVo;
import com.gec.interest.model.vo.rules.RewardRuleResponseVo;
import com.gec.interest.order.client.OrderInfoFeignClient;
import com.gec.interest.rules.client.FeeRuleFeignClient;
import com.gec.interest.rules.client.ProfitsharingRuleFeignClient;
import com.gec.interest.rules.client.RewardRuleFeignClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private FeeRuleFeignClient feeRuleFeignClient;

    @Autowired
    private RewardRuleFeignClient rewardRuleFeignClient;

    @Autowired
    private ProfitsharingRuleFeignClient profitsharingRuleFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Integer getOrderStatus(Long orderId) {
        return orderInfoFeignClient.getOrderStatus(orderId).getData();
    }
    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        return newOrderFeignClient.findNewOrderQueueData(driverId).getData();
    }
    @Override
    public Boolean robNewOrder(Long driverId, Long orderId) {
        return orderInfoFeignClient.robNewOrder(driverId, orderId).getData();
    }
    @Override
    public CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId) {
        return orderInfoFeignClient.searchDriverCurrentOrder(driverId).getData();
    }
    @Override
    public OrderInfoVo getOrderInfo(Long orderId, Long driverId) {
        //订单信息
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        if(orderInfo.getDriverId().longValue() != driverId.longValue()) {
            throw new interestException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //封装订单信息
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        orderInfoVo.setOrderId(orderId);
        BeanUtils.copyProperties(orderInfo, orderInfoVo);
        return orderInfoVo;
    }
    @Autowired
    private MapFeignClient mapFeignClient;

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        return mapFeignClient.calculateDrivingLine(calculateDrivingLineForm).getData();
    }
    @Override
    public Boolean driverArriveStartLocation(Long orderId, Long driverId) {
        //判断是否刷单行为
        //1、计算当前位置与起点位置的距离【误差在1公里】
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderId).getData();
        //获取订单位置
        OrderLocationVo orderLocationVo = locationFeignClient.getCacheOrderLocation(orderId).getData();
        //计算、并且入参起点location
        double distance = LocationUtil.getDistance(
                orderInfo.getStartPointLatitude().doubleValue(), orderInfo.getStartPointLongitude().doubleValue(),
                orderLocationVo.getLatitude().doubleValue(), orderLocationVo.getLongitude().doubleValue()
        );
        //计算-判断
        if (distance > SystemConstant.DRIVER_START_LOCATION_DISTION) {
            throw new interestException(ResultCodeEnum.DRIVER_START_LOCATION_DISTION_ERROR);
        }
        return orderInfoFeignClient.driverArriveStartLocation(orderId, driverId).getData();
    }
    @Override
    public Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm) {
        return orderInfoFeignClient.updateOrderCart(updateOrderCartForm).getData();
    }
    @Override
    public Boolean startDrive(StartDriveForm startDriveForm) {
        return orderInfoFeignClient.startDrive(startDriveForm).getData();
    }
    //结束代驾服务
    @Override
    @SneakyThrows
    public Boolean endDrive(OrderFeeForm orderFeeForm) {
        //1.获取订单信息[有返回-并行处理] CompletableFuture.supplyAsync
        CompletableFuture<OrderInfo> orderInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderFeeForm.getOrderId()).getData();
            return orderInfo;
        }, threadPoolExecutor);


        //2.防止刷单，计算司机的经纬度与代驾的终点经纬度是否在2公里范围内
        CompletableFuture<OrderServiceLastLocationVo> orderServiceLastLocationVoCompletableFuture = CompletableFuture.supplyAsync(
                (
                        () -> {
                            OrderServiceLastLocationVo orderServiceLastLocationVo = locationFeignClient.getOrderServiceLastLocation(orderFeeForm.getOrderId()).getData();
                            return orderServiceLastLocationVo;
                        }
                )
                , threadPoolExecutor
        );

        //把1和2的线程操作进行合并 //标志点方法栈 --》阻塞
        CompletableFuture.allOf(
                orderInfoCompletableFuture,
                orderServiceLastLocationVoCompletableFuture
        ).join();

        //把订单结果获取
        OrderInfo orderInfo = orderInfoCompletableFuture.get();
        //判断线程的结果
        OrderServiceLastLocationVo orderServiceLastLocationVo = orderServiceLastLocationVoCompletableFuture.get();
        //司机的位置与代驾终点位置的距离
        double distance = LocationUtil.getDistance(orderInfo.getEndPointLatitude().doubleValue(), orderInfo.getEndPointLongitude().doubleValue(), orderServiceLastLocationVo.getLatitude().doubleValue(), orderServiceLastLocationVo.getLongitude().doubleValue());
        if (distance > SystemConstant.DRIVER_START_LOCATION_DISTION) {
            throw new interestException(ResultCodeEnum.DRIVER_END_LOCATION_DISTION_ERROR);
        }

        //3、计算实际的里程
        CompletableFuture<BigDecimal> realDistanceCompletableFuture = CompletableFuture.supplyAsync(() -> {
            BigDecimal realDistance = locationFeignClient.calculateOrderRealDistance(orderFeeForm.getOrderId()).getData();
            log.info("结束代驾，订单实际里程：{}", realDistance);
            return realDistance;
        }, threadPoolExecutor);

        //4、计算实际的代驾费用【在里程线程完成后，得到结果，才能计算代驾费用】【串行编排-thenApplyAsync】
        CompletableFuture<FeeRuleResponseVo> feeRuleResponseVoCompletableFuture = realDistanceCompletableFuture.thenApplyAsync((realDistance) -> {
            FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
            feeRuleRequestForm.setDistance(realDistance);
            feeRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
            //等候时间
            Integer waitMinute = Math.abs((int) ((orderInfo.getArriveTime().getTime() - orderInfo.getAcceptTime().getTime()) / (1000 * 60)));
            feeRuleRequestForm.setWaitMinute(waitMinute);
            log.info("结束代驾，费用参数：{}", JSONObject.toJSONString(feeRuleRequestForm));
            FeeRuleResponseVo feeRuleResponseVo = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();
            log.info("费用明细：{}", JSONObject.toJSONString(feeRuleResponseVo));
            //订单总金额 需加上 路桥费、停车费、其他费用、乘客好处费
            BigDecimal totalAmount = feeRuleResponseVo.getTotalAmount().add(orderFeeForm.getTollFee()).add(orderFeeForm.getParkingFee()).add(orderFeeForm.getOtherFee()).add(orderInfo.getFavourFee());
            feeRuleResponseVo.setTotalAmount(totalAmount);
            return feeRuleResponseVo;
        });

        //5、计算系统奖励
        //5.1.获取订单数
        CompletableFuture<Long> orderNumCompletableFuture = CompletableFuture.supplyAsync(() -> {
            String startTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 00:00:00";
            String endTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 23:59:59";
            Long orderNum = orderInfoFeignClient.getOrderNumByTime(startTime, endTime).getData();
            return orderNum;
        }, threadPoolExecutor);
        //5.2.计算算奖励金额封装参数【依赖上面的订单数量】【串行编排】
        CompletableFuture<RewardRuleResponseVo> rewardRuleResponseVoCompletableFuture = orderNumCompletableFuture.thenApplyAsync((orderNum) -> {
            RewardRuleRequestForm rewardRuleRequestForm = new RewardRuleRequestForm();
            rewardRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
            rewardRuleRequestForm.setOrderNum(orderNum);
            //5.3.执行计算奖励金额
            RewardRuleResponseVo rewardRuleResponseVo = rewardRuleFeignClient.calculateOrderRewardFee(rewardRuleRequestForm).getData();
            log.info("结束代驾，系统奖励：{}", JSONObject.toJSONString(rewardRuleResponseVo));
            return rewardRuleResponseVo;
        });

        //6、分账
        CompletableFuture<ProfitsharingRuleResponseVo> profitsharingRuleResponseVoCompletableFuture
                = feeRuleResponseVoCompletableFuture.thenCombineAsync(
                orderNumCompletableFuture,
                (feeRuleResponseVo, orderNum) ->
                {
                    ProfitsharingRuleRequestForm profitsharingRuleRequestForm = new ProfitsharingRuleRequestForm();
                    profitsharingRuleRequestForm.setOrderAmount(feeRuleResponseVo.getTotalAmount());
                    profitsharingRuleRequestForm.setOrderNum(orderNum);
                    ProfitsharingRuleResponseVo profitsharingRuleResponseVo = profitsharingRuleFeignClient.calculateOrderProfitsharingFee(profitsharingRuleRequestForm).getData();
                    log.info("结束代驾，分账信息：{}", JSONObject.toJSONString(profitsharingRuleResponseVo));
                    return profitsharingRuleResponseVo;
                }
        );

        //把所有的线程进行合并
        CompletableFuture.allOf(orderInfoCompletableFuture,
                realDistanceCompletableFuture,
                feeRuleResponseVoCompletableFuture,
                orderNumCompletableFuture,
                rewardRuleResponseVoCompletableFuture,
                profitsharingRuleResponseVoCompletableFuture
        ).join();

        //最后是数据的封装
        //从上面的线程编排对象中获取执行结果
        BigDecimal realDistance = realDistanceCompletableFuture.get();
        FeeRuleResponseVo feeRuleResponseVo = feeRuleResponseVoCompletableFuture.get();
        RewardRuleResponseVo rewardRuleResponseVo = rewardRuleResponseVoCompletableFuture.get();
        ProfitsharingRuleResponseVo profitsharingRuleResponseVo = profitsharingRuleResponseVoCompletableFuture.get();
        //7.封装更新订单账单相关实体对象
        UpdateOrderBillForm updateOrderBillForm = new UpdateOrderBillForm();
        updateOrderBillForm.setOrderId(orderFeeForm.getOrderId());
        updateOrderBillForm.setDriverId(orderFeeForm.getDriverId());
        //路桥费、停车费、其他费用
        updateOrderBillForm.setTollFee(orderFeeForm.getTollFee());
        updateOrderBillForm.setParkingFee(orderFeeForm.getParkingFee());
        updateOrderBillForm.setOtherFee(orderFeeForm.getOtherFee());
        //乘客好处费
        updateOrderBillForm.setFavourFee(orderInfo.getFavourFee());

        //实际里程
        updateOrderBillForm.setRealDistance(realDistance);
        //订单奖励信息
        BeanUtils.copyProperties(rewardRuleResponseVo, updateOrderBillForm);
        //代驾费用信息
        BeanUtils.copyProperties(feeRuleResponseVo, updateOrderBillForm);
        //分账相关信息
        BeanUtils.copyProperties(profitsharingRuleResponseVo, updateOrderBillForm);
        updateOrderBillForm.setProfitsharingRuleId(profitsharingRuleResponseVo.getProfitsharingRuleId());
        log.info("结束代驾，更新账单信息：{}", JSONObject.toJSONString(updateOrderBillForm));

        //8.结束代驾更新账单
        orderInfoFeignClient.endDrive(updateOrderBillForm);
        return true;
    }
    @Override
    public PageVo findDriverOrderPage(Long driverId, Long page, Long limit) {
        return orderInfoFeignClient.findDriverOrderPage(driverId, page, limit).getData();
    }




}
