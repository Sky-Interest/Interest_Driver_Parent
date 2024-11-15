package com.gec.interest.customer.controller;

import com.gec.interest.common.login.InterestLogin;
import com.gec.interest.common.result.Result;
import com.gec.interest.common.util.AuthContextHolder;
import com.gec.interest.customer.service.OrderService;
import com.gec.interest.model.form.customer.ExpectOrderForm;
import com.gec.interest.model.form.customer.SubmitOrderForm;
import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.customer.ExpectOrderVo;
import com.gec.interest.model.vo.driver.DriverInfoVo;
import com.gec.interest.model.vo.map.DrivingLineVo;
import com.gec.interest.model.vo.map.OrderLocationVo;
import com.gec.interest.model.vo.map.OrderServiceLastLocationVo;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "订单API接口管理")
@RestController
@RequestMapping("/order")
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderController {

    @Operation(summary = "查找乘客端当前订单")
    @InterestLogin
    @GetMapping("/searchCustomerCurrentOrder")
    public Result<CurrentOrderInfoVo> searchCustomerCurrentOrder() {
        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
        currentOrderInfoVo.setIsHasCurrentOrder(false);
        return Result.ok(currentOrderInfoVo);
    }
    @Autowired
    private OrderService orderService;

    @Operation(summary = "预估订单数据")
    @InterestLogin
    @PostMapping("/expectOrder")
    public Result<ExpectOrderVo> expectOrder(@RequestBody ExpectOrderForm expectOrderForm) {
        return Result.ok(orderService.expectOrder(expectOrderForm));
    }
    @Operation(summary = "乘客下单")
    @InterestLogin
    @PostMapping("/submitOrder")
    public Result<Long> submitOrder(@RequestBody SubmitOrderForm submitOrderForm) {
        submitOrderForm.setCustomerId(AuthContextHolder.getUserId());
        return Result.ok(orderService.submitOrder(submitOrderForm));
    }
    @Operation(summary = "查询订单状态")
    @InterestLogin
    @GetMapping("/getOrderStatus/{orderId}")
    public Result<Integer> getOrderStatus(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderStatus(orderId));
    }
//    @Operation(summary = "乘客端查找当前订单")
//    @InterestLogin
//    @GetMapping("/searchCustomerCurrentOrder")
//    public Result<CurrentOrderInfoVo> searchCustomerCurrentOrder() {
//        Long customerId = AuthContextHolder.getUserId();
//        return Result.ok(orderService.searchCustomerCurrentOrder(customerId));
//    }
    @Operation(summary = "获取订单信息")
    @InterestLogin
    @GetMapping("/getOrderInfo/{orderId}")
    public Result<OrderInfoVo> getOrderInfo(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getOrderInfo(orderId, customerId));
    }
    @Operation(summary = "根据订单id获取司机基本信息")
    @InterestLogin
    @GetMapping("/getDriverInfo/{orderId}")
    public Result<DriverInfoVo> getDriverInfo(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getDriverInfo(orderId, customerId));
    }
    @Operation(summary = "司机赶往代驾起始点：获取订单经纬度位置")
    @InterestLogin
    @GetMapping("/getCacheOrderLocation/{orderId}")
    public Result<OrderLocationVo> getOrderLocation(@PathVariable Long orderId) {
        return Result.ok(orderService.getCacheOrderLocation(orderId));
    }
    @Operation(summary = "计算最佳驾驶线路")
    @InterestLogin
    @PostMapping("/calculateDrivingLine")
    public Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm) {
        return Result.ok(orderService.calculateDrivingLine(calculateDrivingLineForm));
    }
    @Operation(summary = "代驾服务：获取订单服务最后一个位置信息")
    @InterestLogin
    @GetMapping("/getOrderServiceLastLocation/{orderId}")
    public Result<OrderServiceLastLocationVo> getOrderServiceLastLocation(@PathVariable Long orderId) {
        return Result.ok(orderService.getOrderServiceLastLocation(orderId));
    }
    @Operation(summary = "获取乘客订单分页列表")
    @InterestLogin
    @GetMapping("findCustomerOrderPage/{page}/{limit}")
    public Result<PageVo> findCustomerOrderPage(
            @Parameter(name = "page", description = "当前页码", required = true)
            @PathVariable Long page,

            @Parameter(name = "limit", description = "每页记录数", required = true)
            @PathVariable Long limit) {
        Long customerId = AuthContextHolder.getUserId();
        PageVo pageVo = orderService.findCustomerOrderPage(customerId, page, limit);
        return Result.ok(pageVo);
    }

}

