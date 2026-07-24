package com.smartparking.dto;

import com.smartparking.entity.ParkingLot;

import java.math.BigDecimal;

/**
 * 停车场 DTO（含距离字段，用于附近停车场展示）
 */
public class ParkingLotDTO {

    private Long id;
    private String name;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer totalSlots;
    private Integer freeSlots;
    private String businessHours;
    private BigDecimal ratePerHour;
    private Integer status;
    private Double distance;

    public static ParkingLotDTO fromEntity(ParkingLot lot, Double distance) {
        ParkingLotDTO dto = new ParkingLotDTO();
        dto.setId(lot.getId());
        dto.setName(lot.getName());
        dto.setAddress(lot.getAddress());
        dto.setLongitude(lot.getLongitude());
        dto.setLatitude(lot.getLatitude());
        dto.setTotalSlots(lot.getTotalSlots());
        dto.setFreeSlots(lot.getFreeSlots());
        dto.setBusinessHours(lot.getBusinessHours());
        dto.setRatePerHour(lot.getRatePerHour());
        dto.setStatus(lot.getStatus());
        dto.setDistance(distance);
        return dto;
    }

    // ========== Getters & Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getFreeSlots() { return freeSlots; }
    public void setFreeSlots(Integer freeSlots) { this.freeSlots = freeSlots; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public BigDecimal getRatePerHour() { return ratePerHour; }
    public void setRatePerHour(BigDecimal ratePerHour) { this.ratePerHour = ratePerHour; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
}
