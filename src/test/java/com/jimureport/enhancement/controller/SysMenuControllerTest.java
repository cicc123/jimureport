package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.SysMenu;
import com.jimureport.enhancement.mapper.SysMenuMapper;
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
 * SysMenuController 逻辑测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysMenuControllerTest {

    @Mock
    private SysMenuMapper menuMapper;

    @Mock
    private SysPermissionService permissionService;

    @InjectMocks
    private SysMenuController controller;

    private SysMenu createMenu(String id, String name, String parentId, String type, String perms) {
        SysMenu menu = new SysMenu();
        menu.setId(id);
        menu.setMenuName(name);
        menu.setParentId(parentId);
        menu.setMenuType(type);
        menu.setPerms(perms);
        menu.setStatus("0");
        menu.setOrderNum(1);
        return menu;
    }

    @Nested
    @DisplayName("菜单查询")
    class QueryTests {

        @Test
        @DisplayName("TC-MC001: 获取菜单树")
        void getMenuTree_Success() {
            SysMenu dir = createMenu("1", "系统管理", "0", "M", null);
            SysMenu item = createMenu("2", "用户管理", "1", "C", "system:user:list");
            when(permissionService.getMenuTree()).thenReturn(Arrays.asList(dir, item));

            List<SysMenu> result = controller.getMenuTree();

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("TC-MC002: 获取菜单列表（含树构建）")
        void getMenuList_Success() {
            SysMenu dir = createMenu("1", "系统管理", "0", "M", null);
            SysMenu item = createMenu("2", "用户管理", "1", "C", "system:user:list");
            when(menuMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(dir, item));

            List<SysMenu> result = controller.getMenuList();

            assertNotNull(result);
            assertFalse(result.isEmpty());
            // 根节点 "0" 应包含子菜单
            assertEquals("系统管理", result.get(0).getMenuName());
            assertNotNull(result.get(0).getChildren());
        }

        @Test
        @DisplayName("TC-MC003: 获取菜单详情")
        void getMenuById_Success() {
            SysMenu menu = createMenu("1", "系统管理", "0", "M", null);
            when(menuMapper.selectById("1")).thenReturn(menu);

            SysMenu result = controller.getMenuById("1");

            assertNotNull(result);
            assertEquals("系统管理", result.getMenuName());
        }

        @Test
        @DisplayName("TC-MC004: 获取不存在的菜单")
        void getMenuById_NotFound() {
            when(menuMapper.selectById("nonexist")).thenReturn(null);

            SysMenu result = controller.getMenuById("nonexist");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("菜单增删改")
    class CrudTests {

        @Test
        @DisplayName("TC-MC005: 新增菜单")
        void addMenu_Success() {
            SysMenu menu = createMenu(null, "新菜单", "0", "C", "new:menu");
            when(menuMapper.insert(any(SysMenu.class))).thenReturn(1);

            SysMenu result = controller.addMenu(menu);

            assertNotNull(result);
            verify(menuMapper).insert(any(SysMenu.class));
        }

        @Test
        @DisplayName("TC-MC006: 修改菜单")
        void updateMenu_Success() {
            SysMenu menu = createMenu("1", "修改后", "0", "C", "modified:menu");
            SysMenu updated = createMenu("1", "修改后", "0", "C", "modified:menu");

            when(menuMapper.updateById(any(SysMenu.class))).thenReturn(1);
            when(menuMapper.selectById("1")).thenReturn(updated);

            SysMenu result = controller.updateMenu(menu);

            assertNotNull(result);
            assertEquals("修改后", result.getMenuName());
        }

        @Test
        @DisplayName("TC-MC007: 删除菜单")
        void deleteMenu_Success() {
            when(menuMapper.deleteById("1")).thenReturn(1);

            boolean result = controller.deleteMenu("1");

            assertTrue(result);
        }

        @Test
        @DisplayName("TC-MC008: 删除不存在的菜单")
        void deleteMenu_NotFound() {
            when(menuMapper.deleteById("nonexist")).thenReturn(0);

            boolean result = controller.deleteMenu("nonexist");

            assertFalse(result);
        }
    }
}
