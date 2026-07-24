package com.smartparking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartparking.dto.ParkingLotDTO;
import com.smartparking.entity.ParkingLot;
import com.smartparking.entity.ParkingSlot;

import java.util.List;

public interface ParkingLotService extends IService<ParkingLot> {

    /** 获取附近停车场列表（含距离 + 有空位优先排序） */
    List<ParkingLotDTO> listNearby(Double longitude, Double latitude);

    /** 获取停车场车位列表 */
    List<ParkingSlot> getSlots(Long parkingLotId);
}
