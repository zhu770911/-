package com.smartparking.controller;

import com.smartparking.common.Result;
import com.smartparking.dto.AdminParkingDTO;
import com.smartparking.dto.BatchDeleteSlotsDTO;
import com.smartparking.dto.SlotDTO;
import com.smartparking.service.AdminService;
import com.smartparking.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理端", description = "管理员：订单管理、停车场管理、数据统计")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminService adminService;

    // ============ 订单管理 ============

    @Operation(summary = "所有订单列表")
    @GetMapping("/orders")
    public Result<?> orders(@RequestParam(required = false) Integer status,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.listAll(status, page, size));
    }

    @Operation(summary = "确认入场")
    @PutMapping("/order/{orderId}/enter")
    public Result<?> confirmEnter(@PathVariable Long orderId) {
        orderService.confirmEnter(orderId);
        return Result.ok("已确认入场");
    }

    @Operation(summary = "确认离场（结算）")
    @PutMapping("/order/{orderId}/leave")
    public Result<?> confirmLeave(@PathVariable Long orderId) {
        orderService.confirmLeave(orderId);
        return Result.ok("已确认离场，费用已结算");
    }

    // ============ 停车场管理 ============

    @Operation(summary = "新增停车场")
    @PostMapping("/parking")
    public Result<?> createParking(@RequestBody AdminParkingDTO dto) {
        return Result.ok(adminService.createParking(dto));
    }

    @Operation(summary = "修改停车场信息")
    @PutMapping("/parking/{id}")
    public Result<?> updateParking(@PathVariable Long id, @RequestBody AdminParkingDTO dto) {
        return Result.ok(adminService.updateParking(id, dto));
    }

    @Operation(summary = "删除停车场")
    @DeleteMapping("/parking/{id}")
    public Result<?> deleteParking(@PathVariable Long id) {
        adminService.deleteParking(id);
        return Result.ok("删除成功");
    }

    // ============ 车位管理 ============

    @Operation(summary = "新增一个车位")
    @PostMapping("/slot")
    public Result<?> addSlot(@RequestBody SlotDTO dto) {
        return Result.ok(adminService.addSlot(dto.getParkingLotId(),
                dto.getSlotNumber(), dto.getSlotType()));
    }

    @Operation(summary = "编辑车位信息（编号+类型，仅空闲车位）")
    @PutMapping("/slot/{id}")
    public Result<?> updateSlot(@PathVariable Long id, @RequestBody SlotDTO dto) {
        adminService.updateSlot(id, dto.getSlotNumber(), dto.getSlotType());
        return Result.ok("车位信息已更新");
    }

    @Operation(summary = "批量删除车位")
    @DeleteMapping("/slots/batch")
    public Result<?> batchDeleteSlots(@RequestBody BatchDeleteSlotsDTO dto) {
        adminService.batchDeleteSlots(dto.getSlotIds());
        return Result.ok("批量删除成功");
    }

    @Operation(summary = "修改车位状态（0空闲/1已预约/2已占用/3维护）")
    @PutMapping("/slot/{id}/status")
    public Result<?> updateSlotStatus(@PathVariable Long id,
                                       @RequestParam Integer status) {
        adminService.updateSlotStatus(id, status);
        return Result.ok("车位状态已更新");
    }

    // ============ 封禁管理 ============

    @Operation(summary = "封禁列表")
    @GetMapping("/bans")
    public Result<?> bans(@RequestParam(defaultValue = "all") String status,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.listBans(status, page, size));
    }

    @Operation(summary = "单条解封")
    @DeleteMapping("/ban/{id}")
    public Result<?> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return Result.ok("解封成功");
    }

    @Operation(summary = "批量解封")
    @DeleteMapping("/bans/batch")
    public Result<?> batchUnban(@RequestBody Map<String, List<Long>> body) {
        adminService.batchUnban(body.get("ids"));
        return Result.ok("批量解封成功");
    }

    // ============ 数据统计 ============

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/statistics")
    public Result<?> statistics() {
        return Result.ok(adminService.getStatistics());
    }
}
