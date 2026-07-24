package com.smartparking.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartparking.common.BusinessException;
import com.smartparking.dto.BookingDTO;
import com.smartparking.entity.Order;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.UserParkingBan;
import com.smartparking.mapper.OrderMapper;
import com.smartparking.mapper.ParkingSlotMapper;
import com.smartparking.mapper.UserParkingBanMapper;
import com.smartparking.service.BookingService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private ParkingSlotMapper parkingSlotMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserParkingBanMapper userParkingBanMapper;

    private static final BigDecimal RATE_PER_HOUR = new BigDecimal("5"); // 简化：统一5元/小时

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order reserve(Long userId, BookingDTO dto) {
        // 1. 校验车位存在且空闲
        ParkingSlot slot = parkingSlotMapper.selectById(dto.getSlotId());
        if (slot == null) {
            throw new BusinessException("车位不存在");
        }
        if (slot.getStatus() != 0) {
            throw new BusinessException("该车位当前不可预约");
        }

        // 检查用户是否被该停车场封禁
        LambdaQueryWrapper<UserParkingBan> banWrapper = new LambdaQueryWrapper<>();
        banWrapper.eq(UserParkingBan::getUserId, userId)
                  .eq(UserParkingBan::getParkingLotId, slot.getParkingLotId())
                  .gt(UserParkingBan::getBannedUntil, LocalDateTime.now());
        if (userParkingBanMapper.selectCount(banWrapper) > 0) {
            throw new BusinessException("您因超时未支付订单，已被该停车场封禁24小时，暂时无法预约");
        }

        // 2. Redis 分布式锁（防并发超卖）
        String lockKey = "lock:slot:" + dto.getSlotId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试加锁，等待5秒，锁持有30秒自动释放
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            // 3. 乐观锁 CAS 更新车位状态：0(空闲) → 1(已预约)
            int updated = parkingSlotMapper.updateStatusWithVersion(
                    dto.getSlotId(), 1, slot.getVersion(), 0);
            if (updated == 0) {
                throw new BusinessException("该车位已被他人预约");
            }

            // 4. 生成订单
            Order order = new Order();
            order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
            order.setUserId(userId);
            order.setSlotId(dto.getSlotId());
            order.setParkingLotId(slot.getParkingLotId());
            order.setPlanEnterTime(dto.getPlanEnterTime());
            order.setPlanDuration(dto.getPlanDuration());
            // 预估费用 = 小时数 × 每小时费率
            BigDecimal hours = new BigDecimal(dto.getPlanDuration())
                    .divide(new BigDecimal("60"), 2, java.math.RoundingMode.UP);
            order.setTotalAmount(RATE_PER_HOUR.multiply(hours));
            order.setStatus(0); // 待支付
            orderMapper.insert(order);

            return order;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确");
        }

        // 模拟支付成功 → 已预约
        order.setStatus(1);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        // 只有待支付和已预约状态可取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException("当前订单状态不可取消");
        }

        order.setStatus(4); // 已取消
        orderMapper.updateById(order);

        // 释放车位
        ParkingSlot slot = parkingSlotMapper.selectById(order.getSlotId());
        if (slot != null && slot.getStatus() == 1) {
            slot.setStatus(0); // 恢复为空闲
            parkingSlotMapper.updateById(slot);
        }
    }
}
