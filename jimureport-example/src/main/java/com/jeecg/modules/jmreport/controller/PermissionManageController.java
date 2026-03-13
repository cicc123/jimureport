package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.common.Result;
import com.jeecg.modules.jmreport.dto.*;
import com.jeecg.modules.jmreport.entity.DataPermission;
import com.jeecg.modules.jmreport.entity.Resource;
import com.jeecg.modules.jmreport.entity.ResourcePermission;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.PermissionManageService;
import com.jeecg.modules.jmreport.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
public class PermissionManageController {

    @Autowired
    private PermissionManageService permissionManageService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/resource/tree")
    public Result<List<Resource>> getResourceTree() {
        checkPermission("perm:manage");
        String tenantId = getCurrentTenantId();
        List<Resource> tree = permissionManageService.getResourceTree(tenantId);
        return Result.success(tree);
    }

    @GetMapping("/resource/accessible")
    public Result<List<Resource>> getAccessibleResources() {
        String username = getCurrentUsername();
        List<Resource> resources = permissionManageService.getAccessibleResources(username);
        return Result.success(resources);
    }

    @PostMapping("/resource/assign")
    public Result<Void> assignResourcePermission(@RequestBody ResourcePermissionAssignDTO dto, HttpServletRequest req) {
        checkPermission("perm:manage");
        permissionManageService.assignResourcePermission(
                dto.getResourceId(),
                dto.getTargetType(),
                dto.getTargetId(),
                dto.getPermissionBits(),
                req.getRemoteAddr()
        );
        return Result.success();
    }

    @PostMapping("/resource/batchAssign")
    public Result<Void> batchAssignResourcePermission(@RequestBody BatchPermissionAssignDTO dto, HttpServletRequest req) {
        checkPermission("perm:manage");
        permissionManageService.batchAssignResourcePermission(
                dto.getResourceIds(),
                dto.getTargetType(),
                dto.getTargetId(),
                dto.getPermissionBits(),
                req.getRemoteAddr()
        );
        return Result.success();
    }

    @PostMapping("/resource/revoke")
    public Result<Void> revokeResourcePermission(@RequestBody ResourcePermissionRevokeDTO dto, HttpServletRequest req) {
        checkPermission("perm:manage");
        permissionManageService.revokeResourcePermission(
                dto.getResourceId(),
                dto.getTargetType(),
                dto.getTargetId(),
                req.getRemoteAddr()
        );
        return Result.success();
    }

    @GetMapping("/resource/check")
    public Result<Boolean> checkResourcePermission(
            @RequestParam String resourceId,
            @RequestParam int requiredPerm) {
        String username = getCurrentUsername();
        boolean hasPermission = permissionManageService.checkResourcePermission(resourceId, username, requiredPerm);
        return Result.success(hasPermission);
    }

    @GetMapping("/resource/permissionBits")
    public Result<Integer> getUserResourcePermissionBits(@RequestParam String resourceId) {
        String username = getCurrentUsername();
        int bits = permissionManageService.getUserResourcePermissionBits(resourceId, username);
        return Result.success(bits);
    }

    @GetMapping("/resource/permissions")
    public Result<List<Map<String, Object>>> getResourcePermissions(@RequestParam String resourceId) {
        checkPermission("perm:manage");
        List<Map<String, Object>> permissions = permissionManageService.getResourcePermissions(resourceId);
        return Result.success(permissions);
    }

    @PostMapping("/data/set")
    public Result<Void> setDataPermission(@RequestBody DataPermissionDTO dto, HttpServletRequest req) {
        checkPermission("perm:manage");
        permissionManageService.setDataPermission(
                dto.getResourceId(),
                dto.getTargetType(),
                dto.getTargetId(),
                dto.getFilterType(),
                dto.getFilterRule(),
                req.getRemoteAddr()
        );
        return Result.success();
    }

    @GetMapping("/data/filter")
    public Result<String> getDataFilterSql(@RequestParam String resourceId) {
        String username = getCurrentUsername();
        String filterSql = permissionManageService.buildDataFilterSql(resourceId, username);
        return Result.success(filterSql);
    }

    @GetMapping("/data/permissions")
    public Result<List<Map<String, Object>>> getDataPermissions(@RequestParam String resourceId) {
        checkPermission("perm:manage");
        List<Map<String, Object>> permissions = permissionManageService.getDataPermissions(resourceId);
        return Result.success(permissions);
    }

    @GetMapping("/user/permissions")
    public Result<List<String>> getUserPermissions() {
        String username = getCurrentUsername();
        List<String> permissions = permissionService.getUserPermissions(username);
        return Result.success(permissions);
    }

    @GetMapping("/user/hasPermission")
    public Result<Boolean> hasPermission(@RequestParam String permCode) {
        String username = getCurrentUsername();
        boolean hasPermission = permissionService.hasPermission(username, permCode);
        return Result.success(hasPermission);
    }

    private void checkPermission(String permCode) {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("请先登录");
        }
        String username = StpUtil.getLoginIdAsString();
        if (!permissionService.hasPermission(username, permCode)) {
            throw new RuntimeException("无权限执行此操作");
        }
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private String getCurrentTenantId() {
        return "1";
    }
}
