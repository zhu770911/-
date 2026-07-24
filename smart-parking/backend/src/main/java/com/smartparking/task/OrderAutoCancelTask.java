package com.smartparking.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartparking.entity.Order;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.UserParkingBan;
import com.smartparking.mapper.OrderMapper;
import com.smartparking.mapper.ParkingSlotMapper;
import com.smartparking.mapper.UserParkingBanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时扫描超时未支付订单，自动取消并封禁用户
 */
@Component
public class OrderAutoCancelTask {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelTask.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ParkingSlotMapper parkingSlotMapper;

    @Autowired
    private UserParkingBanMapper userParkingBanMapper;

    @Scheduled(fixedDelay = 30_000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrders() {
        // 查询：待支付 & planEnterTime + 1小时 < 当前时间
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 0)
               .isNotNull(Order::getPlanEnterTime)
               .lt(Order::getPlanEnterTime, cutoffTime);
        List<Order> expiredOrders = orderMapper.selectList(wrapper);

        if (expiredOrders.isEmpty()) {
            return;
        }

        LocalDateTime bannedUntil = LocalDateTime.now().plusHours(24);

        for (Order order : expiredOrders) {
            // 取消订单
            order.setStatus(4);
            orderMapper.updateById(order);

            // 释放车位
            ParkingSlot slot = parkingSlotMapper.selectById(order.getSlotId());
            if (slot != null && slot.getStatus() == 1) {
                slot.setStatus(0);
                parkingSlotMapper.updateById(slot);
            }

            // 插入封禁记录
            UserParkingBan ban = new UserParkingBan();
            ban.setUserId(order.getUserId());
            ban.setParkingLotId(order.getParkingLotId());
            ban.setOrderId(order.getId());
            ban.setBannedUntil(bannedUntil);
            userParkingBanMapper.insert(ban);
        }

        log.info("已自动取消 {} 个超时未支付订单", expiredOrders.size());
    }
}
