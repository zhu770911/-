package com.smartparking.dto;

import com.smartparking.entity.UserParkingBan;

import java.time.LocalDateTime;

/**
 * 封禁记录 DTO（含关联的用户手机号、停车场名称、订单编号）
 */
public class BanRecordDTO {

    private Long id;
    private Long userId;
    private String userPhone;
    private Long parkingLotId;
    private String parkingLotName;
    private Long orderId;
    private String orderNo;
    private LocalDateTime bannedUntil;
    private LocalDateTime createdAt;

    public static BanRecordDTO fromEntity(UserParkingBan ban) {
        BanRecordDTO dto = new BanRecordDTO();
        dto.setId(ban.getId());
        dto.setUserId(ban.getUserId());
        dto.setParkingLotId(ban.getParkingLotId());
        dto.setOrderId(ban.getOrderId());
        dto.setBannedUntil(ban.getBannedUntil());
        dto.setCreatedAt(ban.getCreatedAt());
        return dto;
    }

    // ========== Getters & Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public String getParkingLotName() { return parkingLotName; }
    public void setParkingLotName(String parkingLotName) { this.parkingLotName = parkingLotName; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public LocalDateTime getBannedUntil() { return bannedUntil; }
    public void setBannedUntil(LocalDateTime bannedUntil) { this.bannedUntil = bannedUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
