package com.jimureport.enhancement.controller;

import com.jimureport.enhancement.entity.JimuReportPermission;
import com.jimureport.enhancement.entity.SysMenu;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理", description = "权限管理相关接口")
@RestController
@RequestMapping("/api/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @Operation(summary = "获取当前用户权限")
    @GetMapping("/current/permissions")
    public Set<String> getCurrentUserPermissions(@RequestAttribute String userId) {
        return permissionService.getUserPermissions(userId);
    }

    @Operation(summary = "获取当前用户角色")
    @GetMapping("/current/roles")
    public List<SysRole> getCurrentUserRoles(@RequestAttribute String userId) {
        return permissionService.getUserRoles(userId);
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/current/menus")
    public List<SysMenu> getCurrentUserMenus(@RequestAttribute String userId) {
        return permissionService.getUserMenus(userId);
    }

    @Operation(summary = "检查报表访问权限")
    @GetMapping("/report/check")
    public boolean checkReportPermission(
            @RequestAttribute String userId,
            @RequestParam String reportId,
            @RequestParam String permissionType) {
        return permissionService.canAccessReport(userId, reportId, permissionType);
    }

    @Operation(summary = "获取角色列表")
    @GetMapping("/roles")
    @SaCheckPermission("system:role:list")
    public List<SysRole> getRoleList() {
        return permissionService.getRoleList();
    }

    @Operation(summary = "新增角色")
    @PostMapping("/roles")
    @SaCheckPermission("system:role:add")
    public SysRole addRole(@RequestBody Map<String, Object> params) {
        SysRole role = new SysRole();
        role.setRoleName((String) params.get("roleName"));
        role.setRoleKey((String) params.get("roleKey"));
        role.setRoleSort((Integer) params.get("roleSort"));
        role.setDataScope((String) params.get("dataScope"));
        role.setRemark((String) params.get("remark"));

        @SuppressWarnings("unchecked")
        List<String> menuIds = (List<String>) params.get("menuIds");

        return permissionService.addRole(role, menuIds);
    }

    @Operation(summary = "修改角色")
    @PutMapping("/roles")
    @SaCheckPermission("system:role:edit")
    public SysRole updateRole(@RequestBody Map<String, Object> params) {
        SysRole role = new SysRole();
        role.setId((String) params.get("id"));
        role.setRoleName((String) params.get("roleName"));
        role.setRoleKey((String) params.get("roleKey"));
        role.setRoleSort((Integer) params.get("roleSort"));
        role.setDataScope((String) params.get("dataScope"));
        role.setRemark((String) params.get("remark"));

        @SuppressWarnings("unchecked")
        List<String> menuIds = (List<String>) params.get("menuIds");

        return permissionService.updateRole(role, menuIds);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/roles/{id}")
    @SaCheckPermission("system:role:remove")
    public boolean deleteRole(@PathVariable String id) {
        return permissionService.deleteRole(id);
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/roles/{id}/menus")
    @SaCheckPermission("system:role:query")
    public List<String> getRoleMenuIds(@PathVariable String id) {
        return permissionService.getRoleMenuIds(id);
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/menus/tree")
    public List<SysMenu> getMenuTree() {
        return permissionService.getMenuTree();
    }

    @Operation(summary = "分配报表权限")
    @PostMapping("/reports")
    @SaCheckPermission("report:permission:assign")
    public void assignReportPermission(@RequestBody JimuReportPermission permission) {
        permissionService.assignReportPermission(permission);
    }

    @Operation(summary = "移除报表权限")
    @DeleteMapping("/reports/{id}")
    @SaCheckPermission("report:permission:remove")
    public void removeReportPermission(@PathVariable String id) {
        permissionService.removeReportPermission(id);
    }

    @Operation(summary = "获取报表权限列表")
    @GetMapping("/reports/{reportId}")
    @SaCheckPermission("report:permission:list")
    public List<JimuReportPermission> getReportPermissions(@PathVariable String reportId) {
        return permissionService.getReportPermissions(reportId);
    }
}
