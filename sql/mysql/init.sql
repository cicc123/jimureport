-- =====================================================
-- JimuReport Enhancement - 数据库初始化脚本
-- 版本: 1.0.0
-- 日期: 2024-01-01
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS jimureport_enhancement DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE jimureport_enhancement;

-- =====================================================
-- 1. 用户管理模块
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '用户姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `sex` CHAR(1) DEFAULT '2' COMMENT '用户性别(0男 1女 2未知)',
    `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像地址',
    `dept_id` VARCHAR(32) DEFAULT NULL COMMENT '部门ID',
    `status` CHAR(1) DEFAULT '0' COMMENT '帐号状态(0正常 1停用)',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',
    `login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `login_date` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 部门表
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` VARCHAR(32) NOT NULL COMMENT '部门ID',
    `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `parent_id` VARCHAR(32) DEFAULT '0' COMMENT '父部门ID',
    `ancestors` VARCHAR(500) DEFAULT NULL COMMENT '祖级列表',
    `order_num` INT DEFAULT 0 COMMENT '显示顺序',
    `leader` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` CHAR(1) DEFAULT '0' COMMENT '部门状态(0正常 1停用)',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` VARCHAR(32) NOT NULL COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    `role_sort` INT NOT NULL COMMENT '显示顺序',
    `data_scope` CHAR(1) DEFAULT '1' COMMENT '数据范围(1:全部 2:自定 3:本部门 4:本部门及以下 5:仅本人)',
    `status` CHAR(1) DEFAULT '0' COMMENT '角色状态(0正常 1停用)',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0代表存在 2代表删除)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` VARCHAR(32) NOT NULL COMMENT '菜单ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `parent_id` VARCHAR(32) DEFAULT '0' COMMENT '父菜单ID',
    `order_num` INT DEFAULT 0 COMMENT '显示顺序',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `is_frame` CHAR(1) DEFAULT '1' COMMENT '是否为外链(0是 1否)',
    `is_cache` CHAR(1) DEFAULT '0' COMMENT '是否缓存(0缓存 1不缓存)',
    `menu_type` CHAR(1) DEFAULT NULL COMMENT '菜单类型(M目录 C菜单 F按钮)',
    `visible` CHAR(1) DEFAULT '0' COMMENT '菜单状态(0显示 1隐藏)',
    `status` CHAR(1) DEFAULT '0' COMMENT '菜单状态(0正常 1停用)',
    `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `icon` VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
    `menu_id` VARCHAR(32) NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- =====================================================
-- 2. 权限管理模块
-- =====================================================

