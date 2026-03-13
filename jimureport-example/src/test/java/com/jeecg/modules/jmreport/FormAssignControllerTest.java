package com.jeecg.modules.jmreport;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.FormAssignService;
import com.jeecg.modules.jmreport.service.PermissionService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormAssignControllerTest {

    @Autowired
    private FormAssignService formAssignService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_FORM_ID = "test-form-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_USER_ID = "test-user-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_ROLE_ID = "test-role-" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    public void setup() {
        cleanupTestData();
        initTestData();
    }

    @AfterEach
    public void cleanup() {
        cleanupTestData();
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.update("DELETE FROM jimu_form_permission WHERE form_id = ?", TEST_FORM_ID);
            jdbcTemplate.update("DELETE FROM jimu_user WHERE id = ?", TEST_USER_ID);
            jdbcTemplate.update("DELETE FROM jimu_role WHERE id = ?", TEST_ROLE_ID);
        } catch (Exception e) {
        }
    }

    private void initTestData() {
        String insertUserSql = "INSERT INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertUserSql, TEST_USER_ID, "testuser_form", "$2a$10$test", "测试用户", "1", 1, 0);

        String insertRoleSql = "INSERT INTO jimu_role (id, role_name, role_code, tenant_id, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertRoleSql, TEST_ROLE_ID, "测试角色", "test_role_form", "1", 1);
    }

    @Test
    @Order(1)
    public void testAssignFormToUser() {
        StpUtil.login("admin");

        formAssignService.assignFormToUser(TEST_FORM_ID, TEST_USER_ID, "view,edit");

        String sql = "SELECT * FROM jimu_form_permission WHERE form_id = ? AND user_id = ?";
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(sql, TEST_FORM_ID, TEST_USER_ID);

        assertFalse(permissions.isEmpty(), "表单权限应该被创建");
        assertEquals("view,edit", permissions.get(0).get("perms").toString(), "权限应该正确保存");

        StpUtil.logout();
    }

    @Test
    @Order(2)
    public void testAssignFormToRole() {
        StpUtil.login("admin");

        formAssignService.assignFormToRole(TEST_FORM_ID, TEST_ROLE_ID, "view");

        String sql = "SELECT * FROM jimu_form_permission WHERE form_id = ? AND role_id = ?";
        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(sql, TEST_FORM_ID, TEST_ROLE_ID);

        assertFalse(permissions.isEmpty(), "表单权限应该被创建");
        assertEquals("view", permissions.get(0).get("perms").toString(), "权限应该正确保存");

        StpUtil.logout();
    }

    @Test
    @Order(3)
    public void testRevokeFormPermission() {
        StpUtil.login("admin");

        formAssignService.assignFormToUser(TEST_FORM_ID, TEST_USER_ID, "view,edit");

        String checkSql = "SELECT COUNT(*) FROM jimu_form_permission WHERE form_id = ? AND user_id = ?";
        Integer countBefore = jdbcTemplate.queryForObject(checkSql, Integer.class, TEST_FORM_ID, TEST_USER_ID);
        assertTrue(countBefore > 0, "权限应该存在");

        formAssignService.revokeFormPermission(TEST_FORM_ID, TEST_USER_ID, null);

        Integer countAfter = jdbcTemplate.queryForObject(checkSql, Integer.class, TEST_FORM_ID, TEST_USER_ID);
        assertEquals(0, countAfter, "权限应该被撤销");

        StpUtil.logout();
    }

    @Test
    @Order(4)
    public void testGetFormAssignments() {
        StpUtil.login("admin");

        formAssignService.assignFormToUser(TEST_FORM_ID, TEST_USER_ID, "view");
        formAssignService.assignFormToRole(TEST_FORM_ID, TEST_ROLE_ID, "edit");

        List<Map<String, Object>> assignments = formAssignService.getFormAssignments(TEST_FORM_ID);

        assertFalse(assignments.isEmpty(), "应该有分配记录");
        assertTrue(assignments.size() >= 2, "应该至少有两条分配记录");

        StpUtil.logout();
    }

    @Test
    @Order(5)
    public void testUpdateExistingPermission() {
        StpUtil.login("admin");

        formAssignService.assignFormToUser(TEST_FORM_ID, TEST_USER_ID, "view");

        formAssignService.assignFormToUser(TEST_FORM_ID, TEST_USER_ID, "view,edit,delete");

        String sql = "SELECT perms FROM jimu_form_permission WHERE form_id = ? AND user_id = ?";
        String perms = jdbcTemplate.queryForObject(sql, String.class, TEST_FORM_ID, TEST_USER_ID);
        assertEquals("view,edit,delete", perms, "权限应该被更新");

        StpUtil.logout();
    }

    @Test
    @Order(6)
    public void testAuditLogForFormOperation() {
        StpUtil.login("admin");

        String operation = "分配表单权限";
        String content = "测试内容";
        String ip = "127.0.0.1";

        auditLogService.logFormOperation(TEST_FORM_ID, operation, content, ip);

        String sql = "SELECT * FROM jimu_audit_log WHERE operation = ? AND content LIKE ? ORDER BY create_time DESC LIMIT 1";
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(sql, operation, "%" + TEST_FORM_ID + "%");

        assertFalse(logs.isEmpty(), "审计日志应该被记录");
        assertTrue(logs.get(0).get("content").toString().contains(TEST_FORM_ID), "日志内容应包含表单ID");

        StpUtil.logout();
    }

    @Test
    @Order(7)
    public void testPermissionCheckForAdmin() {
        StpUtil.login("admin");

        boolean hasPermission = permissionService.hasPermission("form:assign");
        assertTrue(hasPermission, "管理员应该有表单分配权限");

        hasPermission = permissionService.hasPermission("user:manage");
        assertTrue(hasPermission, "管理员应该有用户管理权限");

        StpUtil.logout();
    }

    @Test
    @Order(8)
    public void testBatchAssignToUsers() {
        StpUtil.login("admin");

        String userId2 = "test-user-batch-" + UUID.randomUUID().toString().substring(0, 8);
        String insertUserSql = "INSERT INTO jimu_user (id, username, password, real_name, tenant_id, status, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertUserSql, userId2, "testuser_batch", "$2a$10$test", "批量测试用户", "1", 1, 0);

        List<String> userIds = List.of(TEST_USER_ID, userId2);
        formAssignService.batchAssignFormToUsers(TEST_FORM_ID, userIds, "view");

        String sql = "SELECT COUNT(*) FROM jimu_form_permission WHERE form_id = ? AND user_id IN (?, ?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, TEST_FORM_ID, TEST_USER_ID, userId2);
        assertEquals(2, count, "应该有两条权限记录");

        jdbcTemplate.update("DELETE FROM jimu_user WHERE id = ?", userId2);
        StpUtil.logout();
    }
}
