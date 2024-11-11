package com.gec.interest.driver.service.impl;

import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.dispatch.client.NewOrderFeignClient;
import com.gec.interest.driver.service.OrderService;
import com.gec.interest.model.entity.order.OrderInfo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import com.gec.interest.order.client.OrderInfoFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderInfoFeignClient orderInfoFeignClient;

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

}
