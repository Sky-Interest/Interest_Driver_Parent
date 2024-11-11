package com.gec.interest.customer.service;

import com.gec.interest.model.form.customer.ExpectOrderForm;
import com.gec.interest.model.form.customer.SubmitOrderForm;
import com.gec.interest.model.vo.customer.ExpectOrderVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.OrderInfoVo;

public interface OrderService {
    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);
    Long submitOrder(SubmitOrderForm submitOrderForm);
    Integer getOrderStatus(Long orderId);
    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);
    OrderInfoVo getOrderInfo(Long orderId, Long customerId);
}
