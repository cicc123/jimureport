package com.jimureport.enhancement.controller;

import com.jimureport.enhancement.entity.JimuReportPermission;
import com.jimureport.enhancement.entity.SysMenu;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.service.SysPermissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysPermissionController 逻辑测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysPermissionControllerTest {

    @Mock
    private SysPermissionService permissionService;

    @InjectMocks
    private SysPermissionController controller;

    @Nested
    @DisplayName("当前用户权限查询")
    class CurrentUserTests {

        @Test
        @DisplayName("TC-PC001: 获取当前用户权限")
        void getCurrentUserPermissions_Success() {
            Set<String> perms = new HashSet<>(Arrays.asList("system:user:list", "report:list:list"));
            when(permissionService.getUserPermissions("user-001")).thenReturn(perms);

            Set<String> result = controller.getCurrentUserPermissions("user-001");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("system:user:list"));
        }

        @Test
        @DisplayName("TC-PC002: 获取当前用户角色")
        void getCurrentUserRoles_Success() {
            SysRole role = new SysRole();
            role.setId("2");
            role.setRoleName("普通用户");
            role.setRoleKey("user");
            when(permissionService.getUserRoles("user-001"))
                    .thenReturn(Collections.singletonList(role));

            List<SysRole> result = controller.getCurrentUserRoles("user-001");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("普通用户", result.get(0).getRoleName());
        }

        @Test
        @DisplayName("TC-PC003: 获取当前用户菜单")
        void getCurrentUserMenus_Success() {
            SysMenu menu = new SysMenu();
            menu.setId("1");
            menu.setMenuName("系统管理");
            when(permissionService.getUserMenus("user-001"))
                    .thenReturn(Collections.singletonList(menu));

            List<SysMenu> result = controller.getCurrentUserMenus("user-001");

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("角色管理")
    class RoleTests {

        @Test
        @DisplayName("TC-PC004: 新增角色（含菜单）")
        void addRole_Success() {
            SysRole role = new SysRole();
            role.setId("new-role");
            role.setRoleName("新角色");
            role.setRoleKey("new_role");
            when(permissionService.addRole(any(SysRole.class), anyList())).thenReturn(role);

            Map<String, Object> params = new HashMap<>();
            params.put("roleName", "新角色");
            params.put("roleKey", "new_role");
            params.put("roleSort", 1);
            params.put("menuIds", Arrays.asList("1", "2"));

            SysRole result = controller.addRole(params);

            assertNotNull(result);
            assertEquals("新角色", result.getRoleName());
        }

        @Test
        @DisplayName("TC-PC005: 修改角色")
        void updateRole_Success() {
            SysRole role = new SysRole();
            role.setId("1");
            role.setRoleName("修改后的角色");
            when(permissionService.updateRole(any(SysRole.class), anyList())).thenReturn(role);

            Map<String, Object> params = new HashMap<>();
            params.put("id", "1");
            params.put("roleName", "修改后的角色");
            params.put("roleKey", "modified");
            params.put("menuIds", Arrays.asList("1", "2", "3"));

            SysRole result = controller.updateRole(params);

            assertNotNull(result);
            assertEquals("修改后的角色", result.getRoleName());
        }

        @Test
        @DisplayName("TC-PC006: 删除角色")
        void deleteRole_Success() {
            when(permissionService.deleteRole("1")).thenReturn(true);

            boolean result = controller.deleteRole("1");

            assertTrue(result);
        }

        @Test
        @DisplayName("TC-PC007: 获取角色菜单ID列表")
        void getRoleMenuIds_Success() {
            when(permissionService.getRoleMenuIds("1"))
                    .thenReturn(Arrays.asList("1", "2", "3"));

            List<String> result = controller.getRoleMenuIds("1");

            assertNotNull(result);
            assertEquals(3, result.size());
        }
    }

    @Nested
    @DisplayName("报表权限管理")
    class ReportPermissionTests {

        @Test
        @DisplayName("TC-PC008: 检查报表访问权限")
        void checkReportPermission_Success() {
            when(permissionService.canAccessReport("user-001", "report-001", "view"))
                    .thenReturn(true);

            boolean result = controller.checkReportPermission("user-001", "report-001", "view");

            assertTrue(result);
        }

        @Test
        @DisplayName("TC-PC009: 检查报表访问权限 - 无权限")
        void checkReportPermission_NoAccess() {
            when(permissionService.canAccessReport("user-001", "report-001", "design"))
                    .thenReturn(false);

            boolean result = controller.checkReportPermission("user-001", "report-001", "design");

            assertFalse(result);
        }

        @Test
        @DisplayName("TC-PC010: 分配报表权限")
        void assignReportPermission_Success() {
            JimuReportPermission perm = new JimuReportPermission();
            perm.setReportId("report-001");
            perm.setUserId("user-001");
            perm.setPermissionType("view");

            controller.assignReportPermission(perm);

            verify(permissionService).assignReportPermission(any(JimuReportPermission.class));
        }

        @Test
        @DisplayName("TC-PC011: 获取报表权限列表")
        void getReportPermissions_Success() {
            JimuReportPermission perm = new JimuReportPermission();
            perm.setId("1");
            perm.setReportId("report-001");
            perm.setPermissionType("view");
            when(permissionService.getReportPermissions("report-001"))
                    .thenReturn(Collections.singletonList(perm));

            List<JimuReportPermission> result = controller.getReportPermissions("report-001");

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("TC-PC012: 移除报表权限")
        void removeReportPermission_Success() {
            controller.removeReportPermission("perm-001");

            verify(permissionService).removeReportPermission("perm-001");
        }
    }
}
