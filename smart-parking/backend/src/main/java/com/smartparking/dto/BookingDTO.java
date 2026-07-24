package com.smartparking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BookingDTO {

    @NotNull(message = "车位ID不能为空")
    private Long slotId;

    @NotNull(message = "预计入场时间不能为空")
    private LocalDateTime planEnterTime;

    @NotNull(message = "预计停留时长不能为空")
    private Integer planDuration;

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public LocalDateTime getPlanEnterTime() { return planEnterTime; }
    public void setPlanEnterTime(LocalDateTime planEnterTime) { this.planEnterTime = planEnterTime; }
    public Integer getPlanDuration() { return planDuration; }
    public void setPlanDuration(Integer planDuration) { this.planDuration = planDuration; }
}
