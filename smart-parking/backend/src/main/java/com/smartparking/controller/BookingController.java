package com.smartparking.controller;

import com.smartparking.common.Result;
import com.smartparking.dto.BookingDTO;
import com.smartparking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预约模块", description = "车位预约、支付、取消")
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Operation(summary = "预约车位")
    @PostMapping("/reserve")
    public Result<?> reserve(HttpServletRequest request, @Valid @RequestBody BookingDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(bookingService.reserve(userId, dto));
    }

    @Operation(summary = "支付订单（模拟）")
    @PutMapping("/pay/{orderId}")
    public Result<?> pay(HttpServletRequest request, @PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        bookingService.pay(userId, orderId);
        return Result.ok("支付成功");
    }

    @Operation(summary = "取消预约")
    @PutMapping("/cancel/{orderId}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        bookingService.cancel(userId, orderId);
        return Result.ok("取消成功");
    }
}
