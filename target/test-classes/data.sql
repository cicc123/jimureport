-- 测试数据

-- 管理员账号 (密码: admin123 的 BCrypt 哈希)
INSERT INTO sys_user (id, username, password, real_name, email, status, del_flag, create_by)
VALUES ('1', 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'admin@test.com', '0', '0', 'system');

-- 普通用户
INSERT INTO sys_user (id, username, password, real_name, email, phone, dept_id, status, del_flag, create_by)
VALUES ('2', 'testuser', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', 'test@test.com', '13800138000', '1', '0', '0', 'system');

-- 已停用用户
INSERT INTO sys_user (id, username, password, real_name, status, del_flag, create_by)
VALUES ('3', 'disabled', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '停用用户', '1', '0', 'system');

-- 已删除用户
INSERT INTO sys_user (id, username, password, real_name, status, del_flag, create_by)
VALUES ('4', 'deleted', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '已删除用户', '0', '2', 'system');

-- 部门
INSERT INTO sys_department (id, dept_name, parent_id, order_num, status, del_flag, create_by)
VALUES ('1', '总公司', '0', 1, '0', '0', 'system');
INSERT INTO sys_department (id, dept_name, parent_id, order_num, status, del_flag, create_by)
VALUES ('2', '技术部', '1', 1, '0', '0', 'system');

-- 角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by)
VALUES ('1', '超级管理员', 'admin', 1, '1', '0', '0', 'system');
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by)
VALUES ('2', '普通用户', 'user', 2, '5', '0', '0', 'system');
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by)
VALUES ('3', '报表设计师', 'designer', 3, '2', '0', '0', 'system');

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES ('1', '1');
INSERT INTO sys_user_role (user_id, role_id) VALUES ('2', '2');

-- 菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('1', '系统管理', '0', 1, 'M', NULL, '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('2', '用户管理', '1', 1, 'C', 'system:user:list', '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('3', '角色管理', '1', 2, 'C', 'system:role:list', '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('4', '菜单管理', '1', 3, 'C', 'system:menu:list', '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('5', '报表管理', '0', 2, 'M', NULL, '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('6', '报表列表', '5', 1, 'C', 'report:list:list', '0', 'system');
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, menu_type, perms, status, create_by)
VALUES ('7', '填报管理', '5', 2, 'C', 'report:fill:list', '0', 'system');

-- 角色菜单关联
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ('2', '2');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ('2', '5');
INSERT INTO sys_role_menu (role_id, menu_id) VALUES ('2', '6');

-- 报表权限
INSERT INTO jimu_report_permission (id, report_id, user_id, permission_type, data_scope, create_by)
VALUES ('1', 'report-001', '2', 'view', 'all', 'system');
INSERT INTO jimu_report_permission (id, report_id, role_id, permission_type, data_scope, create_by)
VALUES ('2', 'report-001', '3', 'design', 'dept', 'system');

-- 数据权限
INSERT INTO jimu_data_permission (id, report_id, user_id, table_name, condition_field, condition_type, condition_value, value_type, create_by)
VALUES ('1', 'report-001', '2', 'sys_user', 'dept_id', 'eq', '1', 'static', 'system');

-- 填报表单
INSERT INTO jimu_fill_form_config (id, report_id, form_name, form_desc, status, allow_duplicate, duplicate_check_fields, create_by)
VALUES ('form-001', 'report-001', '测试表单', '用于测试的填报表单', '0', '0', 'name', 'system');
INSERT INTO jimu_fill_form_config (id, report_id, form_name, status, allow_duplicate, create_by)
VALUES ('form-002', 'report-002', '已停用表单', '1', '1', 'system');

-- 填报字段
INSERT INTO jimu_fill_form_field (id, form_id, field_name, field_label, field_type, is_required, max_length, order_num, create_by)
VALUES ('field-001', 'form-001', 'name', '姓名', 'text', '1', 50, 1, 'system');
INSERT INTO jimu_fill_form_field (id, form_id, field_name, field_label, field_type, is_required, min_value, max_value, order_num, create_by)
VALUES ('field-002', 'form-001', 'age', '年龄', 'number', '1', 18, 100, 2, 'system');
INSERT INTO jimu_fill_form_field (id, form_id, field_name, field_label, field_type, is_required, order_num, create_by)
VALUES ('field-003', 'form-001', 'email', '邮箱', 'email', '0', 3, 'system');

-- 填报提交记录
INSERT INTO jimu_fill_submit_record (id, form_id, user_id, submit_data, submit_status, create_time)
VALUES ('record-001', 'form-001', '2', '{"name":"张三","age":28,"email":"zhang@test.com"}', 'success', CURRENT_TIMESTAMP);

-- 草稿
INSERT INTO jimu_fill_draft (id, form_id, user_id, draft_data)
VALUES ('draft-001', 'form-001', '2', '{"name":"李四","age":25}');

-- 系统配置
INSERT INTO sys_config (id, config_name, config_key, config_value, config_type, create_by)
VALUES ('1', '系统名称', 'sys.name', 'JimuReport增强版', 'Y', 'system');
