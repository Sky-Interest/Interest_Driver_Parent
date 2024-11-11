package com.gec.interest.driver.controller;

import com.gec.interest.common.login.InterestLogin;
import com.gec.interest.common.result.Result;
import com.gec.interest.common.util.AuthContextHolder;
import com.gec.interest.driver.service.OrderService;
import com.gec.interest.model.vo.order.CurrentOrderInfoVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;
import com.gec.interest.model.vo.order.OrderInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//    @Operation(summary = "查找司机端当前订单")
//    @InterestLogin
//    @GetMapping("/searchDriverCurrentOrder")
//    public Result<CurrentOrderInfoVo> searchDriverCurrentOrder() {
//        CurrentOrderInfoVo currentOrderInfoVo = new CurrentOrderInfoVo();
//        currentOrderInfoVo.setIsHasCurrentOrder(false);
//        return Result.ok(currentOrderInfoVo);
//    }
    @Operation(summary = "司机抢单")
    @InterestLogin
    @GetMapping("/robNewOrder/{orderId}")
    public Result<Boolean> robNewOrder(@PathVariable Long orderId) {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.robNewOrder(driverId, orderId));
    }
    @Operation(summary = "司机端查找当前订单")
    @InterestLogin
    @GetMapping("/searchDriverCurrentOrder")
    public Result<CurrentOrderInfoVo> searchDriverCurrentOrder() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.searchDriverCurrentOrder(driverId));
    }
    @Operation(summary = "获取订单账单详细信息")
    @InterestLogin
    @GetMapping("/getOrderInfo/{orderId}")
    public Result<OrderInfoVo> getOrderInfo(@PathVariable Long orderId) {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getOrderInfo(orderId, driverId));
    }

}

