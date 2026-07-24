package com.smartparking.service;

import com.smartparking.dto.AdminParkingDTO;
import com.smartparking.dto.BanRecordDTO;
import com.smartparking.entity.ParkingLot;
import com.smartparking.entity.ParkingSlot;

import java.util.List;
import java.util.Map;

public interface AdminService {

    /** 新增停车场 */
    ParkingLot createParking(AdminParkingDTO dto);

    /** 修改停车场 */
    ParkingLot updateParking(Long id, AdminParkingDTO dto);

    /** 删除停车场 */
    void deleteParking(Long id);

    /** 新增一个车位 */
    ParkingSlot addSlot(Long parkingLotId, String slotNumber, Integer slotType);

    /** 编辑车位信息（编号+类型，仅空闲车位） */
    void updateSlot(Long slotId, String slotNumber, Integer slotType);

    /** 修改车位状态 */
    void updateSlotStatus(Long slotId, Integer status);

    /** 批量删除车位（逻辑删除） */
    void batchDeleteSlots(java.util.List<Long> slotIds);

    /** 统计数据：今日订单数、营收、利用率、空闲车位等 */
    Map<String, Object> getStatistics();

    /** 查询封禁列表（分页 + 状态筛选） */
    Map<String, Object> listBans(String status, int page, int size);

    /** 单条解封 */
    void unbanUser(Long banId);

    /** 批量解封 */
    void batchUnban(List<Long> ids);
}
