package com.jimureport.enhancement.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.jimureport.enhancement.vo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 单元测试
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("Sa-Token 异常处理")
    class SaTokenExceptionTests {

        @Test
        @DisplayName("TC-E001: 未登录异常 → 401")
        void handleNotLogin() {
            NotLoginException ex = new NotLoginException("test", "token", "message");

            Result<?> result = handler.handleNotLoginException(ex);

            assertEquals(401, result.getCode());
            assertTrue(result.getMessage().contains("请先登录"));
        }

        @Test
        @DisplayName("TC-E002: 缺少角色异常 → 403")
        void handleNotRole() {
            NotRoleException ex = new NotRoleException("admin", "StpUtil");

            Result<?> result = handler.handleNotRoleException(ex);

            assertEquals(403, result.getCode());
            assertTrue(result.getMessage().contains("角色权限"));
        }

        @Test
        @DisplayName("TC-E003: 缺少权限异常 → 403")
        void handleNotPermission() {
            NotPermissionException ex = new NotPermissionException("user:list", "StpUtil");

            Result<?> result = handler.handleNotPermissionException(ex);

            assertEquals(403, result.getCode());
            assertTrue(result.getMessage().contains("操作权限"));
        }
    }

    @Nested
    @DisplayName("参数校验异常处理")
    class ValidationExceptionTests {

        @Test
        @DisplayName("TC-E004: MethodArgumentNotValidException")
        void handleMethodArgumentNotValid() {
            FieldError fieldError = new FieldError("user", "username", "不能为空");
            org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
            when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            Result<?> result = handler.handleMethodArgumentNotValidException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("username"));
        }

        @Test
        @DisplayName("TC-E005: BindException")
        void handleBindException() {
            FieldError fieldError = new FieldError("form", "email", "格式不正确");
            org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
            when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

            BindException ex = new BindException(bindingResult);

            Result<?> result = handler.handleBindException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("email"));
        }

        @Test
        @DisplayName("TC-E006: ConstraintViolationException")
        void handleConstraintViolation() {
            Set<ConstraintViolation<?>> violations = new HashSet<>();
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("用户名长度必须在4-20之间");
            violations.add(violation);

            ConstraintViolationException ex = new ConstraintViolationException(violations);

            Result<?> result = handler.handleConstraintViolationException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("用户名长度"));
        }
    }

    @Nested
    @DisplayName("业务异常处理")
    class BusinessExceptionTests {

        @Test
        @DisplayName("TC-E007: RuntimeException → 400")
        void handleRuntime() {
            RuntimeException ex = new RuntimeException("用户不存在");

            Result<?> result = handler.handleRuntimeException(ex);

            assertEquals(400, result.getCode());
            assertEquals("用户不存在", result.getMessage());
        }

        @Test
        @DisplayName("TC-E008: IllegalArgumentException → 400")
        void handleIllegalArgument() {
            IllegalArgumentException ex = new IllegalArgumentException("参数错误");

            Result<?> result = handler.handleIllegalArgumentException(ex);

            assertEquals(400, result.getCode());
            assertTrue(result.getMessage().contains("参数错误"));
        }
    }

    @Nested
    @DisplayName("系统异常处理")
    class SystemExceptionTests {

        @Test
        @DisplayName("TC-E009: NullPointerException → 500")
        void handleNullPointer() {
            NullPointerException ex = new NullPointerException();

            Result<?> result = handler.handleNullPointerException(ex);

            assertEquals(500, result.getCode());
            assertTrue(result.getMessage().contains("内部错误"));
        }

        @Test
        @DisplayName("TC-E010: 其他 Exception → 500")
        void handleGenericException() {
            Exception ex = new Exception("未知错误");

            Result<?> result = handler.handleException(ex);

            assertEquals(500, result.getCode());
            assertTrue(result.getMessage().contains("内部错误"));
        }
    }
}
