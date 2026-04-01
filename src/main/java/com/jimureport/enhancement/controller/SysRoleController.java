package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.mapper.SysRoleMapper;
import com.jimureport.enhancement.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色管理相关接口")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysPermissionService permissionService;

    @Operation(summary = "获取角色列表")
    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public List<SysRole> getRoleList() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDelFlag, "0")
                        .orderByAsc(SysRole::getRoleSort)
        );
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:role:query")
    public SysRole getRoleById(@PathVariable String id) {
        return roleMapper.selectById(id);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("system:role:add")
    public SysRole addRole(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return role;
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @SaCheckPermission("system:role:edit")
    public SysRole updateRole(@RequestBody SysRole role) {
        roleMapper.updateById(role);
        return roleMapper.selectById(role.getId());
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:remove")
    public boolean deleteRole(@PathVariable String id) {
        SysRole role = roleMapper.selectById(id);
        if (role != null) {
            role.setDelFlag("2");
            roleMapper.updateById(role);
            return true;
        }
        return false;
    }
}
