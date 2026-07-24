package com.smartparking.service;

import com.smartparking.dto.BookingDTO;
import com.smartparking.entity.Order;

public interface BookingService {

    /** 预约车位（含防超卖） */
    Order reserve(Long userId, BookingDTO dto);

    /** 支付订单（模拟） */
    void pay(Long userId, Long orderId);

    /** 取消预约 */
    void cancel(Long userId, Long orderId);
}
