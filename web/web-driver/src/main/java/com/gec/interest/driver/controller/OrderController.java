package com.gec.interest.driver.controller;

import com.gec.interest.common.login.InterestLogin;
import com.gec.interest.common.result.Result;
import com.gec.interest.common.util.AuthContextHolder;
import com.gec.interest.driver.service.OrderService;
import com.gec.interest.model.form.order.OrderFeeForm;
import com.gec.interest.model.form.order.StartDriveForm;
import com.gec.interest.model.form.order.UpdateOrderCartForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.map.DrivingLineVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Slf4j
@Tag(name = "订单API接口管理")
@RestController
@RequestMapping("/order")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderController {
    @Autowired
    OrderService orderService;

    @Operation(summary = "查询订单状态")
    @InterestLogin
    @GetMapping("/getOrderStatus/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderStatus(orderId));
    }
    @Operation(summary = "查询司机新订单数据")
    @InterestLogin
    @GetMapping("/findNewOrderQueueData")
    public Result<List<NewOrderDataVo>> findNewOrderQueueData() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.findNewOrderQueueData(driverId));
    }
    @Operation(summary = "查找司机端当前订单")
    @InterestLogin
    @GetMapping("/searchDriverCurrentOrder")
    public Result<CurrentOrderInfoVo> searchDriverCurrentOrder() {
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        currentOrderInfoVo.setIsHasCurrentOrder(false);
        return Result.ok(currentOrderInfoVo);
    }
    @Operation(summary = "司机抢单")
    @InterestLogin
    @GetMapping("/robNewOrder/{orderId}")
    public Result<Boolean> robNewOrder(@PathVariable Long orderId) {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.robNewOrder(driverId, orderId));
    }
//    @Operation(summary = "司机端查找当前订单")
//    @InterestLogin
//    @GetMapping("/searchDriverCurrentOrder")
//    public Result<CurrentOrderInfoVo> searchDriverCurrentOrder() {
//        Long driverId = AuthContextHolder.getUserId();
//        return Result.ok(orderService.searchDriverCurrentOrder(driverId));
//    }
    @Operation(summary = "获取订单账单详细信息")
    @InterestLogin
    @GetMapping("/getOrderInfo/{orderId}")
    public Result<OrderInfoVo> getOrderInfo(@PathVariable Long orderId) {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getOrderInfo(orderId, driverId));
    }
    @Operation(summary = "计算最佳驾驶线路")
    @InterestLogin
    @PostMapping("/calculateDrivingLine")
    public Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm) {
        return Result.ok(orderService.calculateDrivingLine(calculateDrivingLineForm));
    }
    @Operation(summary = "司机到达代驾起始地点")
    @InterestLogin
    @GetMapping("/driverArriveStartLocation/{orderId}")
    public Result<Boolean> driverArriveStartLocation(@PathVariable Long orderId) {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.driverArriveStartLocation(orderId, driverId));
    }
    @Operation(summary = "更新代驾车辆信息")
    @InterestLogin
    @PostMapping("/updateOrderCart")
    public Result<Boolean> updateOrderCart(@RequestBody UpdateOrderCartForm updateOrderCartForm) {
        Long driverId = AuthContextHolder.getUserId();
        updateOrderCartForm.setDriverId(driverId);

        return Result.ok(orderService.updateOrderCart(updateOrderCartForm));
    }
    @Operation(summary = "开始代驾服务")
    @InterestLogin
    @PostMapping("/startDrive")
    public Result<Boolean> startDrive(@RequestBody StartDriveForm startDriveForm) {
        Long driverId = AuthContextHolder.getUserId();
        startDriveForm.setDriverId(driverId);
        return Result.ok(orderService.startDrive(startDriveForm));
    }
    @Operation(summary = "结束代驾服务更新订单账单")
    @InterestLogin
    @PostMapping("/endDrive")
    public Result<Boolean> endDrive(@RequestBody OrderFeeForm orderFeeForm) {
        Long driverId = AuthContextHolder.getUserId();
        orderFeeForm.setDriverId(driverId);
        return Result.ok(orderService.endDrive(orderFeeForm));
    }
    @Operation(summary = "获取司机订单分页列表")
    @InterestLogin
    @GetMapping("findDriverOrderPage/{page}/{limit}")
    public Result<PageVo> findDriverOrderPage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,

            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        Long driverId = AuthContextHolder.getUserId();
        PageVo pageVo = orderService.findDriverOrderPage(driverId, page, limit);
        return Result.ok(pageVo);
    }



}

