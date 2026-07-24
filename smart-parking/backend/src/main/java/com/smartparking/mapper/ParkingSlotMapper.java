package com.smartparking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartparking.entity.ParkingSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ParkingSlotMapper extends BaseMapper<ParkingSlot> {

    /**
     * CAS 更新车位状态（乐观锁，防超卖）
     */
    @Update("UPDATE parking_slot SET status = #{newStatus}, version = version + 1 " +
            "WHERE id = #{slotId} AND version = #{version} AND status = #{expectedStatus}")
    int updateStatusWithVersion(@Param("slotId") Long slotId,
                                @Param("newStatus") Integer newStatus,
                                @Param("version") Integer version,
                                @Param("expectedStatus") Integer expectedStatus);
}
