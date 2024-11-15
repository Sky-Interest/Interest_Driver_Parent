package com.gec.interest.driver.service;

import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.form.order.OrderFeeForm;
import com.gec.interest.model.form.order.StartDriveForm;
import com.gec.interest.model.form.order.UpdateOrderCartForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.map.DrivingLineVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;
import com.gec.interest.model.vo.order.OrderInfoVo;

import java.util.List;

public interface OrderService {
    Integer getOrderStatus(Long orderId);
    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);
    Boolean robNewOrder(Long driverId, Long orderId);
    CurrentOrderInfoVo searchDriverCurrentOrder(Long driverId);
    OrderInfoVo getOrderInfo(Long orderId, Long customerId);
    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);
    Boolean driverArriveStartLocation(Long orderId, Long driverId);
    Boolean updateOrderCart(UpdateOrderCartForm updateOrderCartForm);
    Boolean startDrive(StartDriveForm startDriveForm);


    Boolean endDrive(OrderFeeForm orderFeeForm);

    PageVo findDriverOrderPage(Long driverId, Long page, Long limit);
}
