package com.jimureport.enhancement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jimureport.enhancement.entity.*;
import com.jimureport.enhancement.mapper.*;
import com.jimureport.enhancement.service.JimuFillFormService;
import com.jimureport.enhancement.service.SysPermissionService;
import com.jimureport.enhancement.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户管理集成测试
 * 使用 H2 内存数据库，真实调用 Service → Mapper → DB 全链路
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserIntegrationTest {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private JimuFillFormService fillFormService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 用户查询 ====================

    @Test
    @Order(1)
    @DisplayName("集成-TC-I001: 查询预置管理员用户")
    void testFindAdmin() {
        SysUser admin = userMapper.selectById("1");
        assertNotNull(admin, "管理员用户应存在");
        assertEquals("admin", admin.getUsername());
        assertEquals("0", admin.getStatus());
        assertEquals("0", admin.getDelFlag());
    }

    @Test
    @Order(2)
    @DisplayName("集成-TC-I002: 查询普通用户")
    void testFindTestUser() {
        SysUser user = userMapper.selectById("2");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("1", user.getDeptId());
    }

    @Test
    @Order(3)
    @DisplayName("集成-TC-I003: 查询已删除用户 - 应返回但 delFlag=2")
    void testFindDeletedUser() {
        SysUser deleted = userMapper.selectById("4");
        assertNotNull(deleted);
        assertEquals("2", deleted.getDelFlag());
    }

    // ==================== 用户注册 ====================

    @Test
    @Order(10)
    @DisplayName("集成-TC-I010: 注册新用户成功")
    void testRegister_Success() {
        SysUser newUser = new SysUser();
        newUser.setUsername("integration_test_user");
        newUser.setPassword("Test@12345");
        newUser.setRealName("集成测试用户");
        newUser.setEmail("integration@test.com");

        SysUser result = userService.register(newUser);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("integration_test_user", result.getUsername());
        // 密码应已加密
        assertNotEquals("Test@12345", result.getPassword());
        assertTrue(passwordEncoder.matches("Test@12345", result.getPassword()));
    }

    @Test
    @Order(11)
    @DisplayName("集成-TC-I011: 注册重复用户名 - 应抛异常")
    void testRegister_DuplicateUsername() {
        SysUser newUser = new SysUser();
        newUser.setUsername("admin");
        newUser.setPassword("Test@12345");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(newUser));
        assertTrue(ex.getMessage().contains("已存在"));
    }

    // ==================== 用户分页 ====================

    @Test
    @Order(20)
    @DisplayName("集成-TC-I020: 分页查询用户列表")
    void testUserPage() {
        IPage<SysUser> page = userService.getUserPage(1, 10, null, null, null);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 2, "至少有 admin 和 testuser");
        assertTrue(page.getRecords().stream().anyMatch(u -> "admin".equals(u.getUsername())));
    }

    @Test
    @Order(21)
    @DisplayName("集成-TC-I021: 按关键词搜索用户")
    void testUserPage_ByKeyword() {
        IPage<SysUser> page = userService.getUserPage(1, 10, "admin", null, null);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
        assertTrue(page.getRecords().stream().allMatch(
                u -> u.getUsername().contains("admin")
                        || (u.getRealName() != null && u.getRealName().contains("admin"))
        ));
    }

    @Test
    @Order(22)
    @DisplayName("集成-TC-I022: 按状态筛选 - 只返回正常用户")
    void testUserPage_ByStatus() {
        IPage<SysUser> page = userService.getUserPage(1, 10, null, "0", null);

        assertNotNull(page);
        assertTrue(page.getRecords().stream().allMatch(u -> "0".equals(u.getStatus())));
    }

    @Test
    @Order(23)
    @DisplayName("集成-TC-I023: 按部门筛选")
    void testUserPage_ByDept() {
        IPage<SysUser> page = userService.getUserPage(1, 10, null, null, "1");

        assertNotNull(page);
        assertTrue(page.getRecords().stream().allMatch(u -> "1".equals(u.getDeptId())));
    }

    // ==================== 用户修改 ====================

    @Test
    @Order(30)
    @DisplayName("集成-TC-I030: 修改用户信息")
    void testUpdateUser() {
        SysUser update = new SysUser();
        update.setId("2");
        update.setRealName("修改后的姓名");
        update.setEmail("updated@test.com");

        SysUser result = userService.updateUser(update);

        assertNotNull(result);
        assertEquals("修改后的姓名", result.getRealName());
        assertEquals("updated@test.com", result.getEmail());
        // 密码不应被修改
        assertNotNull(result.getPassword());
    }

    @Test
    @Order(31)
    @DisplayName("集成-TC-I031: 修改不存在的用户")
    void testUpdateUser_NotFound() {
        SysUser update = new SysUser();
        update.setId("nonexist-id");

        assertThrows(RuntimeException.class, () -> userService.updateUser(update));
    }

    // ==================== 密码管理 ====================

    @Test
    @Order(40)
    @DisplayName("集成-TC-I040: 修改密码 - 旧密码正确")
    void testChangePassword_Success() {
        // 注册一个用户用于密码测试
        SysUser newUser = new SysUser();
        newUser.setUsername("pwtest_user");
        newUser.setPassword("OldPass@123");
        SysUser saved = userService.register(newUser);

        boolean result = userService.changePassword(saved.getId(), "OldPass@123", "NewPass@456");
        assertTrue(result);

        // 验证新密码可用
        SysUser updated = userMapper.selectById(saved.getId());
        assertTrue(passwordEncoder.matches("NewPass@456", updated.getPassword()));
    }

    @Test
    @Order(41)
    @DisplayName("集成-TC-I041: 修改密码 - 旧密码错误")
    void testChangePassword_WrongOld() {
        assertThrows(RuntimeException.class,
                () -> userService.changePassword("1", "WrongOldPass", "NewPass"));
    }

    @Test
    @Order(42)
    @DisplayName("集成-TC-I042: 重置密码")
    void testResetPassword() {
        SysUser newUser = new SysUser();
        newUser.setUsername("resetpw_user");
        newUser.setPassword("Original@123");
        SysUser saved = userService.register(newUser);

        boolean result = userService.resetPassword(saved.getId(), "Reset@789");
        assertTrue(result);

        SysUser updated = userMapper.selectById(saved.getId());
        assertTrue(passwordEncoder.matches("Reset@789", updated.getPassword()));
    }

    // ==================== 用户状态 ====================

    @Test
    @Order(50)
    @DisplayName("集成-TC-I050: 停用用户")
    void testDisableUser() {
        SysUser newUser = new SysUser();
        newUser.setUsername("disable_test");
        newUser.setPassword("Test@123");
        SysUser saved = userService.register(newUser);

        boolean result = userService.changeUserStatus(saved.getId(), "1");
        assertTrue(result);

        SysUser updated = userMapper.selectById(saved.getId());
        assertEquals("1", updated.getStatus());
    }

    @Test
    @Order(51)
    @DisplayName("集成-TC-I051: 启用用户")
    void testEnableUser() {
        boolean result = userService.changeUserStatus("3", "0");
        assertTrue(result);

        SysUser updated = userMapper.selectById("3");
        assertEquals("0", updated.getStatus());
    }

    // ==================== 用户删除 ====================

    @Test
    @Order(60)
    @DisplayName("集成-TC-I060: 逻辑删除用户")
    void testDeleteUser() {
        SysUser newUser = new SysUser();
        newUser.setUsername("delete_test_" + System.currentTimeMillis());
        newUser.setPassword("Test@123");
        SysUser saved = userService.register(newUser);

        boolean result = userService.deleteUser(saved.getId());
        assertTrue(result);

        SysUser deleted = userMapper.selectById(saved.getId());
        assertNotNull(deleted, "逻辑删除后用户记录仍应存在");
        assertEquals("2", deleted.getDelFlag());
    }

    // ==================== 角色分配 ====================

    @Test
    @Order(70)
    @DisplayName("集成-TC-I070: 分配用户角色")
    void testAssignRoles() {
        SysUser newUser = new SysUser();
        newUser.setUsername("role_test");
        newUser.setPassword("Test@123");
        SysUser saved = userService.register(newUser);

        boolean result = userService.assignUserRoles(saved.getId(), Arrays.asList("2", "3"));
        assertTrue(result);
    }

    // ==================== 权限查询 ====================

    @Test
    @Order(80)
    @DisplayName("集成-TC-I080: 管理员权限 - 应包含 *:*:*")
    void testAdminPermissions() {
        Set<String> perms = permissionService.getUserPermissions("1");
        assertNotNull(perms);
        assertTrue(perms.contains("*:*:*"), "管理员应有通配符权限");
    }

    @Test
    @Order(81)
    @DisplayName("集成-TC-I081: 普通用户权限 - 应包含菜单权限")
    void testUserPermissions() {
        Set<String> perms = permissionService.getUserPermissions("2");
        assertNotNull(perms);
        assertFalse(perms.isEmpty(), "普通用户应有分配的菜单权限");
        assertTrue(perms.contains("system:user:list"));
    }

    @Test
    @Order(82)
    @DisplayName("集成-TC-I082: 管理员 hasPermission 检查")
    void testAdminHasPermission() {
        assertTrue(permissionService.hasPermission("1", "anything:any:perm"));
    }

    @Test
    @Order(83)
    @DisplayName("集成-TC-I083: 普通用户检查已有权限")
    void testUserHasPermission() {
        assertTrue(permissionService.hasPermission("2", "system:user:list"));
    }

    @Test
    @Order(84)
    @DisplayName("集成-TC-I084: 普通用户检查未分配权限")
    void testUserNoPermission() {
        assertFalse(permissionService.hasPermission("2", "system:config:edit"));
    }

    @Test
    @Order(85)
    @DisplayName("集成-TC-I085: 菜单树查询")
    void testMenuTree() {
        List<SysMenu> tree = permissionService.getMenuTree();
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
    }

    // ==================== 报表权限 ====================

    @Test
    @Order(90)
    @DisplayName("集成-TC-I090: 查询报表权限列表")
    void testReportPermissions() {
        List<JimuReportPermission> perms = permissionService.getReportPermissions("report-001");
        assertNotNull(perms);
        assertFalse(perms.isEmpty());
    }

    @Test
    @Order(91)
    @DisplayName("集成-TC-I091: 检查报表访问权限 - 有权限")
    void testCanAccessReport_WithPerm() {
        assertTrue(permissionService.canAccessReport("2", "report-001", "view"));
    }

    @Test
    @Order(92)
    @DisplayName("集成-TC-I092: 检查报表访问权限 - 无权限")
    void testCanAccessReport_NoPerm() {
        assertFalse(permissionService.canAccessReport("2", "report-001", "delete"));
    }

    @Test
    @Order(93)
    @DisplayName("集成-TC-I093: 管理员报表访问 - 全部通过")
    void testCanAccessReport_Admin() {
        assertTrue(permissionService.canAccessReport("1", "report-001", "view"));
        assertTrue(permissionService.canAccessReport("1", "report-001", "design"));
        assertTrue(permissionService.canAccessReport("1", "report-001", "delete"));
    }

    // ==================== 数据权限 ====================

    @Test
    @Order(100)
    @DisplayName("集成-TC-I100: 管理员数据权限 - 无限制")
    void testDataPermission_Admin() {
        String condition = permissionService.getDataPermissionCondition("1", "report-001", "sys_user");
        assertNull(condition, "管理员应无数据限制");
    }

    @Test
    @Order(101)
    @DisplayName("集成-TC-I101: 普通用户数据权限 - 有配置")
    void testDataPermission_UserWithConfig() {
        String condition = permissionService.getDataPermissionCondition("2", "report-001", "sys_user");
        assertNotNull(condition);
        assertTrue(condition.contains("dept_id"));
    }

    // ==================== 填报功能 ====================

    @Test
    @Order(110)
    @DisplayName("集成-TC-I110: 获取表单配置")
    void testGetFormConfig() {
        JimuFillFormConfig config = fillFormService.getFormConfig("report-001");
        assertNotNull(config);
        assertEquals("测试表单", config.getFormName());
    }

    @Test
    @Order(111)
    @DisplayName("集成-TC-I111: 获取表单字段")
    void testGetFormFields() {
        JimuFillFormConfig config = fillFormService.getFormConfig("report-001");
        assertNotNull(config);

        List<JimuFillFormField> fields = fillFormService.getFormFields(config.getId());
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
    }

    @Test
    @Order(112)
    @DisplayName("集成-TC-I112: 提交填报数据 - 成功")
    void testSubmitFormData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "集成测试用户");
        data.put("age", 30);
        data.put("email", "integration@submit.com");

        JimuFillSubmitRecord record = fillFormService.submitFormData("form-001", "2", data);

        assertNotNull(record);
        assertEquals("success", record.getSubmitStatus());
        assertEquals("form-001", record.getFormId());
    }

    @Test
    @Order(113)
    @DisplayName("集成-TC-I113: 提交到不存在的表单")
    void testSubmitForm_NotFound() {
        assertThrows(RuntimeException.class,
                () -> fillFormService.submitFormData("nonexist-form", "2", new HashMap<>()));
    }

    @Test
    @Order(114)
    @DisplayName("集成-TC-I114: 提交到已停用表单")
    void testSubmitForm_Disabled() {
        assertThrows(RuntimeException.class,
                () -> fillFormService.submitFormData("form-002", "2", new HashMap<>()));
    }

    @Test
    @Order(115)
    @DisplayName("集成-TC-I115: 必填字段校验失败")
    void testSubmitForm_RequiredValidation() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "");
        data.put("age", 25);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fillFormService.submitFormData("form-001", "2", data));
        assertTrue(ex.getMessage().contains("不能为空"));
    }

    @Test
    @Order(116)
    @DisplayName("集成-TC-I116: 数值范围校验失败")
    void testSubmitForm_RangeValidation() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "张三");
        data.put("age", 150);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> fillFormService.submitFormData("form-001", "2", data));
        assertTrue(ex.getMessage().contains("不能大于"));
    }

    @Test
    @Order(117)
    @DisplayName("集成-TC-I117: 查询提交记录")
    void testGetSubmitRecords() {
        IPage<JimuFillSubmitRecord> page = fillFormService.getSubmitRecords(1, 10, "form-001", null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 1);
    }

    @Test
    @Order(118)
    @DisplayName("集成-TC-I118: 导出填报数据")
    void testExportFormData() {
        List<Map<String, Object>> data = fillFormService.exportFormData("form-001");
        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    @Order(119)
    @DisplayName("集成-TC-I119: 保存草稿")
    void testSaveDraft() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "草稿用户");
        data.put("age", 22);

        JimuFillDraft draft = fillFormService.saveDraft("form-001", "1", data);

        assertNotNull(draft);
        assertEquals("form-001", draft.getFormId());
        assertEquals("1", draft.getUserId());
    }

    @Test
    @Order(120)
    @DisplayName("集成-TC-I120: 获取草稿")
    void testGetDraft() {
        // 先保存草稿，确保数据存在
        Map<String, Object> data = new HashMap<>();
        data.put("name", "获取草稿测试");
        data.put("age", 22);
        fillFormService.saveDraft("form-001", "2", data);

        JimuFillDraft draft = fillFormService.getDraft("form-001", "2");
        assertNotNull(draft);
        assertEquals("form-001", draft.getFormId());
        assertEquals("2", draft.getUserId());
    }

    @Test
    @Order(121)
    @DisplayName("集成-TC-I121: 删除草稿")
    void testDeleteDraft() {
        boolean result = fillFormService.deleteDraft("form-001", "1");
        assertTrue(result);
    }
}
