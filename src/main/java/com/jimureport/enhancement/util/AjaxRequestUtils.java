package com.jimureport.enhancement.util;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.session.SaSession;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AJAX 请求工具类（参考 jeecgboot/jimureport 原始示例）
 * - 判断是否为 AJAX 请求
 * - 设置 Sa-Token 会话登录来源
 */
@Slf4j
public class AjaxRequestUtils {

    /**
     * 判断是否为 AJAX 请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        String contentType = request.getHeader("Content-Type");
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        }
        String ajaxParam = request.getParameter("_ajax");
        return "true".equals(ajaxParam);
    }

    /**
     * 根据请求类型返回相应响应
     */
    public static void writeResponse(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object data) throws IOException {
        if (isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(data));
        } else {
            request.setAttribute("data", data);
        }
    }

    /**
     * 登录来源常量 - 积木报表示例
     */
    public static final String LOGIN_FROM_MODEL_NEED_LOGOUT = "jimu_example";

    /**
     * 设置 Sa-Token 会话的登录来源和拖拽开关
     */
    public static void setLoginSessionInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();
            HttpSession session = request.getSession();
            if (session.getAttribute("loginFrom") == null) {
                log.debug("设置登录来源，BI与报表切换开关，注入个性化session信息");
                session.setAttribute("loginFrom", LOGIN_FROM_MODEL_NEED_LOGOUT);
                session.setAttribute("switchJimuDrag", "true");
            }
        } catch (Exception e) {
            log.debug("设置登录来源失败: {}", e.getMessage());
        }
    }
}
