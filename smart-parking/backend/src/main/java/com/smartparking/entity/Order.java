package com.smartparking.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long slotId;
    private Long parkingLotId;
    private LocalDateTime planEnterTime;
    private Integer planDuration;
    private LocalDateTime actualEnterTime;
    private LocalDateTime actualLeaveTime;
    private BigDecimal totalAmount;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ========== Getters & Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public Long getParkingLotId() { return parkingLotId; }
    public void setParkingLotId(Long parkingLotId) { this.parkingLotId = parkingLotId; }
    public LocalDateTime getPlanEnterTime() { return planEnterTime; }
    public void setPlanEnterTime(LocalDateTime planEnterTime) { this.planEnterTime = planEnterTime; }
    public Integer getPlanDuration() { return planDuration; }
    public void setPlanDuration(Integer planDuration) { this.planDuration = planDuration; }
    public LocalDateTime getActualEnterTime() { return actualEnterTime; }
    public void setActualEnterTime(LocalDateTime actualEnterTime) { this.actualEnterTime = actualEnterTime; }
    public LocalDateTime getActualLeaveTime() { return actualLeaveTime; }
    public void setActualLeaveTime(LocalDateTime actualLeaveTime) { this.actualLeaveTime = actualLeaveTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
