package com.jimureport.enhancement.service;

import com.jimureport.enhancement.config.SaTokenAdapter;
import com.jimureport.enhancement.entity.SysRole;
import com.jimureport.enhancement.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 积木报表 Token 服务实现
 * 
 * 通过 SaTokenAdapter 封装 StpUtil 静态调用，便于单元测试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JimuReportTokenServiceImpl implements JmReportTokenServiceI {

    private final SysUserService userService;
    private final SysPermissionService permissionService;
    private final SaTokenAdapter saTokenAdapter;

    private String cleanToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token.trim();
    }

    @Override
    public String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) return token;

        token = request.getHeader("X-Access-Token");
        if (token != null && !token.isEmpty()) return token;

        token = request.getHeader("token");
        if (token != null && !token.isEmpty()) return token;

        token = request.getParameter("token");
        if (token != null && !token.isEmpty()) return token;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("X-Access-Token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public Boolean verifyToken(String token) {
        token = cleanToken(token);
        if (token == null || token.isEmpty()) return false;
        try {
            return saTokenAdapter.getLoginIdByToken(token) != null;
        } catch (Exception e) {
            log.debug("Sa-Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> userInfo = new HashMap<>();
        token = cleanToken(token);
        if (token == null) return userInfo;
        try {
            Object loginId = saTokenAdapter.getLoginIdByToken(token);
            if (loginId != null) {
                String[] parts = loginId.toString().split("::", 2);
                String userId = parts[0];
                userInfo.put("userId", userId);
                userInfo.put("username", parts.length > 1 ? parts[1] : parts[0]);

                SysUser user = userService.getUserById(userId);
                if (user != null) {
                    userInfo.put("realName", user.getRealName());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("phone", user.getPhone());
                    userInfo.put("deptId", user.getDeptId());
                    userInfo.put("avatar", user.getAvatar());
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }
        return userInfo;
    }

    @Override
    public String getUsername(String token) {
        token = cleanToken(token);
        if (token == null) return "";
        try {
            Object loginId = saTokenAdapter.getLoginIdByToken(token);
            if (loginId != null) {
                String[] parts = loginId.toString().split("::", 2);
                return parts.length > 1 ? parts[1] : parts[0];
            }
        } catch (Exception e) {
            log.debug("获取用户名失败: {}", e.getMessage());
        }
        return "";
    }

    @Override
    @Cacheable(value = "user:roles", key = "#token", unless = "#result == null || #result.length == 0")
    public String[] getRoles(String token) {
        token = cleanToken(token);
        if (token == null) return new String[0];
        try {
            Object loginId = saTokenAdapter.getLoginIdByToken(token);
            if (loginId != null) {
                String userId = loginId.toString().split("::", 2)[0];
                List<SysRole> roles = permissionService.getUserRoles(userId);
                if (roles != null && !roles.isEmpty()) {
                    return roles.stream().map(SysRole::getRoleKey).toArray(String[]::new);
                }
            }
        } catch (Exception e) {
            log.debug("获取角色失败: {}", e.getMessage());
        }
        return new String[0];
    }

    public String[] getPermissions(String token) {
        token = cleanToken(token);
        if (token == null) return new String[0];
        try {
            Object loginId = saTokenAdapter.getLoginIdByToken(token);
            if (loginId != null) {
                String userId = loginId.toString().split("::", 2)[0];
                Set<String> permissions = permissionService.getUserPermissions(userId);
                if (permissions != null && !permissions.isEmpty()) {
                    return permissions.toArray(new String[0]);
                }
            }
        } catch (Exception e) {
            log.debug("获取权限失败: {}", e.getMessage());
        }
        return new String[0];
    }
}
