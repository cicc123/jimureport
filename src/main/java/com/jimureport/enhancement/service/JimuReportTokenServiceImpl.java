package com.jimureport.enhancement.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jimureport.enhancement.entity.SysUser;
import com.jimureport.enhancement.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 积木报表 Token 服务实现
 * 
 * 纯 Sa-Token 验证：
 * 通过 StpUtil.getLoginIdByToken() 直接验证 token 字符串，
 * 不依赖当前请求上下文的 session。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JimuReportTokenServiceImpl implements JmReportTokenServiceI {

    private final SysUserService userService;

    private String cleanToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token.trim();
    }

    /**
     * 从请求中获取 Token
     * 优先级: Authorization → X-Access-Token header → token header → URL token 参数 → Cookie
     */
    @Override
    public String getToken(HttpServletRequest request) {
        // 1. Authorization header
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) return token;

        // 2. X-Access-Token header (积木报表前端默认使用)
        token = request.getHeader("X-Access-Token");
        if (token != null && !token.isEmpty()) return token;

        // 3. token header
        token = request.getHeader("token");
        if (token != null && !token.isEmpty()) return token;

        // 4. URL query parameter (报表分享链接)
        token = request.getParameter("token");
        if (token != null && !token.isEmpty()) return token;

        // 5. Cookie
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

    /**
     * 验证 Token
     * 使用 StpUtil.getLoginIdByToken() 直接验证 token 字符串
     */
    @Override
    public Boolean verifyToken(String token) {
        token = cleanToken(token);
        if (token == null || token.isEmpty()) return false;

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            return loginId != null;
        } catch (Exception e) {
            log.debug("Sa-Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户信息
     */
    @Override
    public Map<String, Object> getUserInfo(String token) {
        Map<String, Object> userInfo = new HashMap<>();
        token = cleanToken(token);
        if (token == null) return userInfo;

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                String loginIdStr = loginId.toString();
                String[] parts = loginIdStr.split("::", 2);
                userInfo.put("userId", parts[0]);
                userInfo.put("username", parts.length > 1 ? parts[1] : parts[0]);
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }

        return userInfo;
    }

    /**
     * 获取用户名
     */
    @Override
    public String getUsername(String token) {
        token = cleanToken(token);
        if (token == null) return "";

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                String loginIdStr = loginId.toString();
                String[] parts = loginIdStr.split("::", 2);
                return parts.length > 1 ? parts[1] : parts[0];
            }
        } catch (Exception e) {
            log.debug("获取用户名失败: {}", e.getMessage());
        }

        return "";
    }

    /**
     * 获取用户角色（验证通过即返回 admin）
     */
    @Override
    public String[] getRoles(String token) {
        token = cleanToken(token);
        if (token == null) return new String[0];

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                return new String[]{"admin"};
            }
        } catch (Exception e) {
            log.debug("获取角色失败: {}", e.getMessage());
        }

        return new String[0];
    }
}
