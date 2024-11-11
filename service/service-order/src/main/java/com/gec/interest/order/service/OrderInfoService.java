package com.gec.interest.order.service;

import com.gec.interest.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.form.order.OrderInfoForm;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;

public interface OrderInfoService extends IService<OrderInfo> {
    Long saveOrderInfo(OrderInfoForm orderInfoForm);
    Integer getOrderStatus(Long orderId);
    Boolean robNewOrder(Long driverId, Long orderId);
    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);
}
