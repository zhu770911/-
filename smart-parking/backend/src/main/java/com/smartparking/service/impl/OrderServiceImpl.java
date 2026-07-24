package com.smartparking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.common.BusinessException;
import com.smartparking.entity.Order;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.mapper.OrderMapper;
import com.smartparking.mapper.ParkingSlotMapper;
import com.smartparking.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ParkingSlotMapper parkingSlotMapper;

    @Override
    public Map<String, Object> listByUser(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (page - 1) * size;
        List<Order> records = this.list(wrapper.last("LIMIT " + offset + "," + size));

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", total > 0 ? (int) Math.ceil((double) total / size) : 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmEnter(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，当前需为已预约状态");
        }

        order.setStatus(2);  // 已入场
        order.setActualEnterTime(LocalDateTime.now());
        this.updateById(order);

        // 更新车位状态：已预约(1) → 已占用(2)
        ParkingSlot slot = parkingSlotMapper.selectById(order.getSlotId());
        if (slot != null) {
            slot.setStatus(2);
            parkingSlotMapper.updateById(slot);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmLeave(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确，当前需为已入场状态");
        }

        LocalDateTime leaveTime = LocalDateTime.now();
        order.setStatus(3);  // 已完成
        order.setActualLeaveTime(leaveTime);
        this.updateById(order);

        // 释放车位：已占用(2) → 空闲(0)
        ParkingSlot slot = parkingSlotMapper.selectById(order.getSlotId());
        if (slot != null) {
            slot.setStatus(0);
            parkingSlotMapper.updateById(slot);
        }
    }

    @Override
    public Map<String, Object> listAll(Integer status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (page - 1) * size;
        List<Order> records = this.list(wrapper.last("LIMIT " + offset + "," + size));

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", total > 0 ? (int) Math.ceil((double) total / size) : 0);
        return result;
    }
}
