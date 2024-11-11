package com.gec.interest.driver.service;

import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface OrderService {
    Integer getOrderStatus(Long orderId);
    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);
    Boolean robNewOrder(Long driverId, Long orderId);
    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);


}
