-- 智能停车云平台 - 数据库初始化脚本
-- 使用前请先创建数据库: CREATE DATABASE smart_parking DEFAULT CHARACTER SET utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(64) NOT NULL COMMENT '密码(MD5加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `plate_number` VARCHAR(20) DEFAULT NULL COMMENT '默认车牌号',
    `credit_score` INT DEFAULT 100 COMMENT '信用分',
    `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 停车场表
CREATE TABLE IF NOT EXISTS `parking_lot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '停车场ID',
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `total_slots` INT DEFAULT 0 COMMENT '总车位数',
    `free_slots` INT DEFAULT 0 COMMENT '空闲车位数',
    `business_hours` VARCHAR(100) DEFAULT '00:00-24:00' COMMENT '营业时间',
    `rate_per_hour` DECIMAL(10,2) DEFAULT 5.00 COMMENT '每小时费率',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-营业 0-停业',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='停车场表';

-- 车位表
CREATE TABLE IF NOT EXISTS `parking_slot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车位ID',
    `parking_lot_id` BIGINT NOT NULL COMMENT '所属停车场ID',
    `slot_number` VARCHAR(20) NOT NULL COMMENT '车位编号',
    `slot_type` TINYINT DEFAULT 1 COMMENT '类型: 1-普通 2-新能源 3-大型',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-空闲 1-已预约 2-已占用 3-维护中',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parking_lot_id` (`parking_lot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车位表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `slot_id` BIGINT NOT NULL COMMENT '车位ID',
    `parking_lot_id` BIGINT NOT NULL COMMENT '停车场ID',
    `plan_enter_time` DATETIME DEFAULT NULL COMMENT '预计入场时间',
    `plan_duration` INT DEFAULT NULL COMMENT '预计停留时长(分钟)',
    `actual_enter_time` DATETIME DEFAULT NULL COMMENT '实际入场时间',
    `actual_leave_time` DATETIME DEFAULT NULL COMMENT '实际离场时间',
    `total_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '订单金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-已预约 2-已入场 3-已完成 4-已取消',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_slot_id` (`slot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `pay_method` TINYINT DEFAULT 1 COMMENT '支付方式: 1-余额支付 2-微信支付模拟',
    `pay_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '支付时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- ============ 测试数据 ============

-- 测试停车场
INSERT INTO `parking_lot` (`name`, `address`, `longitude`, `latitude`, `total_slots`, `free_slots`, `rate_per_hour`) VALUES
('大连理工大学停车场', '辽宁省大连市甘井子区凌工路2号', 121.5319, 38.8773, 50, 45, 3.00),
('万达广场停车场', '大连市高新园区黄浦路500号', 121.5210, 38.8650, 200, 120, 5.00),
('软件园停车场', '大连市沙河口区软件园路8号', 121.5450, 38.8800, 80, 60, 4.00);

-- 测试用户
INSERT INTO `user` (`phone`, `password`, `nickname`, `role`) VALUES
('13800000000', MD5('admin123'), '系统管理员', 1),
('13900001111', MD5('123456'), '测试车主', 0);

-- ============ 批量生成测试车位 ============
-- 生成 50 个车位（停车场1 = 大连理工大学）
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS gen_slots(IN lot_id BIGINT, IN prefix VARCHAR(2), IN cnt INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= cnt DO
        INSERT INTO parking_slot (parking_lot_id, slot_number, slot_type, status)
        VALUES (lot_id, CONCAT(prefix, LPAD(i, 3, '0')), 1, 0);
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 给3个停车场各生成110个车位（共330个）
CALL gen_slots(1, 'A', 50);
CALL gen_slots(2, 'B', 80);
CALL gen_slots(3, 'C', 80);

-- 设置一些已占用/维护状态的车位（用于测试统计）
UPDATE parking_slot SET status = 3 WHERE parking_lot_id = 1 AND slot_number IN ('A001', 'A002');
UPDATE parking_slot SET status = 2 WHERE parking_lot_id = 1 AND slot_number IN ('A003', 'A004', 'A005');
UPDATE parking_slot SET status = 1 WHERE parking_lot_id = 2 AND slot_number IN ('B001', 'B002');

-- 更新停车场空闲数
UPDATE parking_lot SET free_slots = total_slots;
UPDATE parking_lot SET free_slots = free_slots - 2 WHERE id = 1;  -- A001,A002 维护中
UPDATE parking_lot SET free_slots = free_slots - 2 WHERE id = 2;  -- B001,B002 已预约

-- 清理存储过程
DROP PROCEDURE IF EXISTS gen_slots;

-- 用户停车场封禁表
CREATE TABLE IF NOT EXISTS `user_parking_ban` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '封禁记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `parking_lot_id` BIGINT NOT NULL COMMENT '被封禁的停车场ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '触发封禁的订单ID',
    `banned_until` DATETIME NOT NULL COMMENT '封禁截止时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_parking` (`user_id`, `parking_lot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户停车场封禁表';
