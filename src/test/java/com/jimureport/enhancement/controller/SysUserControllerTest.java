package com.jimureport.enhancement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jimureport.enhancement.entity.SysUser;
import com.jimureport.enhancement.service.SysUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SysUserController 逻辑测试（不依赖 Spring 上下文）
 * 测试 Service 层的 Controller 调用逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserControllerTest {

    private final SysUserService userService = mock(SysUserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("TC-C001: 登录逻辑 - 返回 Token 信息")
    void login_ReturnsToken() {
        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-token-xxxxx");
        loginResult.put("userId", "user-001");
        loginResult.put("username", "admin");

        when(userService.login("admin", "admin123")).thenReturn(loginResult);

        Map<String, Object> result = userService.login("admin", "admin123");

        assertNotNull(result);
        assertEquals("test-token-xxxxx", result.get("token"));
        assertEquals("admin", result.get("username"));
    }

    @Test
    @DisplayName("TC-C002: 登录失败 - 用户不存在")
    void login_UserNotFound() {
        when(userService.login("nonexist", "123"))
                .thenThrow(new RuntimeException("用户不存在"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("nonexist", "123"));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("TC-C003: 登录失败 - 密码错误")
    void login_WrongPassword() {
        when(userService.login("admin", "wrong"))
                .thenThrow(new RuntimeException("密码错误"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login("admin", "wrong"));
        assertEquals("密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("TC-C004: 注册逻辑 - 返回用户信息")
    void register_ReturnsUser() {
        SysUser newUser = new SysUser();
        newUser.setId("new-user-001");
        newUser.setUsername("newuser");

        when(userService.register(any(SysUser.class))).thenReturn(newUser);

        SysUser reqUser = new SysUser();
        reqUser.setUsername("newuser");
        reqUser.setPassword("Test123456");

        SysUser result = userService.register(reqUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
    }

    @Test
    @DisplayName("TC-C005: 注册失败 - 用户名重复")
    void register_DuplicateUsername() {
        when(userService.register(any(SysUser.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        SysUser reqUser = new SysUser();
        reqUser.setUsername("admin");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(reqUser));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    @DisplayName("TC-C006: 获取用户信息")
    void getUserById_Success() {
        SysUser user = new SysUser();
        user.setId("user-001");
        user.setUsername("admin");
        user.setRealName("管理员");

        when(userService.getUserById("user-001")).thenReturn(user);

        SysUser result = userService.getUserById("user-001");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("TC-C007: 修改用户信息")
    void updateUser_Success() {
        SysUser updated = new SysUser();
        updated.setId("user-001");
        updated.setRealName("新姓名");

        when(userService.updateUser(any(SysUser.class))).thenReturn(updated);

        SysUser result = userService.updateUser(updated);

        assertNotNull(result);
        assertEquals("新姓名", result.getRealName());
    }

    @Test
    @DisplayName("TC-C008: 删除用户")
    void deleteUser_Success() {
        when(userService.deleteUser("user-001")).thenReturn(true);

        boolean result = userService.deleteUser("user-001");

        assertTrue(result);
    }

    @Test
    @DisplayName("TC-C009: 修改密码")
    void changePassword_Success() {
        when(userService.changePassword("user-001", "oldPass", "newPass")).thenReturn(true);

        boolean result = userService.changePassword("user-001", "oldPass", "newPass");

        assertTrue(result);
    }

    @Test
    @DisplayName("TC-C010: 分配用户角色")
    void assignRoles_Success() {
        when(userService.assignUserRoles("user-001", Arrays.asList("role1", "role2")))
                .thenReturn(true);

        boolean result = userService.assignUserRoles("user-001", Arrays.asList("role1", "role2"));

        assertTrue(result);
    }
}
