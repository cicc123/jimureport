package com.jimureport.enhancement.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全配置 - Sa-Token + Spring Security
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    // 不需要登录验证的路径
    private static final String[] EXCLUDE_PATHS = {
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
            "/static/**",
            "/favicon.ico"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 配置 - 仅保留基础防护，认证交给 Sa-Token
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }

    /**
     * 注册 Sa-Token 登录校验 + 用户信息注入拦截器（合并为一个）
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

            // 3. 未登录时返回 401（即使设置默认值以防止 Spring MVC 参数解析异常）
            if (!loggedIn) {
                // 设置默认值，防止 @RequestAttribute 参数解析异常
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
