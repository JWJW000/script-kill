-- 剧本杀预约管理系统数据库脚本
-- 数据库：script_kill
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS `script_kill` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `script_kill`;

-- 1. 用户表
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` ENUM('PLAYER', 'HOST', 'ADMIN') NOT NULL DEFAULT 'PLAYER' COMMENT '角色：玩家/主持人/管理员',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 剧本表
CREATE TABLE `script` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '剧本ID',
  `name` VARCHAR(100) NOT NULL COMMENT '剧本名称',
  `type` VARCHAR(50) NOT NULL COMMENT '剧本类型：恐怖/情感/硬核/欢乐/推理等',
  `difficulty` ENUM('BEGINNER', 'INTERMEDIATE', 'EXPERT') NOT NULL DEFAULT 'BEGINNER' COMMENT '难度：新手/进阶/烧脑',
  `duration` INT(11) NOT NULL COMMENT '时长（分钟）',
  `min_players` INT(11) NOT NULL COMMENT '最少人数',
  `max_players` INT(11) NOT NULL COMMENT '最多人数',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `description` TEXT COMMENT '简介',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧本表';

-- 3. 场次表
CREATE TABLE `session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `script_id` BIGINT(20) NOT NULL COMMENT '剧本ID',
  `host_id` BIGINT(20) NOT NULL COMMENT '主持人ID',
  `session_time` DATETIME NOT NULL COMMENT '场次时间',
  `location` VARCHAR(200) NOT NULL COMMENT '地点',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `max_players` INT(11) NOT NULL COMMENT '最大人数',
  `current_players` INT(11) NOT NULL DEFAULT 0 COMMENT '当前预约人数',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已取消/下架，1-可预约，2-已满员，3-已结束',
  `attendance_confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '主持人是否确认到场：0-未确认，1-已确认',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_script_id` (`script_id`),
  KEY `idx_host_id` (`host_id`),
  KEY `idx_session_time` (`session_time`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_session_script` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_session_host` FOREIGN KEY (`host_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场次表';

-- 4. 订单表
CREATE TABLE `order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（玩家）',
  `session_id` BIGINT(20) NOT NULL COMMENT '场次ID',
  `player_count` INT(11) NOT NULL COMMENT '预约人数',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：WECHAT/ALIPAY',
  `payment_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已取消，3-已退款',
  `order_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已完成',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_order_status` (`order_status`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 5. 拼场表
CREATE TABLE `team_up` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '拼场ID',
  `script_id` BIGINT(20) NOT NULL COMMENT '剧本ID',
  `initiator_id` BIGINT(20) NOT NULL COMMENT '发起人ID',
  `expected_time` DATETIME NOT NULL COMMENT '期望时间',
  `total_players` INT(11) NOT NULL COMMENT '总人数',
  `current_players` INT(11) NOT NULL DEFAULT 1 COMMENT '当前已拼人数',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已关闭，1-进行中，2-已达标，3-已生成场次',
  `session_id` BIGINT(20) DEFAULT NULL COMMENT '生成的场次ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_script_id` (`script_id`),
  KEY `idx_initiator_id` (`initiator_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_time` (`expected_time`),
  CONSTRAINT `fk_team_up_script` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_team_up_initiator` FOREIGN KEY (`initiator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_team_up_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼场表';

-- 6. 拼场参与表
CREATE TABLE `team_up_participant` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `team_up_id` BIGINT(20) NOT NULL COMMENT '拼场ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_user` (`team_up_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_participant_team` FOREIGN KEY (`team_up_id`) REFERENCES `team_up` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_participant_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼场参与表';

-- 7. 评价表
CREATE TABLE `review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID（玩家）',
  `script_id` BIGINT(20) NOT NULL COMMENT '剧本ID',
  `host_id` BIGINT(20) NOT NULL COMMENT '主持人ID',
  `script_rating` INT(11) NOT NULL COMMENT '剧本评分（1-5星）',
  `host_rating` INT(11) NOT NULL COMMENT '主持人评分（1-5星）',
  `script_comment` TEXT COMMENT '剧本评价内容',
  `host_comment` TEXT COMMENT '主持人评价内容',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_review` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_script_id` (`script_id`),
  KEY `idx_host_id` (`host_id`),
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_script` FOREIGN KEY (`script_id`) REFERENCES `script` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_review_host` FOREIGN KEY (`host_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- 8. AI匹配日志表
CREATE TABLE `ai_match_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `team_up_id` BIGINT(20) NOT NULL COMMENT '拼场ID',
  `match_score` DECIMAL(5,2) DEFAULT NULL COMMENT '匹配分数',
  `match_reason` VARCHAR(500) DEFAULT NULL COMMENT '匹配原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_team_up_id` (`team_up_id`),
  CONSTRAINT `fk_match_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_match_log_team` FOREIGN KEY (`team_up_id`) REFERENCES `team_up` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI匹配日志表';

-- 初始化数据
-- 管理员账号（密码：admin123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', MD5('admin123'), '系统管理员', 'ADMIN', 1);

-- 示例主持人账号（密码：host123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('host001', MD5('host123'), '主持人001', 'HOST', 1);

-- 示例剧本数据
INSERT INTO `script` (`name`, `type`, `difficulty`, `duration`, `min_players`, `max_players`, `price`, `description`, `status`) VALUES
('古木吟', '情感', 'BEGINNER', 240, 6, 6, 88.00, '一个关于青春、友情、成长的情感本', 1),
('年轮', '硬核', 'EXPERT', 300, 5, 5, 128.00, '烧脑推理，时间循环的经典之作', 1),
('病娇男孩的精分日记', '恐怖', 'INTERMEDIATE', 180, 7, 7, 98.00, '细思极恐的恐怖本，多重人格的悬疑故事', 1),
('来电', '欢乐', 'BEGINNER', 150, 6, 6, 78.00, '轻松欢乐的机制本，适合新手', 1);


INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
('player001', MD5('123456'), '张三', '13800000001', 'PLAYER', 1),
('player002', MD5('123456'), '李四', '13800000002', 'PLAYER', 1),
('player003', MD5('123456'), '王五', '13800000003', 'PLAYER', 1),
('player004', MD5('123456'), '赵六', '13800000004', 'PLAYER', 1),
('player005', MD5('123456'), '孙七', '13800000005', 'PLAYER', 1);


INSERT INTO `session`
(`script_id`, `host_id`, `session_time`, `location`, `price`, `max_players`, `current_players`, `status`)
VALUES
(1, 2, '2026-03-01 14:00:00', '一号店·情感厅', 88.00, 6, 3, 1),
(2, 2, '2026-03-02 13:30:00', '一号店·硬核厅', 128.00, 5, 5, 2),
(3, 2, '2026-03-03 19:00:00', '二号店·恐怖厅', 98.00, 7, 2, 1),
(4, 2, '2026-03-04 15:00:00', '二号店·欢乐厅', 78.00, 6, 6, 3);


INSERT INTO `order`
(`order_no`, `user_id`, `session_id`, `player_count`, `total_amount`,
 `payment_method`, `payment_status`, `order_status`, `pay_time`)
VALUES
('ORD20260301001', 3, 1, 2, 176.00, 'WECHAT', 1, 1, '2026-02-25 10:15:00'),
('ORD20260301002', 4, 1, 1, 88.00, 'ALIPAY', 1, 1, '2026-02-25 11:20:00'),
('ORD20260302001', 5, 2, 1, 128.00, 'WECHAT', 1, 3, '2026-02-26 09:30:00'),
('ORD20260303001', 6, 3, 2, 196.00, NULL, 0, 0, NULL);


INSERT INTO `team_up`
(`script_id`, `initiator_id`, `expected_time`, `total_players`,
 `current_players`, `remark`, `status`)
VALUES
(1, 3, '2026-03-05 14:00:00', 6, 3, '想组个情感本车', 1),
(2, 4, '2026-03-06 13:00:00', 5, 5, '硬核老玩家来', 2),
(3, 5, '2026-03-07 19:30:00', 7, 2, '胆小勿入', 1);

INSERT INTO `team_up_participant` (`team_up_id`, `user_id`) VALUES
(1, 3),
(1, 4),
(1, 5),
(2, 4),
(2, 5),
(2, 6),
(2, 3),
(2, 7),
(3, 5),
(3, 6);

INSERT INTO `review`
(`order_id`, `user_id`, `script_id`, `host_id`,
 `script_rating`, `host_rating`, `script_comment`, `host_comment`)
VALUES
(1, 3, 1, 2, 5, 5, '剧情非常走心，体验感拉满', '主持节奏把控很好'),
(2, 4, 1, 2, 4, 5, '整体不错，结尾略平', '主持人很专业'),
(3, 5, 2, 2, 5, 4, '推理强度很高，值得二刷', '讲解清晰');


INSERT INTO `ai_match_log`
(`user_id`, `team_up_id`, `match_score`, `match_reason`)
VALUES
(4, 1, 92.50, '偏好情感本，时间匹配度高'),
(5, 1, 88.00, '历史参与过同类型剧本'),
(6, 3, 85.30, '恐怖本参与频率高'),
(3, 2, 90.10, '硬核推理评分较高');

-- 若场次表已存在且无 attendance_confirmed 列，可执行：
-- ALTER TABLE `session` ADD COLUMN `attendance_confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '主持人是否确认到场：0-未确认，1-已确认';