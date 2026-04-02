package com.jimureport.enhancement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jimureport.enhancement.entity.SysUser;
import com.jimureport.enhancement.entity.SysUserRole;
import com.jimureport.enhancement.mapper.SysUserMapper;
import com.jimureport.enhancement.mapper.SysUserRoleMapper;
import com.jimureport.enhancement.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserServiceImplTest {

    @Mock private SysUserMapper userMapper;
    @Mock private SysUserRoleMapper userRoleMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SysUserServiceImpl userService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId("test-user-id-001");
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPasswordHash");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setStatus("0");
        testUser.setDelFlag("0");
    }

    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {

        @Test
        @DisplayName("TC-U001: 正常注册")
        void register_Success() {
            SysUser newUser = new SysUser();
            newUser.setUsername("newuser");
            newUser.setPassword("Test123456");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(passwordEncoder.encode("Test123456")).thenReturn("$2a$10$encodedNew");
            when(userMapper.insert(any(SysUser.class))).thenReturn(1);

            SysUser result = userService.register(newUser);

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("$2a$10$encodedNew", result.getPassword());
            verify(userMapper).insert(any(SysUser.class));
        }

        @Test
        @DisplayName("TC-U002: 用户名已存在")
        void register_DuplicateUsername() {
            SysUser newUser = new SysUser();
            newUser.setUsername("existinguser");
            newUser.setPassword("Test123456");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(newUser));
            assertTrue(ex.getMessage().contains("已存在"));
        }

        @Test
        @DisplayName("TC-U003: 密码加密存储")
        void register_PasswordEncoded() {
            SysUser newUser = new SysUser();
            newUser.setUsername("pwtest");
            newUser.setPassword("plaintext123");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(passwordEncoder.encode("plaintext123")).thenReturn("$2a$10$hashed");
            when(userMapper.insert(any(SysUser.class))).thenReturn(1);

            SysUser result = userService.register(newUser);

            assertEquals("$2a$10$hashed", result.getPassword());
            assertNotEquals("plaintext123", result.getPassword());
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("TC-U005: 用户不存在")
        void login_UserNotFound() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.login("nonexistent", "password"));
            assertTrue(ex.getMessage().contains("用户不存在"));
        }

        @Test
        @DisplayName("TC-U006: 用户被停用")
        void login_UserDisabled() {
            SysUser disabledUser = new SysUser();
            disabledUser.setId("disabled-id");
            disabledUser.setUsername("disabled");
            disabledUser.setStatus("1");
            disabledUser.setPassword("$2a$10$hash");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(disabledUser);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.login("disabled", "password"));
            assertTrue(ex.getMessage().contains("已被停用"));
        }

        @Test
        @DisplayName("TC-U007: 密码错误")
        void login_WrongPassword() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("wrongpass", testUser.getPassword())).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.login("testuser", "wrongpass"));
            assertTrue(ex.getMessage().contains("密码错误"));
        }
    }

    @Nested
    @DisplayName("用户查询测试")
    class QueryTests {

        @Test
        @DisplayName("TC-U008: 按 ID 查询用户")
        void getUserById_Success() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);

            SysUser result = userService.getUserById("test-user-id-001");

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
        }

        @Test
        @DisplayName("TC-U009: 按 ID 查询不存在的用户")
        void getUserById_NotFound() {
            when(userMapper.selectById("nonexistent")).thenReturn(null);

            SysUser result = userService.getUserById("nonexistent");
            assertNull(result);
        }

        @Test
        @DisplayName("TC-U010: 按用户名查询")
        void getUserByUsername_Success() {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

            SysUser result = userService.getUserByUsername("testuser");

            assertNotNull(result);
            assertEquals("test-user-id-001", result.getId());
        }

        @Test
        @DisplayName("TC-U011: 分页查询用户列表")
        void getUserPage_Success() {
            Page<SysUser> page = new Page<>(1, 10);
            page.setRecords(Collections.singletonList(testUser));
            page.setTotal(1);

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            IPage<SysUser> result = userService.getUserPage(1, 10, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("用户修改测试")
    class UpdateTests {

        @Test
        @DisplayName("TC-U012: 正常修改用户信息")
        void updateUser_Success() {
            SysUser updateData = new SysUser();
            updateData.setId("test-user-id-001");
            updateData.setRealName("修改后的姓名");

            SysUser updatedUser = new SysUser();
            updatedUser.setId("test-user-id-001");
            updatedUser.setUsername("testuser");
            updatedUser.setRealName("修改后的姓名");

            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser).thenReturn(updatedUser);
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            SysUser result = userService.updateUser(updateData);

            assertNotNull(result);
            assertEquals("修改后的姓名", result.getRealName());
        }

        @Test
        @DisplayName("TC-U013: 修改不存在的用户")
        void updateUser_NotFound() {
            SysUser updateData = new SysUser();
            updateData.setId("nonexistent");
            when(userMapper.selectById("nonexistent")).thenReturn(null);

            assertThrows(RuntimeException.class, () -> userService.updateUser(updateData));
        }
    }

    @Nested
    @DisplayName("用户删除测试")
    class DeleteTests {

        @Test
        @DisplayName("TC-U014: 逻辑删除用户")
        void deleteUser_Success() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = userService.deleteUser("test-user-id-001");

            assertTrue(result);
            verify(userMapper).updateById(argThat((SysUser user) -> "2".equals(user.getDelFlag())));
        }

        @Test
        @DisplayName("TC-U015: 删除不存在的用户")
        void deleteUser_NotFound() {
            when(userMapper.selectById("nonexistent")).thenReturn(null);

            assertThrows(RuntimeException.class, () -> userService.deleteUser("nonexistent"));
        }
    }

    @Nested
    @DisplayName("密码管理测试")
    class PasswordTests {

        @Test
        @DisplayName("TC-U016: 修改密码 - 旧密码正确")
        void changePassword_Success() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(passwordEncoder.matches("oldPass", testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPass")).thenReturn("$2a$10$newHash");
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = userService.changePassword("test-user-id-001", "oldPass", "newPass");

            assertTrue(result);
        }

        @Test
        @DisplayName("TC-U017: 修改密码 - 旧密码错误")
        void changePassword_WrongOldPassword() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(passwordEncoder.matches("wrongOld", testUser.getPassword())).thenReturn(false);

            assertThrows(RuntimeException.class,
                    () -> userService.changePassword("test-user-id-001", "wrongOld", "newPass"));
        }

        @Test
        @DisplayName("TC-U018: 重置密码")
        void resetPassword_Success() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(passwordEncoder.encode("resetPass")).thenReturn("$2a$10$resetHash");
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = userService.resetPassword("test-user-id-001", "resetPass");

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("角色分配测试")
    class RoleAssignmentTests {

        @Test
        @DisplayName("TC-U019: 分配用户角色")
        void assignUserRoles_Success() {
            when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
            when(userRoleMapper.insert(any(SysUserRole.class))).thenReturn(1);

            boolean result = userService.assignUserRoles("test-user-id-001", Arrays.asList("role1", "role2"));

            assertTrue(result);
            verify(userRoleMapper, times(2)).insert(any(SysUserRole.class));
        }
    }

    @Nested
    @DisplayName("用户状态测试")
    class StatusTests {

        @Test
        @DisplayName("TC-U020: 启用用户")
        void changeUserStatus_Enable() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = userService.changeUserStatus("test-user-id-001", "0");

            assertTrue(result);
            verify(userMapper).updateById(argThat((SysUser user) -> "0".equals(user.getStatus())));
        }

        @Test
        @DisplayName("TC-U021: 停用用户")
        void changeUserStatus_Disable() {
            when(userMapper.selectById("test-user-id-001")).thenReturn(testUser);
            when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = userService.changeUserStatus("test-user-id-001", "1");

            assertTrue(result);
            verify(userMapper).updateById(argThat((SysUser user) -> "1".equals(user.getStatus())));
        }
    }
}
