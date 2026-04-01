package com.jimureport.enhancement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.jimureport.enhancement.entity.SysUser;
import com.jimureport.enhancement.service.SysUserService;
import com.jimureport.enhancement.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return userService.login(username, password);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public SysUser register(@RequestBody SysUser user) {
        return userService.register(user);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    @SaCheckPermission("system:user:list")
    public IPage<SysUser> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deptId) {
        return userService.getUserPage(pageNum, pageSize, keyword, status, deptId);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("system:user:query")
    public SysUser getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @SaCheckPermission("system:user:add")
    public SysUser addUser(@RequestBody SysUser user) {
        return userService.addUser(user);
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @SaCheckPermission("system:user:edit")
    public SysUser updateUser(@RequestBody SysUser user) {
        return userService.updateUser(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:remove")
    public boolean deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/status")
    @SaCheckPermission("system:user:edit")
    public boolean changeUserStatus(@RequestBody Map<String, String> params) {
        return userService.changeUserStatus(params.get("id"), params.get("status"));
    }

    @Operation(summary = "分配用户角色")
    @PutMapping("/roles")
    @SaCheckPermission("system:user:edit")
    public boolean assignUserRoles(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        @SuppressWarnings("unchecked")
        List<String> roleIds = (List<String>) params.get("roleIds");
        return userService.assignUserRoles(userId, roleIds);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public SysUser getCurrentUser(@RequestAttribute String userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public SysUser updateProfile(@RequestBody SysUser user, @RequestAttribute String userId) {
        user.setId(userId);
        return userService.updateProfile(user);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public boolean changePassword(@RequestBody Map<String, String> params, @RequestAttribute String userId) {
        return userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
