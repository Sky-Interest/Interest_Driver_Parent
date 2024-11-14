package com.gec.interest.order.service;

import com.gec.interest.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.form.order.OrderInfoForm;
import com.gec.interest.model.form.order.StartDriveForm;
import com.gec.interest.model.form.order.UpdateOrderBillForm;
import com.gec.interest.model.form.order.UpdateOrderCartForm;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;

public interface OrderInfoService extends IService<OrderInfo> {
    Long saveOrderInfo(OrderInfoForm orderInfoForm);
    Integer getOrderStatus(Long orderId);
    Boolean robNewOrder(Long driverId, Long orderId);
    CurrentOrderInfoVo searchCustomerCurrentOrder(Long customerId);
    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);
    Boolean driverArriveStartLocation(Long orderId, Long driverId);
    Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm);
    Boolean startDrive(StartDriveForm startDriveForm);

    Long getOrderNumByTime(String startTime, String endTime);

    Boolean endDrive(UpdateOrderBillForm updateOrderBillForm);
}
