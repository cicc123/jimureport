package com.jimureport.enhancement.service;

import com.jimureport.enhancement.config.SaTokenAdapter;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JimuReportTokenServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JimuReportTokenServiceImplTest {

    @Mock private SysUserService userService;
    @Mock private SysPermissionService permissionService;
    @Mock private SaTokenAdapter saTokenAdapter;

    @InjectMocks
    private JimuReportTokenServiceImpl tokenService;

    @Nested
    @DisplayName("getToken - Token 获取")
    class GetTokenTests {

        @Test
        @DisplayName("TC-T001: 从 Authorization 头获取")
        void getToken_FromAuthorization() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("Bearer test-token-123");

            assertEquals("Bearer test-token-123", tokenService.getToken(request));
        }

        @Test
        @DisplayName("TC-T002: 从 X-Access-Token 头获取")
        void getToken_FromXAccessToken() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getHeader("X-Access-Token")).thenReturn("x-token-456");

            assertEquals("x-token-456", tokenService.getToken(request));
        }

        @Test
        @DisplayName("TC-T003: 从 token 头获取")
        void getToken_FromTokenHeader() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getHeader("X-Access-Token")).thenReturn(null);
            when(request.getHeader("token")).thenReturn("plain-token-789");

            assertEquals("plain-token-789", tokenService.getToken(request));
        }

        @Test
        @DisplayName("TC-T004: 从 URL 参数获取")
        void getToken_FromQueryParam() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getHeader("X-Access-Token")).thenReturn(null);
            when(request.getHeader("token")).thenReturn(null);
            when(request.getParameter("token")).thenReturn("query-token-000");

            assertEquals("query-token-000", tokenService.getToken(request));
        }

        @Test
        @DisplayName("TC-T005: 无任何 token 返回 null")
        void getToken_NoToken() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader(anyString())).thenReturn(null);
            when(request.getParameter(anyString())).thenReturn(null);
            when(request.getCookies()).thenReturn(null);

            assertNull(tokenService.getToken(request));
        }

        @Test
        @DisplayName("TC-T006: Authorization 头优先级最高")
        void getToken_HighestPriority() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("Authorization")).thenReturn("auth");
            when(request.getHeader("X-Access-Token")).thenReturn("x");
            when(request.getHeader("token")).thenReturn("t");
            when(request.getParameter("token")).thenReturn("q");

            assertEquals("auth", tokenService.getToken(request));
        }
    }

    @Nested
    @DisplayName("verifyToken - Token 验证")
    class VerifyTokenTests {

        @Test
        @DisplayName("TC-T010: 有效 token 返回 true")
        void verifyToken_Valid() {
            when(saTokenAdapter.getLoginIdByToken("valid-token")).thenReturn("user-001");

            assertTrue(tokenService.verifyToken("valid-token"));
        }

        @Test
        @DisplayName("TC-T011: 无效 token 返回 false")
        void verifyToken_Invalid() {
            when(saTokenAdapter.getLoginIdByToken("bad")).thenThrow(new RuntimeException("无效"));

            assertFalse(tokenService.verifyToken("bad"));
        }

        @Test
        @DisplayName("TC-T012: null token 返回 false")
        void verifyToken_Null() {
            assertFalse(tokenService.verifyToken(null));
        }

        @Test
        @DisplayName("TC-T013: 空字符串返回 false")
        void verifyToken_Empty() {
            assertFalse(tokenService.verifyToken(""));
        }

        @Test
        @DisplayName("TC-T014: Bearer 前缀被清理")
        void verifyToken_BearerPrefix() {
            when(saTokenAdapter.getLoginIdByToken("actual-token")).thenReturn("user-001");

            assertTrue(tokenService.verifyToken("Bearer actual-token"));
        }
    }

    @Nested
    @DisplayName("getUserInfo - 用户信息获取")
    class GetUserInfoTests {

        @Test
        @DisplayName("TC-T020: 有效 token 返回用户信息")
        void getUserInfo_Valid() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("user-001::testuser");

            SysUser user = new SysUser();
            user.setRealName("测试");
            user.setEmail("t@t.com");
            user.setPhone("13800138000");
            when(userService.getUserById("user-001")).thenReturn(user);

            Map<String, Object> info = tokenService.getUserInfo("tok");

            assertEquals("user-001", info.get("userId"));
            assertEquals("testuser", info.get("username"));
            assertEquals("测试", info.get("realName"));
            assertEquals("t@t.com", info.get("email"));
        }

        @Test
        @DisplayName("TC-T021: null token 返回空 Map")
        void getUserInfo_NullToken() {
            assertTrue(tokenService.getUserInfo(null).isEmpty());
        }

        @Test
        @DisplayName("TC-T022: 无效 token 返回空 Map")
        void getUserInfo_Invalid() {
            when(saTokenAdapter.getLoginIdByToken("bad")).thenThrow(new RuntimeException());

            assertTrue(tokenService.getUserInfo("bad").isEmpty());
        }

        @Test
        @DisplayName("TC-T023: loginId 无分隔符")
        void getUserInfo_NoSeparator() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("plain-id");

            Map<String, Object> info = tokenService.getUserInfo("tok");

            assertEquals("plain-id", info.get("userId"));
            assertEquals("plain-id", info.get("username"));
        }
    }

    @Nested
    @DisplayName("getUsername - 用户名获取")
    class GetUsernameTests {

        @Test
        @DisplayName("TC-T030: 有效 token 返回用户名")
        void getUsername_Valid() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("user-001::testuser");

            assertEquals("testuser", tokenService.getUsername("tok"));
        }

        @Test
        @DisplayName("TC-T031: null token 返回空")
        void getUsername_Null() {
            assertEquals("", tokenService.getUsername(null));
        }

        @Test
        @DisplayName("TC-T032: 无分隔符返回 loginId")
        void getUsername_NoSeparator() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("only-id");

            assertEquals("only-id", tokenService.getUsername("tok"));
        }
    }

    @Nested
    @DisplayName("getRoles - 角色获取")
    class GetRolesTests {

        @Test
        @DisplayName("TC-T040: 有角色用户返回数组")
        void getRoles_WithRoles() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("user-001::testuser");

            SysRole r1 = new SysRole(); r1.setRoleKey("user");
            SysRole r2 = new SysRole(); r2.setRoleKey("designer");
            when(permissionService.getUserRoles("user-001")).thenReturn(Arrays.asList(r1, r2));

            String[] roles = tokenService.getRoles("tok");

            assertEquals(2, roles.length);
            assertTrue(Arrays.asList(roles).contains("user"));
        }

        @Test
        @DisplayName("TC-T041: 无角色返回空数组")
        void getRoles_NoRoles() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("user-001");
            when(permissionService.getUserRoles("user-001")).thenReturn(Collections.emptyList());

            assertEquals(0, tokenService.getRoles("tok").length);
        }

        @Test
        @DisplayName("TC-T042: null token 返回空数组")
        void getRoles_NullToken() {
            assertEquals(0, tokenService.getRoles(null).length);
        }
    }

    @Nested
    @DisplayName("getPermissions - 权限获取")
    class GetPermissionsTests {

        @Test
        @DisplayName("TC-T050: 有权限返回数组")
        void getPermissions_WithPerms() {
            when(saTokenAdapter.getLoginIdByToken("tok")).thenReturn("user-001");
            when(permissionService.getUserPermissions("user-001"))
                    .thenReturn(new HashSet<>(Arrays.asList("user:list", "report:list")));

            String[] perms = tokenService.getPermissions("tok");

            assertEquals(2, perms.length);
        }

        @Test
        @DisplayName("TC-T051: 无效 token 返回空数组")
        void getPermissions_Invalid() {
            when(saTokenAdapter.getLoginIdByToken("bad")).thenThrow(new RuntimeException());

            assertEquals(0, tokenService.getPermissions("bad").length);
        }

        @Test
        @DisplayName("TC-T052: null token 返回空数组")
        void getPermissions_Null() {
            assertEquals(0, tokenService.getPermissions(null).length);
        }
    }
}
