package com.jimureport.enhancement.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jimureport.enhancement.config.SecurityConfig;
import com.jimureport.enhancement.util.AjaxRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;

/**
 * 积木报表-设置默认首页跳转（参考 jeecgboot/jimureport 原始示例）
 *
 * 提供报表界面的登录入口，支持通过配置的账号密码登录，
 * 或使用已有的 Sa-Token 会话直接访问报表。
 */
@Slf4j
@Controller
public class JimuReportLoginController {

    public static final String LOGIN_PAGE = "/login/login.html";

    @Autowired
    private SecurityConfig securityConfig;

    /**
     * 报表登录请求
     * 优先使用配置文件中的账号密码验证
     */
    @GetMapping("/doLogin")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        jakarta.servlet.http.HttpServletRequest req) {
        log.info("报表登录请求，用户名：{}", username);
        try {
            SecurityConfig.UserConfig userConfig = securityConfig.getUser();
            if (userConfig != null && userConfig.getName().equals(username) && userConfig.getPassword().equals(password)) {
                StpUtil.login(userConfig.getName());
                log.info("报表登录成功，tokenName={}, tokenValue={}", StpUtil.getTokenName(), StpUtil.getTokenValue());
                AjaxRequestUtils.setLoginSessionInfo();
                return "jmreport/list";
            }
        } catch (Exception e) {
            log.error("报表登录失败", e);
        }
        log.error("报表登录失败，用户名或密码错误");
        return "redirect:" + LOGIN_PAGE + "?error=1";
    }

    /**
     * 首页跳转到报表列表
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "jimureport");
        if (StpUtil.isLogin()) {
            return "jmreport/list";
        }
        return "redirect:" + LOGIN_PAGE;
    }

    /**
     * 查询登录状态
     */
    @RequestMapping("/isLogin")
    public String isLogin() {
        log.info("查询报表登录状态: {}", StpUtil.getTokenInfo());
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "redirect:" + LOGIN_PAGE;
    }
}
