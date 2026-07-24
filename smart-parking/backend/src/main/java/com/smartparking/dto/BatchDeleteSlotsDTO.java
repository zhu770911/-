package com.smartparking.dto;

import java.util.List;

/**
 * 批量删除车位请求 DTO
 */
public class BatchDeleteSlotsDTO {

    private List<Long> slotIds;

    public List<Long> getSlotIds() {
        return slotIds;
    }

    public void setSlotIds(List<Long> slotIds) {
        this.slotIds = slotIds;
    }
}
