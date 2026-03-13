package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.common.Result;
import com.jeecg.modules.jmreport.dto.ReportPermissionAssignDTO;
import com.jeecg.modules.jmreport.dto.ReportPermissionRevokeDTO;
import com.jeecg.modules.jmreport.service.ReportPermissionService;
import com.jeecg.modules.jmreport.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表权限分配控制器
 * 仅管理员可调用，实现报表权限的分配与撤销
 */
@RestController
@RequestMapping("/api/permission/report")
public class ReportPermissionController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportPermissionController.class);
    
    @Autowired
    private ReportPermissionService reportPermissionService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    /**
     * 分配报表权限
     * @param dto 权限分配请求
     * @return 操作结果
     */
    @PostMapping("/assign")
    public Result<Void> assignPermission(@RequestBody ReportPermissionAssignDTO dto) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            logger.info("分配报表权限: reportId={}, targetType={}, targetIds={}, permType={}", 
                    dto.getReportId(), dto.getTargetType(), dto.getTargetIds(), dto.getPermissionType());
            reportPermissionService.assignPermission(dto);
            return Result.successMsg("权限分配成功");
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("分配权限失败: {}", e.getMessage(), e);
            return Result.error("分配权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 撤销报表权限
     * @param dto 权限撤销请求
     * @return 操作结果
     */
    @PostMapping("/revoke")
    public Result<Void> revokePermission(@RequestBody ReportPermissionRevokeDTO dto) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            logger.info("撤销报表权限: reportId={}, targetType={}, targetIds={}, permType={}", 
                    dto.getReportId(), dto.getTargetType(), dto.getTargetIds(), dto.getPermissionType());
            reportPermissionService.revokePermission(dto);
            return Result.successMsg("权限撤销成功");
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("撤销权限失败: {}", e.getMessage(), e);
            return Result.error("撤销权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取报表的权限分配列表
     * @param reportId 报表ID
     * @return 权限分配列表
     */
    @GetMapping("/list/{reportId}")
    public Result<List<Map<String, Object>>> getReportPermissions(@PathVariable String reportId) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            List<Map<String, Object>> permissions = reportPermissionService.getReportPermissions(reportId);
            return Result.success(permissions);
        } catch (Exception e) {
            logger.error("获取权限列表失败: {}", e.getMessage(), e);
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量分配报表权限给用户
     * @param reportIds 报表ID列表
     * @param userIds 用户ID列表
     * @param permissionType 权限类型
     * @return 操作结果
     */
    @PostMapping("/batch/assign/users")
    public Result<Void> batchAssignToUsers(
            @RequestParam List<String> reportIds,
            @RequestParam List<String> userIds,
            @RequestParam String permissionType) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            reportPermissionService.batchAssignToUsers(reportIds, userIds, permissionType);
            return Result.successMsg("批量分配权限成功");
        } catch (Exception e) {
            logger.error("批量分配权限失败: {}", e.getMessage(), e);
            return Result.error("批量分配权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量分配报表权限给用户组
     * @param reportIds 报表ID列表
     * @param groupIds 用户组ID列表
     * @param permissionType 权限类型
     * @return 操作结果
     */
    @PostMapping("/batch/assign/groups")
    public Result<Void> batchAssignToGroups(
            @RequestParam List<String> reportIds,
            @RequestParam List<String> groupIds,
            @RequestParam String permissionType) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            reportPermissionService.batchAssignToGroups(reportIds, groupIds, permissionType);
            return Result.successMsg("批量分配权限成功");
        } catch (Exception e) {
            logger.error("批量分配权限失败: {}", e.getMessage(), e);
            return Result.error("批量分配权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查当前用户是否有报表权限
     * @param reportId 报表ID
     * @param permissionType 权限类型
     * @return 权限检查结果
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkPermission(
            @RequestParam String reportId,
            @RequestParam(defaultValue = "view") String permissionType) {
        try {
            String userId = userGroupService.getCurrentUserId();
            boolean hasPermission = reportPermissionService.hasReportPermission(reportId, userId, permissionType);
            return Result.success(Map.of("hasPermission", hasPermission));
        } catch (Exception e) {
            logger.error("检查权限失败: {}", e.getMessage(), e);
            return Result.error("检查权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户可访问的报表列表
     * @return 报表列表
     */
    @GetMapping("/accessible")
    public Result<List<Map<String, Object>>> getAccessibleReports() {
        try {
            List<Map<String, Object>> reports = reportPermissionService.getCurrentUserAccessibleReports();
            return Result.success(reports);
        } catch (Exception e) {
            logger.error("获取可访问报表失败: {}", e.getMessage(), e);
            return Result.error("获取可访问报表失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     */
    private boolean isAdmin() {
        try {
            return userGroupService.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }
}
