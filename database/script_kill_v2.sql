-- ============================================
-- ScriptKill 新增表结构 SQL
-- 生成时间: 2026-04-09
-- 新增表: notification, room, store, user_favorite, ai_match_log
-- ============================================

-- ----------------------------------------------------
-- 1. 通知消息表 (notification)
-- ----------------------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `team_up_id` bigint DEFAULT NULL COMMENT '拼场ID',
  `session_id` bigint DEFAULT NULL COMMENT '场次ID',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

-- ----------------------------------------------------
-- 2. 门店表 (store)
-- ----------------------------------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_name` varchar(100) NOT NULL COMMENT '门店名称',
  `location` varchar(255) NOT NULL COMMENT '门店地址',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1-正常 0-关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

-- 初始化门店数据
INSERT INTO `store` (`store_name`, `location`, `status`) VALUES
('1号厅', '商场3楼', 1),
('2号厅', '商场4楼', 1),
('3号厅', '写字楼10楼', 1);

-- ----------------------------------------------------
-- 3. 房间表 (room)
-- ----------------------------------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `room_name` varchar(100) NOT NULL COMMENT '房间名称',
  `room_type` varchar(50) DEFAULT '普通' COMMENT '房间类型',
  `capacity` int NOT NULL DEFAULT '8' COMMENT '容纳人数',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态 1-正常 0-关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- 初始化房间数据
INSERT INTO `room` (`store_id`, `room_name`, `room_type`, `capacity`, `status`) VALUES
(1, '推理房1号', '推理房', 8, 1),
(1, '恐怖房1号', '恐怖房', 6, 1),
(2, '沉浸房1号', '沉浸房', 10, 1),
(2, '推理房2号', '推理房', 8, 1),
(3, '情感房1号', '情感房', 6, 1);

-- ----------------------------------------------------
-- 4. 用户收藏表 (user_favorite)
-- ----------------------------------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `script_id` bigint NOT NULL COMMENT '剧本ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_script` (`user_id`, `script_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ----------------------------------------------------
-- 5. AI匹配日志表 (ai_match_log)
-- ----------------------------------------------------
DROP TABLE IF EXISTS `ai_match_log`;
CREATE TABLE `ai_match_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `team_up_id` bigint NOT NULL COMMENT '拼场ID',
  `match_score` decimal(5,2) DEFAULT NULL COMMENT '匹配分数',
  `match_reason` varchar(500) DEFAULT NULL COMMENT '匹配原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_team_up_id` (`team_up_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI匹配日志表';

-- 初始化AI匹配数据（可选）
-- INSERT INTO `ai_match_log` (`user_id`, `team_up_id`, `match_score`, `match_reason`) VALUES
-- (4, 1, 92.50, '偏好情感本，时间匹配度高'),
-- (5, 1, 88.00, '历史参与过同类型剧本');
