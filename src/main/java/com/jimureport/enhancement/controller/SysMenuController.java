package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.SysMenu;
import com.jimureport.enhancement.mapper.SysMenuMapper;
import com.jimureport.enhancement.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuMapper menuMapper;
    private final SysPermissionService permissionService;

    @Operation(summary = "获取菜单列表")
    @GetMapping("/list")
    @SaCheckPermission("system:menu:list")
    public List<SysMenu> getMenuList() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, "0")
                        .orderByAsc(SysMenu::getOrderNum)
        );
        return buildMenuTree(allMenus, "0");
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public List<SysMenu> getMenuTree() {
        return permissionService.getMenuTree();
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:menu:query")
    public SysMenu getMenuById(@PathVariable String id) {
        return menuMapper.selectById(id);
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @SaCheckPermission("system:menu:add")
    public SysMenu addMenu(@RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return menu;
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @SaCheckPermission("system:menu:edit")
    public SysMenu updateMenu(@RequestBody SysMenu menu) {
        menuMapper.updateById(menu);
        return menuMapper.selectById(menu.getId());
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:remove")
    public boolean deleteMenu(@PathVariable String id) {
        return menuMapper.deleteById(id) > 0;
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, String parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }
}
