package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.satoken.exception.AjaxJson;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.PermissionService;
import com.jeecg.modules.jmreport.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public AjaxJson getUserList(@RequestParam(required = false) String username, 
                               @RequestParam(required = false) Integer status, 
                               @RequestParam(defaultValue = "1") Integer page, 
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(defaultValue = "create_time") String sortField,
                               @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            List<Map<String, Object>> users = userService.getUserList(username, status, page, pageSize, sortField, sortOrder);
            int total = userService.getUserCount(username, status);
            Map<String, Object> result = Map.of(
                "list", users,
                "total", total,
                "page", page,
                "pageSize", pageSize
            );
            return AjaxJson.success().put("data", result);
        } catch (Exception e) {
            logger.error("获取用户列表失败：", e);
            return AjaxJson.error("获取用户列表失败");
        }
    }

    /**
     * 获取单个用户信息
     */
    @GetMapping("/info/{id}")
    public AjaxJson getUserInfo(@PathVariable String id) {
        try {
            Map<String, Object> user = userService.getUserById(id);
            if (user == null) {
                return AjaxJson.error("用户不存在");
            }
            return AjaxJson.success().put("data", user);
        } catch (Exception e) {
            logger.error("获取用户信息失败：", e);
            return AjaxJson.error("获取用户信息失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public AjaxJson createUser(@RequestBody Map<String, Object> userData, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = "anonymous";
            
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                currentUser = StpUtil.getLoginIdAsString();
                
                // 检查权限
                if (!permissionService.hasPermission("user:manage")) {
                    auditLogService.logUserOperation(currentUser, ip, "创建用户", "权限不足：用户没有创建用户的权限");
                    return AjaxJson.error("权限不足：您没有创建用户的权限");
                }
            }
            
            AjaxJson result = userService.createUser(userData);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "创建用户", "成功创建用户：" + userData.get("username"));
            } else {
                auditLogService.logUserOperation(currentUser, ip, "创建用户", "创建用户失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("创建用户失败：", e);
            return AjaxJson.error("创建用户失败");
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/update/{id}")
    public AjaxJson updateUser(@PathVariable String id, @RequestBody Map<String, Object> userData, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = "anonymous";
            
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                currentUser = StpUtil.getLoginIdAsString();
            }
            
            AjaxJson result = userService.updateUser(id, userData);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "更新用户", "成功更新用户：" + userData.get("username"));
            } else {
                auditLogService.logUserOperation(currentUser, ip, "更新用户", "更新用户失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("更新用户失败：", e);
            return AjaxJson.error("更新用户失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public AjaxJson deleteUser(@PathVariable String id, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = "anonymous";
            
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                currentUser = StpUtil.getLoginIdAsString();
            }
            
            AjaxJson result = userService.deleteUser(id);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "删除用户", "成功删除用户ID：" + id);
            } else {
                auditLogService.logUserOperation(currentUser, ip, "删除用户", "删除用户失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("删除用户失败：", e);
            return AjaxJson.error("删除用户失败");
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batchDelete")
    public AjaxJson batchDeleteUser(@RequestBody List<String> userIds, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = "anonymous";
            
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                currentUser = StpUtil.getLoginIdAsString();
            }
            
            AjaxJson result = userService.batchDeleteUser(userIds);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "批量删除用户", "成功删除用户数量：" + userIds.size());
            } else {
                auditLogService.logUserOperation(currentUser, ip, "批量删除用户", "批量删除用户失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("批量删除用户失败：", e);
            return AjaxJson.error("批量删除用户失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/status/{id}")
    public AjaxJson updateUserStatus(@PathVariable String id, @RequestParam Integer status, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = "anonymous";
            
            // 检查用户是否登录
            if (StpUtil.isLogin()) {
                currentUser = StpUtil.getLoginIdAsString();
            }
            
            AjaxJson result = userService.updateUserStatus(id, status);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, status == 1 ? "启用用户" : "禁用用户", "成功" + (status == 1 ? "启用" : "禁用") + "用户ID：" + id);
            } else {
                auditLogService.logUserOperation(currentUser, ip, status == 1 ? "启用用户" : "禁用用户", (status == 1 ? "启用" : "禁用") + "用户失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("更新用户状态失败：", e);
            return AjaxJson.error("更新用户状态失败");
        }
    }
}
