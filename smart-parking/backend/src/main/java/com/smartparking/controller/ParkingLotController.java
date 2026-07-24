package com.smartparking.controller;

import com.smartparking.common.Result;
import com.smartparking.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "停车场模块", description = "停车场列表、车位查询")
@RestController
@RequestMapping("/api/parking")
public class ParkingLotController {

    @Autowired
    private ParkingLotService parkingLotService;

    @Operation(summary = "获取附近停车场列表")
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Double longitude,
                          @RequestParam(required = false) Double latitude) {
        return Result.ok(parkingLotService.listNearby(longitude, latitude));
    }

    @Operation(summary = "获取停车场详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.ok(parkingLotService.getById(id));
    }

    @Operation(summary = "获取停车场车位列表")
    @GetMapping("/{id}/slots")
    public Result<?> slots(@PathVariable Long id) {
        return Result.ok(parkingLotService.getSlots(id));
    }
}
