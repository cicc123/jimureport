package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.satoken.exception.AjaxJson;
import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/userGroup")
public class UserGroupController {

    private Logger logger = LoggerFactory.getLogger(UserGroupController.class);

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 获取用户组列表
     */
    @GetMapping("/list")
    public AjaxJson getUserGroupList(@RequestParam(required = false) String groupName, 
                                   @RequestParam(required = false) Integer status, 
                                   @RequestParam(defaultValue = "1") Integer page, 
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<Map<String, Object>> groups = userGroupService.getUserGroupList(groupName, status, page, pageSize);
            int total = userGroupService.getUserGroupCount(groupName, status);
            Map<String, Object> result = Map.of(
                "list", groups,
                "total", total,
                "page", page,
                "pageSize", pageSize
            );
            return AjaxJson.success().put("data", result);
        } catch (Exception e) {
            logger.error("获取用户组列表失败：", e);
            return AjaxJson.error("获取用户组列表失败");
        }
    }

    /**
     * 获取单个用户组信息
     */
    @GetMapping("/info/{id}")
    public AjaxJson getUserGroupInfo(@PathVariable String id) {
        try {
            Map<String, Object> group = userGroupService.getUserGroupById(id);
            if (group == null) {
                return AjaxJson.error("用户组不存在");
            }
            return AjaxJson.success().put("data", group);
        } catch (Exception e) {
            logger.error("获取用户组信息失败：", e);
            return AjaxJson.error("获取用户组信息失败");
        }
    }

    /**
     * 获取用户组中的用户列表
     */
    @GetMapping("/users/{groupId}")
    public AjaxJson getUsersInGroup(@PathVariable String groupId) {
        try {
            List<Map<String, Object>> users = userGroupService.getUsersInGroup(groupId);
            return AjaxJson.success().put("data", users);
        } catch (Exception e) {
            logger.error("获取用户组用户列表失败：", e);
            return AjaxJson.error("获取用户组用户列表失败");
        }
    }

    /**
     * 获取不在用户组中的用户列表
     */
    @GetMapping("/usersNotInGroup/{groupId}")
    public AjaxJson getUsersNotInGroup(@PathVariable String groupId, @RequestParam(required = false) String username) {
        try {
            List<Map<String, Object>> users = userGroupService.getUsersNotInGroup(groupId, username);
            return AjaxJson.success().put("data", users);
        } catch (Exception e) {
            logger.error("获取不在用户组中的用户列表失败：", e);
            return AjaxJson.error("获取不在用户组中的用户列表失败");
        }
    }

    /**
     * 创建用户组
     */
    @PostMapping("/create")
    public AjaxJson createUserGroup(@RequestBody Map<String, Object> groupData, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = StpUtil.getLoginIdAsString();
            
            AjaxJson result = userGroupService.createUserGroup(groupData);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "创建用户组", "成功创建用户组：" + groupData.get("group_name"));
            } else {
                auditLogService.logUserOperation(currentUser, ip, "创建用户组", "创建用户组失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("创建用户组失败：", e);
            return AjaxJson.error("创建用户组失败");
        }
    }

    /**
     * 更新用户组
     */
    @PutMapping("/update/{id}")
    public AjaxJson updateUserGroup(@PathVariable String id, @RequestBody Map<String, Object> groupData, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = StpUtil.getLoginIdAsString();
            
            AjaxJson result = userGroupService.updateUserGroup(id, groupData);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "更新用户组", "成功更新用户组：" + groupData.get("group_name"));
            } else {
                auditLogService.logUserOperation(currentUser, ip, "更新用户组", "更新用户组失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("更新用户组失败：", e);
            return AjaxJson.error("更新用户组失败");
        }
    }

    /**
     * 删除用户组
     */
    @DeleteMapping("/delete/{id}")
    public AjaxJson deleteUserGroup(@PathVariable String id, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = StpUtil.getLoginIdAsString();
            
            AjaxJson result = userGroupService.deleteUserGroup(id);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "删除用户组", "成功删除用户组ID：" + id);
            } else {
                auditLogService.logUserOperation(currentUser, ip, "删除用户组", "删除用户组失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("删除用户组失败：", e);
            return AjaxJson.error("删除用户组失败");
        }
    }

    /**
     * 批量删除用户组
     */
    @DeleteMapping("/batchDelete")
    public AjaxJson batchDeleteUserGroup(@RequestBody List<String> groupIds, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = StpUtil.getLoginIdAsString();
            
            AjaxJson result = userGroupService.batchDeleteUserGroup(groupIds);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, "批量删除用户组", "成功删除用户组数量：" + groupIds.size());
            } else {
                auditLogService.logUserOperation(currentUser, ip, "批量删除用户组", "批量删除用户组失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("批量删除用户组失败：", e);
            return AjaxJson.error("批量删除用户组失败");
        }
    }

    /**
     * 启用/禁用用户组
     */
    @PutMapping("/status/{id}")
    public AjaxJson updateUserGroupStatus(@PathVariable String id, @RequestParam Integer status, HttpServletRequest req) {
        try {
            String ip = req.getRemoteAddr();
            String currentUser = StpUtil.getLoginIdAsString();
            
            AjaxJson result = userGroupService.updateUserGroupStatus(id, status);
            if (result.getSuccess()) {
                auditLogService.logUserOperation(currentUser, ip, status == 1 ? "启用用户组" : "禁用用户组", "成功" + (status == 1 ? "启用" : "禁用") + "用户组ID：" + id);
            } else {
                auditLogService.logUserOperation(currentUser, ip, status == 1 ? "启用用户组" : "禁用用户组", (status == 1 ? "启用" : "禁用") + "用户组失败：" + result.getMsg());
            }
            return result;
        } catch (Exception e) {
            logger.error("更新用户组状态失败：", e);
            return AjaxJson.error("更新用户组状态失败");
        }
    }
}
