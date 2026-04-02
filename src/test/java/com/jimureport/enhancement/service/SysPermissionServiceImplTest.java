package com.jimureport.enhancement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.*;
import com.jimureport.enhancement.mapper.*;
import com.jimureport.enhancement.service.impl.SysPermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysPermissionServiceImplTest {

    @Mock private SysRoleMapper roleMapper;
    @Mock private SysRoleMenuMapper roleMenuMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private SysMenuMapper menuMapper;
    @Mock private JimuReportPermissionMapper reportPermissionMapper;
    @Mock private JimuDataPermissionMapper dataPermissionMapper;

    @InjectMocks
    private SysPermissionServiceImpl permissionService;

    private SysRole testRole;
    private SysMenu testMenu;

    @BeforeEach
    void setUp() {
        testRole = new SysRole();
        testRole.setId("role-001");
        testRole.setRoleName("测试角色");
        testRole.setRoleKey("test_role");
        testRole.setStatus("0");
        testRole.setDelFlag("0");

        testMenu = new SysMenu();
        testMenu.setId("menu-001");
        testMenu.setMenuName("用户管理");
        testMenu.setParentId("0");
        testMenu.setMenuType("M");
        testMenu.setStatus("0");
        testMenu.setPerms("user:list");
    }

    @Nested
    @DisplayName("角色管理测试")
    class RoleTests {

        @Test
        @DisplayName("TC-P001: 获取角色列表")
        void getRoleList_Success() {
            when(roleMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testRole));

            List<SysRole> result = permissionService.getRoleList();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试角色", result.get(0).getRoleName());
        }

        @Test
        @DisplayName("TC-P002: 新增角色（含菜单分配）")
        void addRole_WithMenus() {
            SysRole newRole = new SysRole();
            newRole.setRoleName("新角色");
            newRole.setRoleKey("new_role");

            when(roleMapper.insert(any(SysRole.class))).thenReturn(1);
            when(roleMenuMapper.insert(any(SysRoleMenu.class))).thenReturn(1);

            SysRole result = permissionService.addRole(newRole, Arrays.asList("menu-001", "menu-002"));

            assertNotNull(result);
            verify(roleMenuMapper, times(2)).insert(any(SysRoleMenu.class));
        }

        @Test
        @DisplayName("TC-P003: 删除角色 - 角色未被使用")
        void deleteRole_Success() {
            when(roleMapper.selectById("role-001")).thenReturn(testRole);
            when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);

            boolean result = permissionService.deleteRole("role-001");

            assertTrue(result);
            // delFlag 应设为 "2"
            verify(roleMapper).updateById(argThat((SysRole r) -> "2".equals(r.getDelFlag())));
            verify(roleMenuMapper).delete(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("TC-P003b: 删除角色 - 角色已被使用")
        void deleteRole_InUse() {
            when(roleMapper.selectById("role-001")).thenReturn(testRole);
            when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> permissionService.deleteRole("role-001"));
            assertTrue(ex.getMessage().contains("已被用户使用"));
        }

        @Test
        @DisplayName("TC-P004: 修改角色（含菜单重新分配）")
        void updateRole_Success() {
            SysRole updatedRole = new SysRole();
            updatedRole.setId("role-001");
            updatedRole.setRoleName("修改后的角色");

            SysRole returnedRole = new SysRole();
            returnedRole.setId("role-001");
            returnedRole.setRoleName("修改后的角色");

            when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);
            when(roleMapper.selectById("role-001")).thenReturn(returnedRole);

            SysRole result = permissionService.updateRole(updatedRole, Arrays.asList("menu-003"));

            assertNotNull(result);
            assertEquals("修改后的角色", result.getRoleName());
            verify(roleMenuMapper).delete(any(LambdaQueryWrapper.class));
            verify(roleMenuMapper).insert(any(SysRoleMenu.class));
        }
    }

    @Nested
    @DisplayName("菜单管理测试")
    class MenuTests {

        @Test
        @DisplayName("TC-P005: 获取菜单树")
        void getMenuTree_Success() {
            when(menuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(testMenu));

            List<SysMenu> result = permissionService.getMenuTree();

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("TC-P006: 获取用户菜单 - 超级管理员返回全部")
        void getUserMenus_Admin() {
            SysRole adminRole = new SysRole();
            adminRole.setId("role-admin");
            adminRole.setRoleKey("admin");

            when(roleMapper.selectRolesByUserId("admin-001")).thenReturn(Collections.singletonList(adminRole));
            when(menuMapper.selectMenuTree()).thenReturn(Collections.singletonList(testMenu));

            List<SysMenu> result = permissionService.getUserMenus("admin-001");

            assertNotNull(result);
            verify(menuMapper).selectMenuTree();
        }

        @Test
        @DisplayName("TC-P007: 获取用户权限集合 - 管理员")
        void getUserPermissions_Admin() {
            SysRole adminRole = new SysRole();
            adminRole.setId("role-admin");
            adminRole.setRoleKey("admin");

            when(roleMapper.selectRolesByUserId("admin-001")).thenReturn(Collections.singletonList(adminRole));

            Set<String> result = permissionService.getUserPermissions("admin-001");

            assertNotNull(result);
            assertTrue(result.contains("*:*:*"));
        }

        @Test
        @DisplayName("TC-P008: 获取用户权限集合 - 普通用户")
        void getUserPermissions_NormalUser() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(menuMapper.selectMenusByRoleId("role-001")).thenReturn(Collections.singletonList(testMenu));

            Set<String> result = permissionService.getUserPermissions("user-001");

            assertNotNull(result);
            assertTrue(result.contains("user:list"));
        }

        @Test
        @DisplayName("TC-P009: 获取角色菜单 ID 列表")
        void getRoleMenuIds_Success() {
            when(roleMenuMapper.selectMenuIdsByRoleId("role-001"))
                    .thenReturn(Arrays.asList("menu-001", "menu-002"));

            List<String> result = permissionService.getRoleMenuIds("role-001");

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("TC-P010: 检查用户有指定权限")
        void hasPermission_WithPerm() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(menuMapper.selectMenusByRoleId("role-001")).thenReturn(Collections.singletonList(testMenu));

            assertTrue(permissionService.hasPermission("user-001", "user:list"));
        }

        @Test
        @DisplayName("TC-P011: 检查用户无指定权限")
        void hasPermission_NoPerm() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(menuMapper.selectMenusByRoleId("role-001")).thenReturn(Collections.singletonList(testMenu));

            assertFalse(permissionService.hasPermission("user-001", "admin:delete"));
        }
    }

    @Nested
    @DisplayName("报表权限测试")
    class ReportPermissionTests {

        @Test
        @DisplayName("TC-P012: 分配报表权限")
        void assignReportPermission_Success() {
            JimuReportPermission perm = new JimuReportPermission();
            perm.setReportId("report-001");
            perm.setPermissionType("view");

            permissionService.assignReportPermission(perm);

            verify(reportPermissionMapper).insert(any(JimuReportPermission.class));
        }

        @Test
        @DisplayName("TC-P013: 查询报表权限列表")
        void getReportPermissions_Success() {
            JimuReportPermission perm = new JimuReportPermission();
            perm.setReportId("report-001");
            perm.setPermissionType("view");

            // 实际调用的是 selectByReportId，不是 selectList
            when(reportPermissionMapper.selectByReportId("report-001"))
                    .thenReturn(Collections.singletonList(perm));

            List<JimuReportPermission> result = permissionService.getReportPermissions("report-001");

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("TC-P014: 移除报表权限")
        void removeReportPermission_Success() {
            permissionService.removeReportPermission("perm-001");

            verify(reportPermissionMapper).deleteById("perm-001");
        }

        @Test
        @DisplayName("TC-P015: 检查报表访问权限 - 管理员直接通过")
        void canAccessReport_Admin() {
            SysRole adminRole = new SysRole();
            adminRole.setId("role-admin");
            adminRole.setRoleKey("admin");

            when(roleMapper.selectRolesByUserId("admin-001")).thenReturn(Collections.singletonList(adminRole));

            assertTrue(permissionService.canAccessReport("admin-001", "report-001", "view"));
        }

        @Test
        @DisplayName("TC-P016: 检查报表访问权限 - 普通用户有权限")
        void canAccessReport_NormalUserWithPerm() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(reportPermissionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            assertTrue(permissionService.canAccessReport("user-001", "report-001", "view"));
        }

        @Test
        @DisplayName("TC-P017: 检查报表访问权限 - 普通用户无权限")
        void canAccessReport_NormalUserNoPerm() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(reportPermissionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            assertFalse(permissionService.canAccessReport("user-001", "report-001", "view"));
        }
    }

    @Nested
    @DisplayName("数据权限测试")
    class DataPermissionTests {

        @Test
        @DisplayName("TC-P018: 获取数据权限条件 - 管理员无限制")
        void getDataPermissionCondition_Admin() {
            SysRole adminRole = new SysRole();
            adminRole.setId("role-admin");
            adminRole.setRoleKey("admin");

            when(roleMapper.selectRolesByUserId("admin-001")).thenReturn(Collections.singletonList(adminRole));

            String result = permissionService.getDataPermissionCondition("admin-001", "report-001", "sys_user");

            assertNull(result); // 管理员无数据限制
        }

        @Test
        @DisplayName("TC-P019: 获取数据权限条件 - 普通用户无配置")
        void getDataPermissionCondition_NoConfig() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(dataPermissionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            String result = permissionService.getDataPermissionCondition("user-001", "report-001", "sys_user");

            assertNull(result); // 无配置时返回 null
        }

        @Test
        @DisplayName("TC-P020: 获取数据权限条件 - 有用户级权限")
        void getDataPermissionCondition_WithUserPerm() {
            SysRole userRole = new SysRole();
            userRole.setId("role-001");
            userRole.setRoleKey("user");

            JimuDataPermission userPerm = new JimuDataPermission();
            userPerm.setConditionField("dept_id");
            userPerm.setConditionType("eq");
            userPerm.setConditionValue("10");
            userPerm.setValueType("static");

            when(roleMapper.selectRolesByUserId("user-001")).thenReturn(Collections.singletonList(userRole));
            when(dataPermissionMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(userPerm));

            String result = permissionService.getDataPermissionCondition("user-001", "report-001", "sys_user");

            assertNotNull(result);
            assertTrue(result.contains("dept_id"));
        }
    }
}
