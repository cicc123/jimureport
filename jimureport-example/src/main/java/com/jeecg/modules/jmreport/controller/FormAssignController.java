package com.jeecg.modules.jmreport.controller;

import com.jeecg.modules.jmreport.service.AuditLogService;
import com.jeecg.modules.jmreport.service.FormAssignService;
import com.jeecg.modules.jmreport.service.PermissionService;
import com.jeecg.modules.jmreport.satoken.exception.AjaxJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/formAssign")
public class FormAssignController {

    @Autowired
    private FormAssignService formAssignService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 跳转到表单分配页面
     */
    @GetMapping("/index")
    public String index() {
        // 检查权限
        if (!permissionService.hasPermission("form:assign")) {
            return "error/403";
        }
        return "formAssign/index";
    }

    /**
     * 分配表单给用户
     */
    @PostMapping("/assignToUser")
    @ResponseBody
    public AjaxJson assignToUser(@RequestParam String formId, @RequestParam String userId, @RequestParam String perms, jakarta.servlet.http.HttpServletRequest req) {
        try {
            formAssignService.assignFormToUser(formId, userId, perms);
            // 记录审计日志
            auditLogService.logFormOperation(formId, "分配表单权限", "分配给用户：" + userId + "，权限：" + perms, req.getRemoteAddr());
            return AjaxJson.success("分配成功");
        } catch (Exception e) {
            return AjaxJson.error("分配失败：" + e.getMessage());
        }
    }

    /**
     * 分配表单给角色
     */
    @PostMapping("/assignToRole")
    @ResponseBody
    public AjaxJson assignToRole(@RequestParam String formId, @RequestParam String roleId, @RequestParam String perms, jakarta.servlet.http.HttpServletRequest req) {
        try {
            formAssignService.assignFormToRole(formId, roleId, perms);
            // 记录审计日志
            auditLogService.logFormOperation(formId, "分配表单权限", "分配给角色：" + roleId + "，权限：" + perms, req.getRemoteAddr());
            return AjaxJson.success("分配成功");
        } catch (Exception e) {
            return AjaxJson.error("分配失败：" + e.getMessage());
        }
    }

    /**
     * 批量分配表单给用户
     */
    @PostMapping("/batchAssignToUsers")
    @ResponseBody
    public AjaxJson batchAssignToUsers(@RequestParam String formId, @RequestParam List<String> userIds, @RequestParam String perms, jakarta.servlet.http.HttpServletRequest req) {
        try {
            formAssignService.batchAssignFormToUsers(formId, userIds, perms);
            // 记录审计日志
            auditLogService.logFormOperation(formId, "批量分配表单权限", "批量分配给用户：" + userIds.size() + "个，权限：" + perms, req.getRemoteAddr());
            return AjaxJson.success("批量分配成功");
        } catch (Exception e) {
            return AjaxJson.error("批量分配失败：" + e.getMessage());
        }
    }

    /**
     * 批量分配表单给角色
     */
    @PostMapping("/batchAssignToRoles")
    @ResponseBody
    public AjaxJson batchAssignToRoles(@RequestParam String formId, @RequestParam List<String> roleIds, @RequestParam String perms, jakarta.servlet.http.HttpServletRequest req) {
        try {
            formAssignService.batchAssignFormToRoles(formId, roleIds, perms);
            // 记录审计日志
            auditLogService.logFormOperation(formId, "批量分配表单权限", "批量分配给角色：" + roleIds.size() + "个，权限：" + perms, req.getRemoteAddr());
            return AjaxJson.success("批量分配成功");
        } catch (Exception e) {
            return AjaxJson.error("批量分配失败：" + e.getMessage());
        }
    }

    /**
     * 撤销表单权限
     */
    @PostMapping("/revokePermission")
    @ResponseBody
    public AjaxJson revokePermission(@RequestParam String formId, @RequestParam(required = false) String userId, @RequestParam(required = false) String roleId, jakarta.servlet.http.HttpServletRequest req) {
        try {
            formAssignService.revokeFormPermission(formId, userId, roleId);
            // 记录审计日志
            if (userId != null) {
                auditLogService.logFormOperation(formId, "撤销表单权限", "从用户：" + userId + "撤销权限", req.getRemoteAddr());
            } else if (roleId != null) {
                auditLogService.logFormOperation(formId, "撤销表单权限", "从角色：" + roleId + "撤销权限", req.getRemoteAddr());
            }
            return AjaxJson.success("撤销成功");
        } catch (Exception e) {
            return AjaxJson.error("撤销失败：" + e.getMessage());
        }
    }

    /**
     * 获取表单的分配记录
     */
    @GetMapping("/getAssignments")
    @ResponseBody
    public AjaxJson getAssignments(@RequestParam String formId) {
        try {
            List<Map<String, Object>> assignments = formAssignService.getFormAssignments(formId);
            return AjaxJson.success().put("data", assignments);
        } catch (Exception e) {
            return AjaxJson.error("获取分配记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的表单权限
     */
    @GetMapping("/getUserFormPermissions")
    @ResponseBody
    public AjaxJson getUserFormPermissions(@RequestParam String userId) {
        try {
            List<Map<String, Object>> permissions = formAssignService.getUserFormPermissions(userId);
            return AjaxJson.success().put("data", permissions);
        } catch (Exception e) {
            return AjaxJson.error("获取用户表单权限失败：" + e.getMessage());
        }
    }

    /**
     * 获取角色的表单权限
     */
    @GetMapping("/getRoleFormPermissions")
    @ResponseBody
    public AjaxJson getRoleFormPermissions(@RequestParam String roleId) {
        try {
            List<Map<String, Object>> permissions = formAssignService.getRoleFormPermissions(roleId);
            return AjaxJson.success().put("data", permissions);
        } catch (Exception e) {
            return AjaxJson.error("获取角色表单权限失败：" + e.getMessage());
        }
    }
}
