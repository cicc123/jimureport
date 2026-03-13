-- =====================================================
-- 报表权限分配功能 - 数据库表结构
-- 版本: 1.0.0
-- 创建时间: 2024
-- 说明: 实现用户分组+报表权限分配+多Portal页权限展示
-- =====================================================

-- -----------------------------------------------------
-- 表 jimu_user_group - 用户组表
-- 说明: 存储用户组信息，支持管理员组和普通用户组
-- 设计理由:
--   - group_type: 区分管理员组(1)和普通用户组(2)，便于权限判断
--   - tenant_id: 支持多租户数据隔离
--   - status: 支持启用/禁用用户组
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jimu_user_group` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户组名称',
  `group_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户组编码',
  `group_type` int NOT NULL DEFAULT 2 COMMENT '用户组类型: 1-管理员组, 2-普通用户组',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户组描述',
  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '租户ID',
  `status` int DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_code`(`group_code` ASC) USING BTREE,
  INDEX `idx_group_type`(`group_type` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '用户组表';

-- -----------------------------------------------------
-- 表 jimu_user_group_member - 用户-分组关联表
-- 说明: 存储用户与用户组的关联关系
-- 设计理由:
--   - 支持一个用户属于多个用户组
--   - 使用联合唯一索引防止重复关联
--   - 添加user_id和group_id索引提高查询效率
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jimu_user_group_member` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
  `group_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户组ID',
  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_group`(`user_id` ASC, `group_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '用户-分组关联表';

-- -----------------------------------------------------
-- 表 jimu_report_permission - 报表权限分配表
-- 说明: 存储报表与用户/用户组的权限分配关系
-- 设计理由:
--   - target_type: 区分权限分配目标是用户(1)还是用户组(2)
--   - target_id: 存储用户ID或用户组ID
--   - permission_type: 支持查看(view)、编辑(edit)、删除(delete)等权限
--   - 使用联合索引提高权限查询效率
--   - 支持权限优先级: 用户直接权限 > 用户组权限
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `jimu_report_permission` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键ID',
  `report_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报表ID',
  `target_type` int NOT NULL COMMENT '权限目标类型: 1-用户, 2-用户组',
  `target_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '目标ID(用户ID或用户组ID)',
  `permission_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'view' COMMENT '权限类型: view-查看, edit-编辑, delete-删除',
  `tenant_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_report_target`(`report_id` ASC, `target_type` ASC, `target_id` ASC, `permission_type` ASC) USING BTREE,
  INDEX `idx_report_id`(`report_id` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '报表权限分配表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认用户组
-- 管理员组ID固定为1，普通用户组ID固定为2
INSERT INTO `jimu_user_group` (`id`, `group_name`, `group_code`, `group_type`, `description`, `tenant_id`, `status`, `create_by`) VALUES
('1', '管理员组', 'admin_group', 1, '系统管理员组，拥有所有权限', '1', 1, 'admin'),
('2', '普通用户组', 'user_group', 2, '普通用户组，需要分配报表权限', '1', 1, 'admin');

-- 将admin用户加入管理员组
INSERT INTO `jimu_user_group_member` (`id`, `user_id`, `group_id`, `tenant_id`, `create_by`) VALUES
('1', '1', '1', '1', 'admin');

-- =====================================================
-- 表结构变更版本控制说明
-- =====================================================
-- V1.0.0 - 初始版本
--   - 创建 jimu_user_group 表
--   - 创建 jimu_user_group_member 表
--   - 创建 jimu_report_permission 表
--   - 初始化管理员组和普通用户组
-- 
-- 与原有系统的兼容性说明:
--   - 新表与原有表无直接外键关联，避免影响原有数据
--   - 通过 user_id 关联 jimu_user 表
--   - 通过 report_id 关联 jimu_report 表
--   - 通过 tenant_id 支持多租户隔离
--   - 所有表都使用 varchar(36) 作为主键类型，与原有表保持一致
-- =====================================================
