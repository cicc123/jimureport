package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.mapper.SysRoleMapper;
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
 * SysRoleController 逻辑测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysRoleControllerTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysPermissionService permissionService;

    @InjectMocks
    private SysRoleController controller;

    private SysRole createTestRole(String id, String name, String key) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleName(name);
        role.setRoleKey(key);
        role.setRoleSort(1);
        role.setStatus("0");
        role.setDelFlag("0");
        return role;
    }

    @Nested
    @DisplayName("角色查询")
    class QueryTests {

        @Test
        @DisplayName("TC-RC001: 获取角色列表")
        void getRoleList_Success() {
            List<SysRole> roles = Arrays.asList(
                    createTestRole("1", "管理员", "admin"),
                    createTestRole("2", "用户", "user")
            );
            when(roleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(roles);

            List<SysRole> result = controller.getRoleList();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("管理员", result.get(0).getRoleName());
        }

        @Test
        @DisplayName("TC-RC002: 角色列表为空")
        void getRoleList_Empty() {
            when(roleMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            List<SysRole> result = controller.getRoleList();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("TC-RC003: 获取角色详情")
        void getRoleById_Success() {
            SysRole role = createTestRole("1", "管理员", "admin");
            when(roleMapper.selectById("1")).thenReturn(role);

            SysRole result = controller.getRoleById("1");

            assertNotNull(result);
            assertEquals("管理员", result.getRoleName());
        }

        @Test
        @DisplayName("TC-RC004: 获取不存在的角色")
        void getRoleById_NotFound() {
            when(roleMapper.selectById("nonexist")).thenReturn(null);

            SysRole result = controller.getRoleById("nonexist");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("角色增删改")
    class CrudTests {

        @Test
        @DisplayName("TC-RC005: 新增角色")
        void addRole_Success() {
            SysRole role = createTestRole(null, "新角色", "new_role");
            when(roleMapper.insert(any(SysRole.class))).thenReturn(1);

            SysRole result = controller.addRole(role);

            assertNotNull(result);
            verify(roleMapper).insert(any(SysRole.class));
        }

        @Test
        @DisplayName("TC-RC006: 修改角色")
        void updateRole_Success() {
            SysRole role = createTestRole("1", "修改后", "modified");
            SysRole updated = createTestRole("1", "修改后", "modified");

            when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);
            when(roleMapper.selectById("1")).thenReturn(updated);

            SysRole result = controller.updateRole(role);

            assertNotNull(result);
            assertEquals("修改后", result.getRoleName());
        }

        @Test
        @DisplayName("TC-RC007: 删除存在的角色")
        void deleteRole_Success() {
            SysRole role = createTestRole("1", "管理员", "admin");
            when(roleMapper.selectById("1")).thenReturn(role);
            when(roleMapper.updateById(any(SysRole.class))).thenReturn(1);

            boolean result = controller.deleteRole("1");

            assertTrue(result);
            verify(roleMapper).updateById(argThat((SysRole r) -> "2".equals(r.getDelFlag())));
        }

        @Test
        @DisplayName("TC-RC008: 删除不存在的角色")
        void deleteRole_NotFound() {
            when(roleMapper.selectById("nonexist")).thenReturn(null);

            boolean result = controller.deleteRole("nonexist");

            assertFalse(result);
            verify(roleMapper, never()).updateById(any(SysRole.class));
        }
    }
}
