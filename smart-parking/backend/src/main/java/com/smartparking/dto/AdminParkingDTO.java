package com.smartparking.dto;

import java.math.BigDecimal;

/**
 * 管理端 — 创建/更新停车场时接收的参数
 */
public class AdminParkingDTO {

    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer totalSlots;
    private String businessHours;
    private BigDecimal ratePerHour;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public BigDecimal getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(BigDecimal ratePerHour) { this.ratePerHour = ratePerHour; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
