package com.smartparking.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户停车场封禁表
 */
@TableName("user_parking_ban")
public class UserParkingBan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long parkingLotId;
    private Long orderId;
    private LocalDateTime bannedUntil;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ========== Getters & Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
