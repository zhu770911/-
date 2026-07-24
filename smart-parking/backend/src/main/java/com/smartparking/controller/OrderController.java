package com.smartparking.controller;

import com.smartparking.common.Result;
import com.smartparking.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单模块", description = "订单列表、详情、入场/离场确认")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "我的订单列表")
    @GetMapping("/my")
    public Result<?> myOrders(HttpServletRequest request,
                              @RequestParam(required = false) Integer status,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.listByUser(userId, status, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }
}
