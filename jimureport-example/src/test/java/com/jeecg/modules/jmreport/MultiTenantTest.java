package com.jeecg.modules.jmreport;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.FormAssignService;
import com.jeecg.modules.jmreport.service.PermissionService;
import com.jeecg.modules.jmreport.service.UserService;
import com.jeecg.modules.jmreport.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiTenantTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private FormAssignService formAssignService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        // 清除测试数据
        clearTestData();
        // 初始化测试数据
        initTestData();
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM jimu_form_permission WHERE form_id = 'test-form'");
        jdbcTemplate.update("DELETE FROM jimu_user WHERE username IN ('test-user1', 'test-user2')");
        jdbcTemplate.update("DELETE FROM jimu_role WHERE role_code IN ('test-role1', 'test-role2')");
        jdbcTemplate.update("DELETE FROM jimu_tenant WHERE tenant_code = 'test-tenant'");
    }

    private void initTestData() {
        // 创建测试租户
        jdbcTemplate.update("INSERT INTO jimu_tenant (id, tenant_name, tenant_code, status) VALUES ('2', '测试租户', 'test-tenant', 1)");

        // 创建测试用户
        jdbcTemplate.update("INSERT INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin) VALUES ('2', 'test-user1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户1', '1', 1, 0)");
        jdbcTemplate.update("INSERT INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin) VALUES ('3', 'test-user2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '测试用户2', '2', 1, 0)");

        // 创建测试角色
        jdbcTemplate.update("INSERT INTO jimu_role (id, role_name, role_code, tenant_id, status) VALUES ('3', '测试角色1', 'test-role1', '1', 1)");
        jdbcTemplate.update("INSERT INTO jimu_role (id, role_name, role_code, tenant_id, status) VALUES ('4', '测试角色2', 'test-role2', '2', 1)");

        // 关联用户和角色
        jdbcTemplate.update("INSERT INTO jimu_user_role (id, user_id, role_id) VALUES ('2', '2', '3')");
        jdbcTemplate.update("INSERT INTO jimu_user_role (id, user_id, role_id) VALUES ('3', '3', '4')");
    }

    /**
     * 测试多租户隔离性
     */
    @Test
    public void testTenantIsolation() {
        // 测试用户1（租户1）登录
        StpUtil.login("test-user1");
        TenantContext.setTenantId("1");

        // 分配表单权限给用户1
        formAssignService.assignFormToUser("test-form", "2", "view,edit");

        // 切换到用户2（租户2）
        StpUtil.logout();
        StpUtil.login("test-user2");
        TenantContext.setTenantId("2");

        // 验证用户2无法访问用户1的表单权限
        assertFalse(permissionService.hasFormPermission("test-user2", "test-form", "view"), "不同租户间数据应该隔离");

        StpUtil.logout();
    }

    /**
     * 测试权限控制
     */
    @Test
    public void testPermissionControl() {
        // 测试用户1登录
        StpUtil.login("test-user1");
        TenantContext.setTenantId("1");

        // 验证普通用户没有管理员权限
        assertFalse(permissionService.hasPermission("user:manage"), "普通用户不应该有用户管理权限");

        // 分配表单权限给用户1
        formAssignService.assignFormToUser("test-form", "2", "view");

        // 验证用户1有表单查看权限
        assertTrue(permissionService.hasFormPermission("test-form", "view"), "用户应该有表单查看权限");
        // 验证用户1没有表单编辑权限
        assertFalse(permissionService.hasFormPermission("test-form", "edit"), "用户不应该有表单编辑权限");

        StpUtil.logout();

        // 管理员登录
        StpUtil.login("admin");
        TenantContext.setTenantId("1");

        // 验证管理员有所有权限
        assertTrue(permissionService.hasPermission("user:manage"), "管理员应该有用户管理权限");
        assertTrue(permissionService.hasPermission("form:assign"), "管理员应该有表单分配权限");

        StpUtil.logout();
    }

    /**
     * 测试表单分配功能
     */
    @Test
    public void testFormAssignment() {
        // 管理员登录
        StpUtil.login("admin");
        TenantContext.setTenantId("1");

        // 分配表单给用户
        formAssignService.assignFormToUser("test-form", "2", "view,edit");

        // 验证分配成功
        var assignments = formAssignService.getFormAssignments("test-form");
        assertFalse(assignments.isEmpty(), "表单分配记录应该存在");

        // 撤销表单权限
        formAssignService.revokeFormPermission("test-form", "2", null);

        // 验证撤销成功
        assignments = formAssignService.getFormAssignments("test-form");
        assertTrue(assignments.isEmpty(), "表单分配记录应该被撤销");

        StpUtil.logout();
    }

    /**
     * 测试安全渗透（防越权）
     */
    @Test
    public void testSecurity() {
        // 测试用户1登录
        StpUtil.login("test-user1");
        TenantContext.setTenantId("1");

        // 尝试访问管理员权限
        assertFalse(permissionService.hasPermission("user:manage"), "普通用户不应该能访问管理员权限");

        // 尝试访问其他租户的资源
        TenantContext.setTenantId("2");
        assertFalse(permissionService.hasPermission("form:view"), "用户不应该能访问其他租户的资源");

        StpUtil.logout();
    }

    /**
     * 测试审计日志
     */
    @Test
    public void testAuditLog() {
        // 测试用户1登录
        StpUtil.login("test-user1");
        TenantContext.setTenantId("1");

        // 记录审计日志
        auditLogService.log("测试操作", "测试内容", "127.0.0.1");

        // 验证日志记录
        var logs = auditLogService.getAuditLogs(1, 10, "test-user1", "测试操作", null, null);
        assertFalse(logs.isEmpty(), "审计日志应该被记录");

        StpUtil.logout();
    }
}