-- 报表权限表
CREATE TABLE IF NOT EXISTS `jimu_report_permission` (
    `id` VARCHAR(32) NOT NULL COMMENT '权限ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报表ID',
    `role_id` VARCHAR(32) DEFAULT NULL COMMENT '角色ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型(view:查看 design:设计 fill:填报)',
    `data_scope` VARCHAR(20) DEFAULT 'all' COMMENT '数据范围(all:全部 dept:部门 self:个人 custom:自定义)',
    `data_scope_rule` TEXT DEFAULT NULL COMMENT '数据范围规则(JSON格式)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表权限表';

-- 数据权限表
CREATE TABLE IF NOT EXISTS `jimu_data_permission` (
    `id` VARCHAR(32) NOT NULL COMMENT '权限ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报表ID',
    `role_id` VARCHAR(32) DEFAULT NULL COMMENT '角色ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT '用户ID',
    `table_name` VARCHAR(100) NOT NULL COMMENT '表名',
    `condition_field` VARCHAR(100) NOT NULL COMMENT '条件字段',
    `condition_type` VARCHAR(20) NOT NULL COMMENT '条件类型(eq:等于 like:包含 gt:大于 lt:小于 in:包含于)',
    `condition_value` VARCHAR(500) DEFAULT NULL COMMENT '条件值',
    `value_type` VARCHAR(20) DEFAULT 'static' COMMENT '值类型(static:静态 user_id:用户ID dept_id:部门ID)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限表';

-- =====================================================
-- 3. 填报增强模块
-- =====================================================

-- 填报表单配置表
CREATE TABLE IF NOT EXISTS `jimu_fill_form_config` (
    `id` VARCHAR(32) NOT NULL COMMENT '配置ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报表ID',
    `form_name` VARCHAR(100) NOT NULL COMMENT '表单名称',
    `form_desc` VARCHAR(500) DEFAULT NULL COMMENT '表单描述',
    `submit_type` VARCHAR(20) DEFAULT 'database' COMMENT '提交方式(database:数据库 api:接口 java:自定义Java)',
    `submit_config` TEXT DEFAULT NULL COMMENT '提交配置(JSON格式)',
    `allow_draft` CHAR(1) DEFAULT '0' COMMENT '是否允许草稿(0否 1是)',
    `allow_duplicate` CHAR(1) DEFAULT '0' COMMENT '是否允许重复提交(0否 1是)',
    `duplicate_check_fields` VARCHAR(500) DEFAULT NULL COMMENT '重复检查字段(逗号分隔)',
    `success_message` VARCHAR(200) DEFAULT '提交成功' COMMENT '成功提示消息',
    `redirect_url` VARCHAR(500) DEFAULT NULL COMMENT '提交后跳转URL',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='填报表单配置表';

-- 填报表单字段配置表
CREATE TABLE IF NOT EXISTS `jimu_fill_form_field` (
    `id` VARCHAR(32) NOT NULL COMMENT '字段ID',
    `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
    `field_name` VARCHAR(100) NOT NULL COMMENT '字段名称',
    `field_label` VARCHAR(100) NOT NULL COMMENT '字段标签',
    `field_type` VARCHAR(20) NOT NULL COMMENT '字段类型',
    `data_type` VARCHAR(20) DEFAULT 'string' COMMENT '数据类型',
    `data_source_type` VARCHAR(20) DEFAULT 'manual' COMMENT '数据来源',
    `data_source_config` TEXT DEFAULT NULL COMMENT '数据来源配置(JSON格式)',
    `default_value` VARCHAR(500) DEFAULT NULL COMMENT '默认值',
    `default_value_type` VARCHAR(20) DEFAULT 'static' COMMENT '默认值类型',
    `is_required` CHAR(1) DEFAULT '0' COMMENT '是否必填(0否 1是)',
    `validation_rule` TEXT DEFAULT NULL COMMENT '校验规则(JSON格式)',
    `placeholder` VARCHAR(200) DEFAULT NULL COMMENT '占位提示',
    `help_text` VARCHAR(500) DEFAULT NULL COMMENT '帮助文本',
    `min_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最小值',
    `max_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最大值',
    `min_length` INT DEFAULT NULL COMMENT '最小长度',
    `max_length` INT DEFAULT NULL COMMENT '最大长度',
    `order_num` INT DEFAULT 0 COMMENT '显示顺序',
    `is_visible` CHAR(1) DEFAULT '1' COMMENT '是否可见(0否 1是)',
    `is_readonly` CHAR(1) DEFAULT '0' COMMENT '是否只读(0否 1是)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_form_id` (`form_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='填报表单字段配置表';

-- 填报数据草稿表
CREATE TABLE IF NOT EXISTS `jimu_fill_draft` (
    `id` VARCHAR(32) NOT NULL COMMENT '草稿ID',
    `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `draft_data` TEXT NOT NULL COMMENT '草稿数据(JSON格式)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_form_user` (`form_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='填报数据草稿表';

-- 填报提交记录表
CREATE TABLE IF NOT EXISTS `jimu_fill_submit_record` (
    `id` VARCHAR(32) NOT NULL COMMENT '记录ID',
    `form_id` VARCHAR(32) NOT NULL COMMENT '表单ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `submit_data` TEXT NOT NULL COMMENT '提交数据(JSON格式)',
    `submit_status` VARCHAR(20) DEFAULT 'success' COMMENT '提交状态(success:成功 fail:失败)',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_form_id` (`form_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='填报提交记录表';

-- =====================================================
-- 4. 设计器增强模块
-- =====================================================

-- 报表字段默认值配置表
CREATE TABLE IF NOT EXISTS `jimu_report_field_default` (
    `id` VARCHAR(32) NOT NULL COMMENT '配置ID',
    `report_id` VARCHAR(32) NOT NULL COMMENT '报表ID',
    `field_name` VARCHAR(100) NOT NULL COMMENT '字段名称',
    `field_label` VARCHAR(100) DEFAULT NULL COMMENT '字段标签',
    `default_type` VARCHAR(20) NOT NULL COMMENT '默认值类型',
    `default_value` VARCHAR(500) DEFAULT NULL COMMENT '默认值',
    `is_hidden` CHAR(1) DEFAULT '0' COMMENT '是否隐藏字段(0否 1是)',
    `is_readonly` CHAR(1) DEFAULT '0' COMMENT '是否只读(0否 1是)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表字段默认值配置表';

-- =====================================================
-- 5. 系统管理模块
-- =====================================================

-- 登录日志表
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` VARCHAR(32) NOT NULL COMMENT '日志ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户账号',
    `status` VARCHAR(10) NOT NULL COMMENT '登录状态(success:成功 fail:失败)',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '登录IP地址',
    `login_location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器类型',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '提示消息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id` VARCHAR(32) NOT NULL COMMENT '日志ID',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '模块标题',
    `business_type` INT DEFAULT 0 COMMENT '业务类型(0其它 1新增 2修改 3删除)',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '方法名称',
    `request_method` VARCHAR(20) DEFAULT NULL COMMENT '请求方式',
    `operator_type` INT DEFAULT 0 COMMENT '操作类别(0其它 1后台用户 2手机端用户)',
    `oper_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
    `dept_name` VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    `oper_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `oper_ip` VARCHAR(50) DEFAULT NULL COMMENT '主机地址',
    `oper_location` VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
    `oper_param` TEXT DEFAULT NULL COMMENT '请求参数',
    `json_result` TEXT DEFAULT NULL COMMENT '返回参数',
    `status` INT DEFAULT 0 COMMENT '状态(0正常 1异常)',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误消息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` VARCHAR(32) NOT NULL COMMENT '配置ID',
    `config_name` VARCHAR(100) DEFAULT NULL COMMENT '配置名称',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键名',
    `config_value` TEXT DEFAULT NULL COMMENT '配置键值',
    `config_type` CHAR(1) DEFAULT 'N' COMMENT '系统内置(Y是 N否)',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =====================================================
-- 6. 初始化数据
-- =====================================================

-- 初始化管理员账号 (密码: admin123)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `status`, `del_flag`, `create_by`) 
VALUES ('1', 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', '0', '0', 'system')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 初始化部门
INSERT INTO `sys_department` (`id`, `dept_name`, `parent_id`, `order_num`, `status`, `del_flag`, `create_by`) 
VALUES ('1', '总公司', '0', 1, '0', '0', 'system')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 初始化角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `del_flag`, `create_by`) 
VALUES 
('1', '超级管理员', 'admin', 1, '1', '0', '0', 'system'),
('2', '普通用户', 'user', 2, '5', '0', '0', 'system'),
('3', '报表设计师', 'designer', 3, '2', '0', '0', 'system')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 初始化用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) 
VALUES ('1', '1')
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

-- 初始化菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `menu_type`, `perms`, `icon`, `create_by`) 
VALUES 
('1', '系统管理', '0', 1, 'system', NULL, 'M', NULL, 'setting', 'system'),
('2', '用户管理', '1', 1, 'user', 'system/user/index', 'C', 'system:user:list', 'user', 'system'),
('3', '角色管理', '1', 2, 'role', 'system/role/index', 'C', 'system:role:list', 'peoples', 'system'),
('4', '菜单管理', '1', 3, 'menu', 'system/menu/index', 'C', 'system:menu:list', 'tree-table', 'system'),
('5', '部门管理', '1', 4, 'dept', 'system/dept/index', 'C', 'system:dept:list', 'tree', 'system'),
('6', '报表管理', '0', 2, 'report', NULL, 'M', NULL, 'chart', 'system'),
('7', '报表列表', '6', 1, 'list', 'report/list/index', 'C', 'report:list:list', 'list', 'system'),
('8', '报表设计', '6', 2, 'design', 'report/design/index', 'C', 'report:design:list', 'edit', 'system'),
('9', '填报管理', '6', 3, 'fill', 'report/fill/index', 'C', 'report:fill:list', 'form', 'system'),
('10', '权限管理', '0', 3, 'permission', NULL, 'M', NULL, 'lock', 'system'),
('11', '报表权限', '10', 1, 'report', 'permission/report/index', 'C', 'report:permission:list', 'lock', 'system'),
('12', '数据权限', '10', 2, 'data', 'permission/data/index', 'C', 'report:permission:data', 'database', 'system')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- 初始化系统配置
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`) 
VALUES 
('1', '系统名称', 'sys.name', 'JimuReport增强版', 'Y', 'system'),
('2', '系统版本', 'sys.version', '1.0.0', 'Y', 'system'),
('3', '用户注册', 'sys.register.enabled', 'true', 'N', 'system'),
('4', '密码强度', 'sys.password.strength', 'medium', 'N', 'system'),
('5', '登录超时', 'sys.login.timeout', '30', 'N', 'system')
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

-- =====================================================
-- 7. 创建视图
-- =====================================================

-- 用户详情视图
CREATE OR REPLACE VIEW `v_user_detail` AS
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    u.sex,
    u.avatar,
    u.status,
    u.dept_id,
    d.dept_name,
    u.login_ip,
    u.login_date,
    u.create_time,
    GROUP_CONCAT(r.role_name) AS role_names,
    GROUP_CONCAT(r.role_key) AS role_keys
FROM sys_user u
LEFT JOIN sys_department d ON u.dept_id = d.id
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
WHERE u.del_flag = '0'
GROUP BY u.id;

-- 报表权限视图
CREATE OR REPLACE VIEW `v_report_permission` AS
SELECT 
    p.id,
    p.report_id,
    p.permission_type,
    p.data_scope,
    p.data_scope_rule,
    CASE 
        WHEN p.role_id IS NOT NULL THEN r.role_name
        WHEN p.user_id IS NOT NULL THEN u.real_name
    END AS grantee_name,
    CASE 
        WHEN p.role_id IS NOT NULL THEN 'role'
        WHEN p.user_id IS NOT NULL THEN 'user'
    END AS grantee_type,
    p.role_id,
    p.user_id
FROM jimu_report_permission p
LEFT JOIN sys_role r ON p.role_id = r.id
LEFT JOIN sys_user u ON p.user_id = u.id;

-- =====================================================
-- 8. 创建存储过程
-- =====================================================

DELIMITER //

-- 获取用户权限
CREATE PROCEDURE IF NOT EXISTS `sp_get_user_permissions`(
    IN p_user_id VARCHAR(32)
)
BEGIN
    -- 如果是超级管理员，返回所有权限
    IF EXISTS (
        SELECT 1 FROM sys_user_role ur 
        JOIN sys_role r ON ur.role_id = r.id 
        WHERE ur.user_id = p_user_id AND r.role_key = 'admin'
    ) THEN
        SELECT DISTINCT perms FROM sys_menu WHERE perms IS NOT NULL AND status = '0';
    ELSE
        -- 返回用户角色对应的权限
        SELECT DISTINCT m.perms 
        FROM sys_user_role ur
        JOIN sys_role_menu rm ON ur.role_id = rm.role_id
        JOIN sys_menu m ON rm.menu_id = m.id
        WHERE ur.user_id = p_user_id 
        AND m.perms IS NOT NULL 
        AND m.status = '0';
    END IF;
END //

-- 检查报表访问权限
CREATE PROCEDURE IF NOT EXISTS `sp_check_report_permission`(
    IN p_user_id VARCHAR(32),
    IN p_report_id VARCHAR(32),
    IN p_permission_type VARCHAR(20),
    OUT p_has_permission BOOLEAN
)
BEGIN
    DECLARE v_is_admin BOOLEAN DEFAULT FALSE;
    
    -- 检查是否是超级管理员
    SELECT EXISTS (
        SELECT 1 FROM sys_user_role ur 
        JOIN sys_role r ON ur.role_id = r.id 
        WHERE ur.user_id = p_user_id AND r.role_key = 'admin'
    ) INTO v_is_admin;
    
    IF v_is_admin THEN
        SET p_has_permission = TRUE;
    ELSE
        -- 检查报表权限
        SELECT EXISTS (
            SELECT 1 FROM jimu_report_permission 
            WHERE report_id = p_report_id 
            AND permission_type = p_permission_type
            AND (
                user_id = p_user_id 
                OR role_id IN (
                    SELECT role_id FROM sys_user_role WHERE user_id = p_user_id
                )
            )
        ) INTO p_has_permission;
    END IF;
END //

DELIMITER ;

-- =====================================================
-- 完成
-- =====================================================
