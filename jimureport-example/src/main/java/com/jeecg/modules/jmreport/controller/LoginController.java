package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.satoken.util.AjaxRequestUtils;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

/**
 * 积木报表-设置默认首页跳转
 */
@Controller
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(LoginController.class);
    public final static String LOGIN_PAGE = "/login/login.html";

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 登录请求
     *
     * @param username
     * @param password
     * @param req
     * @return
     */
    @PostMapping("/doLogin")
    public String login(@RequestParam String username, @RequestParam String password, jakarta.servlet.http.HttpServletRequest req) {
        String ip = req.getRemoteAddr();
        logger.info("登录请求，用户名：" + username);
        
        try {
            var result = userService.login(username, password);
            if (result.getSuccess()) {
                logger.info("登录成功，当前会话tokeName={}, tokenValue={}", StpUtil.getTokenName(), StpUtil.getTokenValue());

                // 记录登录成功日志
                auditLogService.logLogin(username, ip, true, "登录成功");

                // 设置登录来源，方便退出登录时区分
                AjaxRequestUtils.setLoginSessionInfo();
                
                // 根据用户角色跳转到不同页面
                try {
                    logger.info("开始获取用户信息：{}", username);
                    Map<String, Object> userInfo = userService.getUserInfo(username);
                    logger.info("获取到用户信息：{}", userInfo);
                    
                    // 检查userInfo是否为null
                    if (userInfo == null) {
                        logger.error("用户信息为null：{}", username);
                        return "redirect:/index.html";
                    }
                    
                    // 检查is_admin字段是否存在
                    if (!userInfo.containsKey("is_admin")) {
                        logger.error("用户信息中缺少is_admin字段：{}", userInfo);
                        return "redirect:/index.html";
                    }
                    
                    int isAdmin = Integer.parseInt(userInfo.get("is_admin").toString());
                    logger.info("用户 {} 的is_admin值：{}", username, isAdmin);
                    
                    if (isAdmin == 1) {
                        // 管理员跳转到首页
                        logger.info("管理员用户 {} 跳转到首页", username);
                        return "redirect:/index.html";
                    } else {
                        // 非管理员跳转到报表门户
                        logger.info("非管理员用户 {} 跳转到报表门户", username);
                        return "redirect:/static/portal/index.html";
                    }
                } catch (Exception e) {
                    logger.error("获取用户信息失败：" + e.getMessage(), e);
                    // 出错时默认跳转到首页
                    return "redirect:/index.html";
                }
            } else {
                logger.error("登录失败：" + result.getMsg());
                // 记录登录失败日志
                auditLogService.logLogin(username, ip, false, result.getMsg());
                // 返回密码错误提示
                return "redirect:" + LOGIN_PAGE + "?error=1&msg=" + result.getMsg();
            }
        } catch (Exception e) {
            logger.error("登录异常：" + e.getMessage());
            // 记录登录异常日志
            auditLogService.logLogin(username, ip, false, "登录异常：" + e.getMessage());
            return "redirect:" + LOGIN_PAGE + "?error=1&msg=" + e.getMessage();
        }
    }

    /**
     * 首页跳转
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String index(Model model) {
        if (!StpUtil.isLogin()) {
            return "redirect:" + LOGIN_PAGE;
        }
        
        // 根据用户角色跳转到不同页面
        try {
            String username = StpUtil.getLoginIdAsString();
            Map<String, Object> userInfo = userService.getUserInfo(username);
            int isAdmin = Integer.parseInt(userInfo.get("is_admin").toString());
            if (isAdmin == 1) {
                // 管理员跳转到首页
                model.addAttribute("name", "jimureport");
                return "redirect:/index.html"; // 重定向到管理员首页
            } else {
                // 非管理员跳转到报表门户
                return "redirect:/static/portal/index.html";
            }
        } catch (Exception e) {
            logger.error("获取用户信息失败：" + e.getMessage());
            // 出错时默认跳转到首页
            model.addAttribute("name", "jimureport");
            return "redirect:/index.html";
        }
    }

    /**
     * 查询登录状态
     *
     * @return
     */
    @RequestMapping("/isLogin")
    public String isLogin() {
        logger.info("查询登录状态:{}", StpUtil.getTokenInfo());
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    /**
     * 退出登录
     *
     * @return
     */
    @RequestMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "redirect:/login/login.html";
    }
    
    /**
     * 退出登录（备用路径）
     *
     * @return
     */
    @RequestMapping("/api/logout")
    public String apiLogout() {
        StpUtil.logout();
        return "redirect:/login/login.html";
    }

    /**
     * 跳转到修改密码页面
     */
    @GetMapping("/changePassword")
    public String changePassword() {
        if (!StpUtil.isLogin()) {
            return "redirect:" + LOGIN_PAGE;
        }
        return "redirect:/login/changePassword.html";
    }

    /**
     * 执行修改密码
     */
    @PostMapping("/doChangePassword")
    public String doChangePassword(@RequestParam String oldPassword, @RequestParam String newPassword, jakarta.servlet.http.HttpServletRequest req) {
        if (!StpUtil.isLogin()) {
            return "redirect:" + LOGIN_PAGE;
        }

        String ip = req.getRemoteAddr();
        String username = StpUtil.getLoginIdAsString();

        var result = userService.changePassword(oldPassword, newPassword);
        if (result.getSuccess()) {
            // 记录密码修改成功日志
            auditLogService.logPasswordChange(username, ip, true, "密码修改成功");
            return "redirect:/";
        } else {
            // 记录密码修改失败日志
            auditLogService.logPasswordChange(username, ip, false, result.getMsg());
            return "redirect:/changePassword?error=" + result.getMsg();
        }
    }
}