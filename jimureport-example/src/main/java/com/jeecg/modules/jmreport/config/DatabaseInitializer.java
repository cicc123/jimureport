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
        // 使用 H2 兼容的 SQL 语句创建表结构
        String[] createTables = {
            "CREATE TABLE IF NOT EXISTS jimu_tenant (id VARCHAR(36) NOT NULL PRIMARY KEY, tenant_name VARCHAR(100) NOT NULL, tenant_code VARCHAR(50) NOT NULL, status INT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user (id VARCHAR(36) NOT NULL PRIMARY KEY, username VARCHAR(50) NOT NULL, password VARCHAR(255) NOT NULL, real_name VARCHAR(50), tenant_id VARCHAR(36), status INT DEFAULT 1, is_admin INT DEFAULT 0, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_role (id VARCHAR(36) NOT NULL PRIMARY KEY, role_name VARCHAR(50) NOT NULL, role_code VARCHAR(50) NOT NULL, tenant_id VARCHAR(36), status INT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, perm_code VARCHAR(100) NOT NULL, perm_name VARCHAR(100) NOT NULL, perm_type VARCHAR(20), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_role (id VARCHAR(36) NOT NULL PRIMARY KEY, user_id VARCHAR(36) NOT NULL, role_id VARCHAR(36) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_role_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, role_id VARCHAR(36) NOT NULL, perm_id VARCHAR(36) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_form_permission (id VARCHAR(36) NOT NULL PRIMARY KEY, form_id VARCHAR(36) NOT NULL, user_id VARCHAR(36), role_id VARCHAR(36), tenant_id VARCHAR(36) NOT NULL, perms VARCHAR(255) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_audit_log (id VARCHAR(36) NOT NULL PRIMARY KEY, user_id VARCHAR(36) NOT NULL, username VARCHAR(50) NOT NULL, tenant_id VARCHAR(36), operation VARCHAR(100) NOT NULL, content TEXT NOT NULL, ip VARCHAR(50) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_group (id VARCHAR(36) NOT NULL PRIMARY KEY, group_name VARCHAR(100) NOT NULL, group_code VARCHAR(50) NOT NULL, tenant_id VARCHAR(36), description VARCHAR(255), status INT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS jimu_user_group_user (id VARCHAR(36) NOT NULL PRIMARY KEY, group_id VARCHAR(36) NOT NULL, user_id VARCHAR(36) NOT NULL, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        // 创建索引
        String[] createIndexes = {
            "CREATE UNIQUE INDEX uk_tenant_code ON jimu_tenant(tenant_code)",
            "CREATE UNIQUE INDEX uk_username ON jimu_user(username)",
            "CREATE INDEX idx_tenant_id ON jimu_user(tenant_id)",
            "CREATE UNIQUE INDEX uk_role_code ON jimu_role(role_code)",
            "CREATE INDEX idx_role_tenant_id ON jimu_role(tenant_id)",
            "CREATE UNIQUE INDEX uk_perm_code ON jimu_permission(perm_code)",
            "CREATE UNIQUE INDEX uk_user_role ON jimu_user_role(user_id, role_id)",
            "CREATE INDEX idx_user_id ON jimu_user_role(user_id)",
            "CREATE INDEX idx_role_id ON jimu_user_role(role_id)",
            "CREATE UNIQUE INDEX uk_role_perm ON jimu_role_permission(role_id, perm_id)",
            "CREATE INDEX idx_rp_role_id ON jimu_role_permission(role_id)",
            "CREATE INDEX idx_perm_id ON jimu_role_permission(perm_id)",
            "CREATE INDEX idx_form_id ON jimu_form_permission(form_id)",
            "CREATE INDEX idx_fp_user_id ON jimu_form_permission(user_id)",
            "CREATE INDEX idx_fp_role_id ON jimu_form_permission(role_id)",
            "CREATE INDEX idx_fp_tenant_id ON jimu_form_permission(tenant_id)",
            "CREATE INDEX idx_al_user_id ON jimu_audit_log(user_id)",
            "CREATE INDEX idx_al_tenant_id ON jimu_audit_log(tenant_id)",
            "CREATE INDEX idx_create_time ON jimu_audit_log(create_time)",
            "CREATE UNIQUE INDEX uk_group_code ON jimu_user_group(group_code)",
            "CREATE INDEX idx_group_tenant_id ON jimu_user_group(tenant_id)",
            "CREATE UNIQUE INDEX uk_group_user ON jimu_user_group_user(group_id, user_id)",
            "CREATE INDEX idx_gu_group_id ON jimu_user_group_user(group_id)",
            "CREATE INDEX idx_gu_user_id ON jimu_user_group_user(user_id)"
        };

        // 初始化数据
        String[] initData = {
            "INSERT IGNORE INTO jimu_tenant (id, tenant_name, tenant_code, status) VALUES ('1', '默认租户', 'default', 1)",
            "INSERT IGNORE INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin) VALUES ('1', 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', '1', 1, 1)",
            "INSERT IGNORE INTO jimu_role (id, role_name, role_code, tenant_id, status) VALUES ('1', '系统管理员', 'admin', '1', 1), ('2', '普通用户', 'user', '1', 1)",
            "INSERT IGNORE INTO jimu_permission (id, perm_code, perm_name, perm_type) VALUES ('1', 'form:view', '查看表单', 'form'), ('2', 'form:edit', '编辑表单', 'form'), ('3', 'form:delete', '删除表单', 'form'), ('4', 'form:design', '设计表单', 'form'), ('5', 'user:manage', '用户管理', 'system'), ('6', 'role:manage', '角色管理', 'system'), ('7', 'tenant:manage', '租户管理', 'system'), ('8', 'form:assign', '分配表单', 'form')",
            "INSERT IGNORE INTO jimu_user_role (id, user_id, role_id) VALUES ('1', '1', '1')",
            "INSERT IGNORE INTO jimu_role_permission (id, role_id, perm_id) VALUES ('1', '1', '1'), ('2', '1', '2'), ('3', '1', '3'), ('4', '1', '4'), ('5', '1', '5'), ('6', '1', '6'), ('7', '1', '7'), ('8', '1', '8'), ('9', '2', '1')"
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
