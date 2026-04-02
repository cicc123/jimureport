package com.jimureport.enhancement.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 安全配置 - 统一使用 Sa-Token
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    /**
     * 配置文件中的用户账号（支持报表登录）
     */
    @Data
    @ConfigurationProperties(prefix = "spring.security.user")
    public static class UserConfig {
        private String name = "admin";
        private String password = "123456";
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.security.user")
    public UserConfig userConfig() {
        return new UserConfig();
    }

    public UserConfig getUser() {
        return userConfig();
    }

    // 不需要登录验证的路径
    private static final String[] EXCLUDE_PATHS = {
            "/",
            "/api/user/login",
            "/api/user/register",
            "/api/public/**",
            "/jmreport/**",
            "/drag/**",
            "/sys/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/actuator/**",
            "/assets/**",
            "/static/**",
            "/favicon.ico",
            "/*.html",
            "*.js",
            "*.css",
            "*.svg",
            "*.png",
            "*.ico"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册 Sa-Token 登录校验 + 用户信息注入拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaTokenUserInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    /**
     * Sa-Token 登录校验 + 用户信息注入拦截器
     * - 未登录：返回 401 JSON
     * - 已登录：将 userId/username 注入 request 属性
     */
    public static class SaTokenUserInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 设置安全响应头
            response.setHeader("Content-Security-Policy", "frame-ancestors *");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("X-Content-Type-Options", "nosniff");

            // 1. Sa-Token 登录校验
            boolean loggedIn = false;
            try {
                StpUtil.checkLogin();
                loggedIn = true;
            } catch (Exception e) {
                log.debug("Sa-Token 登录校验失败: {}", e.getMessage());
            }

            // 2. 注入用户信息
            if (loggedIn) {
                try {
                    Object loginId = StpUtil.getLoginId();
                    if (loginId != null) {
                        String id = loginId.toString();
                        String[] parts = id.split("::", 2);
                        request.setAttribute("userId", parts[0]);
                        request.setAttribute("username", parts.length > 1 ? parts[1] : parts[0]);
                    }
                } catch (Exception e) {
                    log.warn("用户信息注入失败: {}", e.getMessage());
                    loggedIn = false;
                }
            }

            // 3. 未登录时返回 401
            if (!loggedIn) {
                request.setAttribute("userId", "");
                request.setAttribute("username", "");
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"请先登录\",\"data\":null}");
                return false;
            }

            return true;
        }
    }
}
