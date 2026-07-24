package com.smartparking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartparking.dto.ParkingLotDTO;
import com.smartparking.entity.ParkingLot;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.mapper.ParkingLotMapper;
import com.smartparking.mapper.ParkingSlotMapper;
import com.smartparking.service.ParkingLotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot>
        implements ParkingLotService {

    @Autowired
    private ParkingSlotMapper parkingSlotMapper;

    /** 默认坐标：大连理工大学 */
    private static final double DEFAULT_LNG = 121.532;
    private static final double DEFAULT_LAT = 38.877;

    @Override
    public List<ParkingLotDTO> listNearby(Double longitude, Double latitude) {
        // 获取所有营业中的停车场
        LambdaQueryWrapper<ParkingLot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingLot::getStatus, 1);
        List<ParkingLot> lots = this.list(wrapper);

        // 使用默认坐标（如果没有传参）
        final double lon = (longitude != null) ? longitude : DEFAULT_LNG;
        final double lat = (latitude != null) ? latitude : DEFAULT_LAT;

        // 计算距离并转为 DTO
        List<ParkingLotDTO> dtos = lots.stream()
                .map(lot -> {
                    double distance = haversine(lat, lon,
                            lot.getLatitude().doubleValue(),
                            lot.getLongitude().doubleValue());
                    return ParkingLotDTO.fromEntity(lot,
                            Math.round(distance * 100.0) / 100.0);
                })
                .collect(Collectors.toList());

        // 排序：有空位优先（按距离升序），满位在后（按距离升序）
        dtos.sort(Comparator
                .comparingInt((ParkingLotDTO d) -> d.getFreeSlots() != null && d.getFreeSlots() > 0 ? 0 : 1)
                .thenComparingDouble(ParkingLotDTO::getDistance));

        return dtos;
    }

    /**
     * Haversine 公式计算两点间球面距离
     * @return 距离（公里）
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // 地球半径（公里）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public List<ParkingSlot> getSlots(Long parkingLotId) {
        LambdaQueryWrapper<ParkingSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSlot::getParkingLotId, parkingLotId)
                .orderByAsc(ParkingSlot::getSlotNumber);
        return parkingSlotMapper.selectList(wrapper);
    }
}
