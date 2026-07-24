package com.smartparking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartparking.entity.Order;

import java.util.Map;

public interface OrderService extends IService<Order> {

    /** 用户订单列表，返回 {records, total, current, pages} */
    Map<String, Object> listByUser(Long userId, Integer status, int page, int size);

    /** 管理员确认入场 */
    void confirmEnter(Long orderId);

    /** 管理员确认离场（结算） */
    void confirmLeave(Long orderId);

    /** 所有订单（管理端），返回 {records, total, current, pages} */
    Map<String, Object> listAll(Integer status, int page, int size);
}
