package com.jeecg.modules.jmreport.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头或域名中获取租户信息
        String tenantId = request.getHeader("X-Tenant-Id");
        String tenantCode = request.getHeader("X-Tenant-Code");

        // 如果请求头中没有，尝试从域名中获取
        if (tenantId == null || tenantCode == null) {
            String serverName = request.getServerName();
            // 简单示例：从域名中提取租户信息
            // 例如：tenant1.example.com -> tenant1
            if (serverName.contains(".")) {
                String[] parts = serverName.split("\\.");
                if (parts.length > 2) {
                    tenantCode = parts[0];
                    // 这里可以根据tenantCode查询对应的tenantId
                    // 简化处理，默认使用1
                    tenantId = "1";
                }
            }
        }

        // 设置默认租户
        if (tenantId == null) {
            tenantId = "1";
        }
        if (tenantCode == null) {
            tenantCode = "default";
        }

        TenantContext.setTenantId(tenantId);
        TenantContext.setTenantCode(tenantCode);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 清理租户上下文
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理租户上下文
        TenantContext.clear();
    }
}
