package com.gec.interest.customer.service;

import com.gec.interest.model.form.customer.ExpectOrderForm;
import com.gec.interest.model.form.customer.SubmitOrderForm;
import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.form.payment.CreateWxPaymentForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.customer.ExpectOrderVo;
import com.gec.interest.model.vo.driver.DriverInfoVo;
import com.gec.interest.model.vo.map.DrivingLineVo;
import com.gec.interest.model.vo.map.OrderLocationVo;
import com.gec.interest.model.vo.map.OrderServiceLastLocationVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import com.gec.interest.model.vo.payment.WxPrepayVo;

public interface OrderService {
    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);
    Long submitOrder(SubmitOrderForm submitOrderForm);
    Integer getOrderStatus(Long orderId);
    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);
    OrderInfoVo getOrderInfo(Long orderId, Long customerId);
    DriverInfoVo getDriverInfo(Long orderId, Long customerId);
    OrderLocationVo getCacheOrderLocation(Long orderId);
    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);
    OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId);

    PageVo findCustomerOrderPage(Long customerId, Long page, Long limit);

    WxPrepayVo createWxPayment(CreateWxPaymentForm createWxPaymentForm);

    Boolean queryPayStatus(String orderNo);
}
