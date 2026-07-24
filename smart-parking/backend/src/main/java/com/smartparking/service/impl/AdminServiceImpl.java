package com.smartparking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartparking.common.BusinessException;
import com.smartparking.dto.AdminParkingDTO;
import com.smartparking.dto.BanRecordDTO;
import com.smartparking.entity.Order;
import com.smartparking.entity.ParkingLot;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.entity.User;
import com.smartparking.entity.UserParkingBan;
import com.smartparking.mapper.OrderMapper;
import com.smartparking.mapper.ParkingLotMapper;
import com.smartparking.mapper.ParkingSlotMapper;
import com.smartparking.mapper.UserMapper;
import com.smartparking.mapper.UserParkingBanMapper;
import com.smartparking.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private ParkingSlotMapper parkingSlotMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserParkingBanMapper userParkingBanMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingLot createParking(AdminParkingDTO dto) {
        ParkingLot lot = new ParkingLot();
        lot.setName(dto.getName());
        lot.setAddress(dto.getAddress());
        lot.setLongitude(dto.getLongitude());
        lot.setLatitude(dto.getLatitude());
        lot.setTotalSlots(dto.getTotalSlots());
        lot.setFreeSlots(dto.getTotalSlots()); // 新建时全部空闲
        lot.setBusinessHours(dto.getBusinessHours());
        lot.setRatePerHour(dto.getRatePerHour() != null ? dto.getRatePerHour() : new BigDecimal("5"));
        lot.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        parkingLotMapper.insert(lot);

        // 自动创建车位
        if (dto.getTotalSlots() != null && dto.getTotalSlots() > 0) {
            for (int i = 1; i <= dto.getTotalSlots(); i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingLotId(lot.getId());
                slot.setSlotNumber(String.format("%c%02d",
                        getSlotPrefix(lot), i));
                slot.setSlotType(1);  // 默认普通车位
                slot.setStatus(0);    // 空闲
                parkingSlotMapper.insert(slot);
            }
        }

        return lot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingLot updateParking(Long id, AdminParkingDTO dto) {
        ParkingLot lot = parkingLotMapper.selectById(id);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }

        int oldTotal = lot.getTotalSlots() != null ? lot.getTotalSlots() : 0;

        if (dto.getName() != null) lot.setName(dto.getName());
        if (dto.getAddress() != null) lot.setAddress(dto.getAddress());
        if (dto.getLongitude() != null) lot.setLongitude(dto.getLongitude());
        if (dto.getLatitude() != null) lot.setLatitude(dto.getLatitude());
        if (dto.getTotalSlots() != null) lot.setTotalSlots(dto.getTotalSlots());
        if (dto.getBusinessHours() != null) lot.setBusinessHours(dto.getBusinessHours());
        if (dto.getRatePerHour() != null) lot.setRatePerHour(dto.getRatePerHour());
        if (dto.getStatus() != null) lot.setStatus(dto.getStatus());

        parkingLotMapper.updateById(lot);

        // 如果车位数增加了，自动补建新车位
        int newTotal = dto.getTotalSlots() != null ? dto.getTotalSlots() : oldTotal;
        if (newTotal > oldTotal) {
            int nextNum = getMaxSlotNumber(lot.getId()) + 1;
            char prefix = getSlotPrefix(lot);
            for (int i = 0; i < newTotal - oldTotal; i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingLotId(lot.getId());
                slot.setSlotNumber(String.format("%c%02d", prefix, nextNum + i));
                slot.setSlotType(1);
                slot.setStatus(0);
                parkingSlotMapper.insert(slot);
            }
        }

        recalcFreeSlots(lot.getId());
        return lot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSlot addSlot(Long parkingLotId, String slotNumber, Integer slotType) {
        ParkingLot lot = parkingLotMapper.selectById(parkingLotId);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }

        // 校验编号不为空
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            throw new BusinessException("车位编号不能为空");
        }
        slotNumber = slotNumber.trim();

        // 校验编号在停车场内不重复
        LambdaQueryWrapper<ParkingSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSlot::getParkingLotId, parkingLotId)
               .eq(ParkingSlot::getSlotNumber, slotNumber);
        if (parkingSlotMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("车位编号 " + slotNumber + " 已存在");
        }

        // 校验类型
        if (slotType == null || slotType < 1 || slotType > 3) {
            throw new BusinessException("车位类型无效（1普通/2新能源/3大型）");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setParkingLotId(parkingLotId);
        slot.setSlotNumber(slotNumber);
        slot.setSlotType(slotType);
        slot.setStatus(0);
        parkingSlotMapper.insert(slot);

        lot.setTotalSlots(lot.getTotalSlots() != null ? lot.getTotalSlots() + 1 : 1);
        lot.setFreeSlots(lot.getFreeSlots() != null ? lot.getFreeSlots() + 1 : 1);
        parkingLotMapper.updateById(lot);

        return slot;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlot(Long slotId, String slotNumber, Integer slotType) {
        ParkingSlot slot = parkingSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new BusinessException("车位不存在");
        }
        if (slot.getStatus() != 0) {
            throw new BusinessException("只能编辑空闲车位的编号和类型");
        }

        // 校验编号
        if (slotNumber == null || slotNumber.trim().isEmpty()) {
            throw new BusinessException("车位编号不能为空");
        }
        slotNumber = slotNumber.trim();

        // 如果编号变了，校验新编号不重复
        if (!slotNumber.equals(slot.getSlotNumber())) {
            LambdaQueryWrapper<ParkingSlot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ParkingSlot::getParkingLotId, slot.getParkingLotId())
                   .eq(ParkingSlot::getSlotNumber, slotNumber);
            if (parkingSlotMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("车位编号 " + slotNumber + " 已存在");
            }
        }

        // 校验类型
        if (slotType == null || slotType < 1 || slotType > 3) {
            throw new BusinessException("车位类型无效（1普通/2新能源/3大型）");
        }

        slot.setSlotNumber(slotNumber);
        slot.setSlotType(slotType);
        parkingSlotMapper.updateById(slot);
    }

    /** 从停车场名称中取一个非数字字符作为车位前缀 */
    private char getSlotPrefix(ParkingLot lot) {
        String name = lot.getName();
        if (name != null) {
            for (char c : name.toCharArray()) {
                if (!Character.isDigit(c)) return c;
            }
        }
        return 'P';
    }

    /** 获取停车场现有最大车位编号的数字部分 */
    private int getMaxSlotNumber(Long parkingLotId) {
        LambdaQueryWrapper<ParkingSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSlot::getParkingLotId, parkingLotId);
        List<ParkingSlot> slots = parkingSlotMapper.selectList(wrapper);
        int max = 0;
        for (ParkingSlot s : slots) {
            String num = s.getSlotNumber();
            try {
                // 去掉非数字前缀，取数字部分
                String digits = num.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    int n = Integer.parseInt(digits);
                    if (n > max) max = n;
                }
            } catch (NumberFormatException ignored) {}
        }
        return max;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteParking(Long id) {
        ParkingLot lot = parkingLotMapper.selectById(id);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        // 删除所有车位
        LambdaQueryWrapper<ParkingSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(ParkingSlot::getParkingLotId, id);
        parkingSlotMapper.delete(slotWrapper);
        // 删除停车场
        parkingLotMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteSlots(List<Long> slotIds) {
        if (slotIds == null || slotIds.isEmpty()) {
            throw new BusinessException("请选择要删除的车位");
        }

        // 查出所有待删除车位
        List<ParkingSlot> slots = parkingSlotMapper.selectBatchIds(slotIds);
        if (slots.size() != slotIds.size()) {
            throw new BusinessException("部分车位不存在");
        }

        // 校验：只允许删除空闲(0)或维护中(3)的车位
        List<String> unremovable = new java.util.ArrayList<>();
        for (ParkingSlot s : slots) {
            if (s.getStatus() == 1 || s.getStatus() == 2) {
                unremovable.add(s.getSlotNumber());
            }
        }
        if (!unremovable.isEmpty()) {
            throw new BusinessException("车位 " + String.join("、", unremovable)
                    + " 当前不可删除（已预约或已占用）");
        }

        // 逻辑删除
        parkingSlotMapper.deleteBatchIds(slotIds);

        // 更新对应停车场的空闲车位数
        java.util.Set<Long> lotIds = new java.util.HashSet<>();
        for (ParkingSlot s : slots) {
            lotIds.add(s.getParkingLotId());
        }
        for (Long lotId : lotIds) {
            recalcFreeSlots(lotId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSlotStatus(Long slotId, Integer status) {
        ParkingSlot slot = parkingSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new BusinessException("车位不存在");
        }
        slot.setStatus(status);
        parkingSlotMapper.updateById(slot);

        // 同步更新停车场的空闲车位数
        recalcFreeSlots(slot.getParkingLotId());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 今日凌晨
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 今日订单统计
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreatedAt, todayStart);
        List<Order> todayOrders = orderMapper.selectList(todayWrapper);
        long todayOrderCount = todayOrders.size();
        long todayPaid = todayOrders.stream().filter(o -> o.getStatus() >= 1 && o.getStatus() <= 3).count();
        long todayCancelled = todayOrders.stream().filter(o -> o.getStatus() == 4).count();
        long todayPending = todayOrders.stream().filter(o -> o.getStatus() == 0).count();

        stats.put("todayOrders", todayOrderCount);
        stats.put("todayPaid", todayPaid);
        stats.put("todayCancelled", todayCancelled);
        stats.put("todayPending", todayPending);

        // 今日营收（已支付/已入场/已完成的订单金额）
        LambdaQueryWrapper<Order> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.ge(Order::getCreatedAt, todayStart)
                   .in(Order::getStatus, 1, 2, 3);
        List<Order> paidOrders = orderMapper.selectList(paidWrapper);
        BigDecimal todayRevenue = paidOrders.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todayRevenue", todayRevenue);

        // 车位统计（直接从 slot 表统计，确保实时准确）
        List<ParkingSlot> allSlots = parkingSlotMapper.selectList(null);
        int totalSlots = allSlots.size();
        int freeSlots = (int) allSlots.stream().filter(s -> s.getStatus() == 0).count();
        int occupiedSlots = (int) allSlots.stream().filter(s -> s.getStatus() == 1 || s.getStatus() == 2).count();
        int maintenanceSlots = (int) allSlots.stream().filter(s -> s.getStatus() == 3).count();

        stats.put("totalSlots", totalSlots);
        stats.put("freeSlots", freeSlots);
        stats.put("occupiedSlots", occupiedSlots);
        stats.put("maintenanceSlots", maintenanceSlots);
        stats.put("utilizationRate", totalSlots > 0 ?
                Math.round((occupiedSlots + maintenanceSlots) * 10000.0 / totalSlots) / 100.0 : 0);

        // 同步更新 parking_lot 表的 freeSlots
        for (ParkingLot lot : parkingLotMapper.selectList(null)) {
            int lotFree = (int) allSlots.stream()
                    .filter(s -> s.getParkingLotId().equals(lot.getId()) && s.getStatus() == 0)
                    .count();
            if (lot.getFreeSlots() == null || lot.getFreeSlots() != lotFree) {
                lot.setFreeSlots(lotFree);
                lot.setTotalSlots((int) allSlots.stream()
                        .filter(s -> s.getParkingLotId().equals(lot.getId())).count());
                parkingLotMapper.updateById(lot);
            }
        }

        // 本周每日订单量（用于图表）
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        long[] dailyOrders = new long[7];
        String[] days = new String[7];
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            LambdaQueryWrapper<Order> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.ge(Order::getCreatedAt, LocalDateTime.of(date, LocalTime.MIN))
                      .lt(Order::getCreatedAt, LocalDateTime.of(date.plusDays(1), LocalTime.MIN));
            dailyOrders[i] = orderMapper.selectCount(dayWrapper);
            days[i] = date.toString().substring(5); // "MM-DD"
        }
        stats.put("dailyOrders", dailyOrders);
        stats.put("dailyOrderDays", days);

        return stats;
    }

    /** 重新计算停车场空闲车位数 */
    private void recalcFreeSlots(Long parkingLotId) {
        LambdaQueryWrapper<ParkingSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSlot::getParkingLotId, parkingLotId);
        List<ParkingSlot> slots = parkingSlotMapper.selectList(wrapper);

        int free = (int) slots.stream().filter(s -> s.getStatus() == 0).count();
        int total = slots.size();

        ParkingLot lot = parkingLotMapper.selectById(parkingLotId);
        if (lot != null) {
            lot.setFreeSlots(free);
            lot.setTotalSlots(total);
            parkingLotMapper.updateById(lot);
        }
    }

    // ============ 封禁管理 ============

    @Override
    public Map<String, Object> listBans(String status, int page, int size) {
        // 三个月前
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        LambdaQueryWrapper<UserParkingBan> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(UserParkingBan::getCreatedAt, threeMonthsAgo)
               .orderByDesc(UserParkingBan::getCreatedAt);

        // 状态筛选
        if ("active".equals(status)) {
            wrapper.gt(UserParkingBan::getBannedUntil, LocalDateTime.now());
        } else if ("expired".equals(status)) {
            wrapper.le(UserParkingBan::getBannedUntil, LocalDateTime.now());
        }
        // "all" 不过滤

        // 分页
        long total = userParkingBanMapper.selectCount(wrapper);
        int offset = (page - 1) * size;
        List<UserParkingBan> bans = userParkingBanMapper.selectList(
                wrapper.last("LIMIT " + offset + "," + size));

        // 批量查关联数据
        Set<Long> userIds = bans.stream().map(UserParkingBan::getUserId).collect(Collectors.toSet());
        Set<Long> lotIds = bans.stream().map(UserParkingBan::getParkingLotId).collect(Collectors.toSet());
        Set<Long> orderIds = bans.stream().map(UserParkingBan::getOrderId)
                .filter(id -> id != null).collect(Collectors.toSet());

        Map<Long, String> userPhoneMap = new HashMap<>();
        Map<Long, String> lotNameMap = new HashMap<>();
        Map<Long, String> orderNoMap = new HashMap<>();

        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userPhoneMap.put(u.getId(), u.getPhone());
            }
        }
        if (!lotIds.isEmpty()) {
            List<ParkingLot> lots = parkingLotMapper.selectBatchIds(lotIds);
            for (ParkingLot l : lots) {
                lotNameMap.put(l.getId(), l.getName());
            }
        }
        if (!orderIds.isEmpty()) {
            List<Order> orders = orderMapper.selectBatchIds(orderIds);
            for (Order o : orders) {
                orderNoMap.put(o.getId(), o.getOrderNo());
            }
        }

        // 组装 DTO
        List<BanRecordDTO> records = new ArrayList<>();
        for (UserParkingBan ban : bans) {
            BanRecordDTO dto = BanRecordDTO.fromEntity(ban);
            dto.setUserPhone(userPhoneMap.getOrDefault(ban.getUserId(), "未知"));
            dto.setParkingLotName(lotNameMap.getOrDefault(ban.getParkingLotId(), "未知"));
            if (ban.getOrderId() != null) {
                dto.setOrderNo(orderNoMap.getOrDefault(ban.getOrderId(), "未知"));
            }
            records.add(dto);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", total > 0 ? (int) Math.ceil((double) total / size) : 0);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long banId) {
        UserParkingBan ban = userParkingBanMapper.selectById(banId);
        if (ban == null) {
            throw new BusinessException("封禁记录不存在");
        }
        userParkingBanMapper.deleteById(banId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUnban(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要解封的记录");
        }
        userParkingBanMapper.deleteBatchIds(ids);
    }
}
