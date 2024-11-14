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
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
    public Boolean endDrive(OrderFeeForm orderFeeForm) {
        //1.获取订单信息
        OrderInfo orderInfo = orderInfoFeignClient.getOrderInfo(orderFeeForm.getOrderId()).getData();
        if (orderInfo.getDriverId().longValue() != orderFeeForm.getDriverId().longValue()) {
            throw new interestException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
        }

        //2.防止刷单，计算司机的经纬度与代驾的终点经纬度是否在2公里范围内
        OrderServiceLastLocationVo orderServiceLastLocationVo = locationFeignClient.getOrderServiceLastLocation(
                orderFeeForm.getOrderId()
        ).getData();
        //司机的位置与代驾终点位置的距离
        double distance = LocationUtil.getDistance(
                orderInfo.getEndPointLatitude().doubleValue(), orderInfo.getEndPointLongitude().doubleValue(),
                orderServiceLastLocationVo.getLatitude().doubleValue(), orderServiceLastLocationVo.getLongitude().doubleValue()
        );
        System.out.println("endDrive---->"+distance);
        if (distance > SystemConstant.DRIVER_END_LOCATION_DISTION) {//2公里
            throw new interestException(ResultCodeEnum.DRIVER_END_LOCATION_DISTION_ERROR);
        }

        //2、计算实际的里程
        BigDecimal realDistance = locationFeignClient.calculateOrderRealDistance(orderInfo.getId()).getData();
        log.info("结束代驾，订单实际里程：{}", realDistance);

        //3、计算实际的代驾费用
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDistance(realDistance);//距离
        feeRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());//开始时间
        //随机生成等待时长
        Integer waitMinute = Math.abs((int) ((orderInfo.getArriveTime().getTime() - orderInfo.getAcceptTime().getTime()) / (1000 * 60)));
        feeRuleRequestForm.setWaitMinute(waitMinute);
        log.info("结束代驾，费用参数：{}", JSONObject.toJSONString(feeRuleRequestForm));
        FeeRuleResponseVo feeRuleResponseVo = feeRuleFeignClient.calculateOrderFee(feeRuleRequestForm).getData();
        log.info("费用明细：{}", JSONObject.toJSONString(feeRuleResponseVo));
        //订单总的费用中：路桥费、停车费、其他费用、乘客好处费
        BigDecimal totalAmount = feeRuleResponseVo.getTotalAmount()
                .add(orderFeeForm.getTollFee())
                .add(orderFeeForm.getParkingFee())
                .add(orderFeeForm.getOtherFee());
        //4、计算系统奖励
        String startTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 00:00:00";
        String endTime = new DateTime(orderInfo.getStartServiceTime()).toString("yyyy-MM-dd") + " 23:59:59"; //
        Long orderNum = orderInfoFeignClient.getOrderNumByTime(startTime, endTime).getData();
        //提供请求参数
        RewardRuleRequestForm rewardRuleRequestForm = new RewardRuleRequestForm();
        rewardRuleRequestForm.setStartTime(orderInfo.getStartServiceTime());
        rewardRuleRequestForm.setOrderNum(orderNum);
        //调用服务
        RewardRuleResponseVo rewardRuleResponseVo = rewardRuleFeignClient.calculateOrderRewardFee(rewardRuleRequestForm).getData();
        log.info("结束代驾，系统奖励：{}", JSONObject.toJSONString(rewardRuleResponseVo));
        //5、分账
        ProfitsharingRuleRequestForm profitsharingRuleRequestForm = new ProfitsharingRuleRequestForm();
        profitsharingRuleRequestForm.setOrderAmount(feeRuleResponseVo.getTotalAmount());
        profitsharingRuleRequestForm.setOrderNum(orderNum);
        ProfitsharingRuleResponseVo profitsharingRuleResponseVo = profitsharingRuleFeignClient.calculateOrderProfitsharingFee(profitsharingRuleRequestForm).getData();
        log.info("结束代驾，分账信息：{}", JSONObject.toJSONString(profitsharingRuleResponseVo));
        //6、订单账单信息ORDER_BILL
        UpdateOrderBillForm updateOrderBillForm = new UpdateOrderBillForm();
        updateOrderBillForm.setOrderId(orderFeeForm.getOrderId());
        updateOrderBillForm.setDriverId(orderFeeForm.getDriverId());
        updateOrderBillForm.setProfitsharingRuleId(1l);
        updateOrderBillForm.setRewardRuleId(1l);
        updateOrderBillForm.setFeeRuleId(1l);
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
        //分账信息
        BeanUtils.copyProperties(profitsharingRuleResponseVo, updateOrderBillForm);
        //统计\更新
        orderInfoFeignClient.endDrive(updateOrderBillForm);
        System.out.println("--------------- orderInfoFeignClient.endDrive(profitsharingRuleResponseVo);---------->" + JSONObject.toJSONString(profitsharingRuleResponseVo));
        System.out.println("--------------- orderInfoFeignClient.endDrive(updateOrderBillForm);---------->" + JSONObject.toJSONString(updateOrderBillForm));
        return true;
    }



}
