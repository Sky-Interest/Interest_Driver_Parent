package com.gec.interest.order.service;

import com.gec.interest.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.form.order.OrderInfoForm;

public interface OrderInfoService extends IService<OrderInfo> {
    Long saveOrderInfo(OrderInfoForm orderInfoForm);
    Integer getOrderStatus(Long orderId);
}
