package com.jeecg.modules.jmreport.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        // 初始化多租户相关表结构
        initMultiTenantTables();
    }

    private void initMultiTenantTables() {
        String[] createTables = {
            "CREATE TABLE IF NOT EXISTS jimu_tenant (id VARCHAR(36) NOT NULL PRIMARY KEY, tenant_name VARCHAR(100) NOT NULL, tenant_code VARCHAR(50) NOT NULL, status INT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user (id VARCHAR(36) NOT NULL PRIMARY KEY, username VARCHAR(50) NOT NULL, password VARCHAR(255) NOT NULL, real_name VARCHAR(50), tenant_id VARCHAR(36), status INT DEFAULT 1, is_admin INT DEFAULT 0, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_role (id VARCHAR(36) NOT NULL PRIMARY KEY, role_name VARCHAR(50) NOT NULL, role_code VARCHAR(50) NOT NULL, tenant_id VARCHAR(36), status INT DEFAULT 1, description VARCHAR(255), sort_order INT DEFAULT 0, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, perm_code VARCHAR(100) NOT NULL, perm_name VARCHAR(100) NOT NULL, perm_type VARCHAR(20), parent_id VARCHAR(36), sort_order INT DEFAULT 0, description VARCHAR(255), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_role (id VARCHAR(36) NOT NULL PRIMARY KEY, user_id VARCHAR(36) NOT NULL, role_id VARCHAR(36) NOT NULL, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_role_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, role_id VARCHAR(36) NOT NULL, perm_id VARCHAR(36) NOT NULL, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_group (id VARCHAR(36) NOT NULL PRIMARY KEY, group_name VARCHAR(100) NOT NULL, group_code VARCHAR(50) NOT NULL, group_type INT DEFAULT 2, parent_id VARCHAR(36), tenant_id VARCHAR(36), description VARCHAR(255), status INT DEFAULT 1, sort_order INT DEFAULT 0, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_group_member (id VARCHAR(36) NOT NULL PRIMARY KEY, group_id VARCHAR(36) NOT NULL, user_id VARCHAR(36) NOT NULL, tenant_id VARCHAR(36), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_resource (id VARCHAR(36) NOT NULL PRIMARY KEY, resource_name VARCHAR(100) NOT NULL, resource_code VARCHAR(100), resource_type INT NOT NULL, parent_id VARCHAR(36), tenant_id VARCHAR(36), path VARCHAR(255), icon VARCHAR(100), sort_order INT DEFAULT 0, status INT DEFAULT 1, description VARCHAR(255), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_resource_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, resource_id VARCHAR(36) NOT NULL, target_type INT NOT NULL, target_id VARCHAR(36) NOT NULL, permission_bits INT DEFAULT 1, tenant_id VARCHAR(36), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_data_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, resource_id VARCHAR(36) NOT NULL, target_type INT NOT NULL, target_id VARCHAR(36) NOT NULL, filter_type INT NOT NULL, filter_rule TEXT NOT NULL, tenant_id VARCHAR(36), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_form_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, form_id VARCHAR(36) NOT NULL, user_id VARCHAR(36), role_id VARCHAR(36), group_id VARCHAR(36), tenant_id VARCHAR(36) NOT NULL, perms VARCHAR(255) NOT NULL, create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_report_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, report_id VARCHAR(36) NOT NULL, target_type INT NOT NULL, target_id VARCHAR(36) NOT NULL, permission_type VARCHAR(50) NOT NULL, tenant_id VARCHAR(36), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_audit_log (id VARCHAR(36) NOT NULL PRIMARY KEY, user_id VARCHAR(36), username VARCHAR(50), tenant_id VARCHAR(36), operation VARCHAR(100) NOT NULL, content TEXT, ip VARCHAR(50), module VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_permission_config (id VARCHAR(36) NOT NULL PRIMARY KEY, config_key VARCHAR(100) NOT NULL, config_value TEXT, config_type VARCHAR(50), description VARCHAR(255), tenant_id VARCHAR(36), create_by VARCHAR(50), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_by VARCHAR(50), update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)"
        };

        String[] createIndexes = {
            "CREATE UNIQUE INDEX uk_tenant_code ON jimu_tenant(tenant_code)",
            "CREATE UNIQUE INDEX uk_username ON jimu_user(username)",
            "CREATE INDEX idx_tenant_id ON jimu_user(tenant_id)",
            "CREATE UNIQUE INDEX uk_role_code ON jimu_role(role_code)",
            "CREATE INDEX idx_role_tenant_id ON jimu_role(tenant_id)",
            "CREATE UNIQUE INDEX uk_perm_code ON jimu_permission(perm_code)",
            "CREATE INDEX idx_perm_parent ON jimu_permission(parent_id)",
            "CREATE UNIQUE INDEX uk_user_role ON jimu_user_role(user_id, role_id)",
            "CREATE INDEX idx_user_id ON jimu_user_role(user_id)",
            "CREATE INDEX idx_role_id ON jimu_user_role(role_id)",
            "CREATE UNIQUE INDEX uk_role_perm ON jimu_role_permission(role_id, perm_id)",
            "CREATE INDEX idx_rp_role_id ON jimu_role_permission(role_id)",
            "CREATE INDEX idx_perm_id ON jimu_role_permission(perm_id)",
            "CREATE UNIQUE INDEX uk_group_code ON jimu_user_group(group_code)",
            "CREATE INDEX idx_group_tenant_id ON jimu_user_group(tenant_id)",
            "CREATE INDEX idx_group_parent ON jimu_user_group(parent_id)",
            "CREATE UNIQUE INDEX uk_group_member ON jimu_user_group_member(group_id, user_id)",
            "CREATE INDEX idx_gm_group_id ON jimu_user_group_member(group_id)",
            "CREATE INDEX idx_gm_user_id ON jimu_user_group_member(user_id)",
            "CREATE INDEX idx_resource_parent ON jimu_resource(parent_id)",
            "CREATE INDEX idx_resource_tenant ON jimu_resource(tenant_id)",
            "CREATE INDEX idx_resource_type ON jimu_resource(resource_type)",
            "CREATE UNIQUE INDEX uk_resource_permission ON jimu_resource_permission(resource_id, target_type, target_id)",
            "CREATE INDEX idx_rp_resource ON jimu_resource_permission(resource_id)",
            "CREATE INDEX idx_rp_target ON jimu_resource_permission(target_type, target_id)",
            "CREATE INDEX idx_dp_resource ON jimu_data_permission(resource_id)",
            "CREATE INDEX idx_dp_target ON jimu_data_permission(target_type, target_id)",
            "CREATE INDEX idx_form_id ON jimu_form_permission(form_id)",
            "CREATE INDEX idx_fp_user_id ON jimu_form_permission(user_id)",
            "CREATE INDEX idx_fp_role_id ON jimu_form_permission(role_id)",
            "CREATE INDEX idx_fp_group_id ON jimu_form_permission(group_id)",
            "CREATE INDEX idx_fp_tenant_id ON jimu_form_permission(tenant_id)",
            "CREATE INDEX idx_report_id ON jimu_report_permission(report_id)",
            "CREATE INDEX idx_rp_target_type ON jimu_report_permission(target_type)",
            "CREATE INDEX idx_rp_target_id ON jimu_report_permission(target_id)",
            "CREATE UNIQUE INDEX uk_report_perm ON jimu_report_permission(report_id, target_type, target_id, permission_type)",
            "CREATE INDEX idx_al_user_id ON jimu_audit_log(user_id)",
            "CREATE INDEX idx_al_tenant_id ON jimu_audit_log(tenant_id)",
            "CREATE INDEX idx_al_create_time ON jimu_audit_log(create_time)",
            "CREATE INDEX idx_al_module ON jimu_audit_log(module)",
            "CREATE UNIQUE INDEX uk_perm_config_key ON jimu_permission_config(config_key, tenant_id)"
        };

        String[] initData = {
            "INSERT IGNORE INTO jimu_tenant (id, tenant_name, tenant_code, status) VALUES ('1', '默认租户', 'default', 1)",
            "INSERT IGNORE INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin, create_by) VALUES ('1', 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', '1', 1, 1, 'system')",
            "INSERT IGNORE INTO jimu_role (id, role_name, role_code, tenant_id, status, description, sort_order, create_by) VALUES ('1', '超级管理员', 'super_admin', '1', 1, '拥有系统所有权限', 1, 'system'), ('2', '报表设计员', 'report_designer', '1', 1, '可设计和管理报表', 2, 'system'), ('3', '普通用户', 'user', '1', 1, '仅可查看报表', 3, 'system'), ('4', '部门管理员', 'dept_admin', '1', 1, '管理本部门用户和报表', 4, 'system')",
            "INSERT IGNORE INTO jimu_permission (id, perm_code, perm_name, perm_type, parent_id, sort_order, description) VALUES " +
                "('1', 'system', '系统管理', 'menu', NULL, 1, '系统管理菜单'), " +
                "('2', 'user:manage', '用户管理', 'system', '1', 1, '管理用户'), " +
                "('3', 'role:manage', '角色管理', 'system', '1', 2, '管理角色'), " +
                "('4', 'tenant:manage', '租户管理', 'system', '1', 3, '管理租户'), " +
                "('5', 'perm:manage', '权限管理', 'system', '1', 4, '管理权限'), " +
                "('6', 'group:manage', '用户组管理', 'system', '1', 5, '管理用户组'), " +
                "('7', 'audit:view', '审计日志', 'system', '1', 6, '查看审计日志'), " +
                "('8', 'report', '报表管理', 'menu', NULL, 2, '报表管理菜单'), " +
                "('9', 'report:view', '查看报表', 'report', '8', 1, '查看报表'), " +
                "('10', 'report:edit', '编辑报表', 'report', '8', 2, '编辑报表'), " +
                "('11', 'report:delete', '删除报表', 'report', '8', 3, '删除报表'), " +
                "('12', 'report:design', '设计报表', 'report', '8', 4, '设计报表'), " +
                "('13', 'report:export', '导出报表', 'report', '8', 5, '导出报表'), " +
                "('14', 'report:assign', '分配报表', 'report', '8', 6, '分配报表权限'), " +
                "('15', 'report:import', '导入报表', 'report', '8', 7, '导入报表'), " +
                "('16', 'data:filter', '数据过滤', 'data', NULL, 3, '数据权限过滤')",
            "INSERT IGNORE INTO jimu_user_role (id, user_id, role_id, create_by) VALUES ('1', '1', '1', 'system')",
            "INSERT IGNORE INTO jimu_role_permission (id, role_id, perm_id, create_by) VALUES " +
                "('1', '1', '1', 'system'), ('2', '1', '2', 'system'), ('3', '1', '3', 'system'), ('4', '1', '4', 'system'), ('5', '1', '5', 'system'), ('6', '1', '6', 'system'), ('7', '1', '7', 'system'), " +
                "('8', '1', '8', 'system'), ('9', '1', '9', 'system'), ('10', '1', '10', 'system'), ('11', '1', '11', 'system'), ('12', '1', '12', 'system'), ('13', '1', '13', 'system'), ('14', '1', '14', 'system'), ('15', '1', '15', 'system'), ('16', '1', '16', 'system'), " +
                "('17', '2', '8', 'system'), ('18', '2', '9', 'system'), ('19', '2', '10', 'system'), ('20', '2', '12', 'system'), ('21', '2', '13', 'system'), ('22', '2', '14', 'system'), " +
                "('23', '3', '8', 'system'), ('24', '3', '9', 'system'), ('25', '3', '13', 'system'), " +
                "('26', '4', '1', 'system'), ('27', '4', '2', 'system'), ('28', '4', '6', 'system'), ('29', '4', '8', 'system'), ('30', '4', '9', 'system'), ('31', '4', '10', 'system'), ('32', '4', '14', 'system')",
            "INSERT IGNORE INTO jimu_user_group (id, group_name, group_code, group_type, parent_id, tenant_id, description, status, sort_order, create_by) VALUES " +
                "('1', '系统管理部', 'sys_admin', 1, NULL, '1', '系统管理员组', 1, 1, 'system'), " +
                "('2', '财务部', 'finance', 2, NULL, '1', '财务部门用户组', 1, 2, 'system'), " +
                "('3', '销售部', 'sales', 2, NULL, '1', '销售部门用户组', 1, 3, 'system'), " +
                "('4', '人力资源部', 'hr', 2, NULL, '1', '人力资源部门用户组', 1, 4, 'system')",
            "INSERT IGNORE INTO jimu_resource (id, resource_name, resource_code, resource_type, parent_id, tenant_id, path, icon, sort_order, status, description, create_by) VALUES " +
                "('1', '报表中心', 'report_center', 1, NULL, '1', '/report', 'report', 1, 1, '报表中心', 'system'), " +
                "('2', '财务报表', 'finance_report', 2, '1', '1', '/report/finance', 'money', 1, 1, '财务报表目录', 'system'), " +
                "('3', '销售报表', 'sales_report', 2, '1', '1', '/report/sales', 'shopping', 2, 1, '销售报表目录', 'system'), " +
                "('4', '人事报表', 'hr_report', 2, '1', '1', '/report/hr', 'team', 3, 1, '人事报表目录', 'system'), " +
                "('5', '系统管理', 'system_manage', 1, NULL, '1', '/system', 'setting', 2, 1, '系统管理', 'system'), " +
                "('6', '用户管理', 'user_manage', 2, '5', '1', '/system/user', 'user', 1, 1, '用户管理', 'system'), " +
                "('7', '角色管理', 'role_manage', 2, '5', '1', '/system/role', 'team', 2, 1, '角色管理', 'system'), " +
                "('8', '权限管理', 'perm_manage', 2, '5', '1', '/system/permission', 'lock', 3, 1, '权限管理', 'system')"
        };

        // 执行创建表语句
        for (String sql : createTables) {
            try {
                jdbcTemplate.execute(sql);
                System.out.println("执行 SQL: " + sql);
            } catch (Exception e) {
                System.err.println("执行 SQL 失败: " + sql);
                e.printStackTrace();
            }
        }

        // 执行创建索引语句
        for (String sql : createIndexes) {
            try {
                jdbcTemplate.execute(sql);
                System.out.println("执行 SQL: " + sql);
            } catch (Exception e) {
                // 检查是否是重复键错误
                boolean isDuplicateKeyError = false;
                Throwable current = e;
                while (current != null) {
                    if (current.getMessage() != null && current.getMessage().toLowerCase().contains("duplicate")) {
                        isDuplicateKeyError = true;
                        break;
                    }
                    current = current.getCause();
                }
                
                // 忽略索引已存在的错误
                if (isDuplicateKeyError) {
                    System.out.println("索引已存在，跳过创建: " + sql);
                } else {
                    System.err.println("执行 SQL 失败: " + sql);
                    e.printStackTrace();
                }
            }
        }

        // 执行初始化数据语句
        for (String sql : initData) {
            try {
                jdbcTemplate.execute(sql);
                System.out.println("执行 SQL: " + sql);
            } catch (Exception e) {
                System.err.println("执行 SQL 失败: " + sql);
                e.printStackTrace();
            }
        }
    }
}
