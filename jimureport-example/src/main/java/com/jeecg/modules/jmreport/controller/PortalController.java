package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.common.Result;
import com.jeecg.modules.jmreport.service.PortalService;
import com.jeecg.modules.jmreport.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Portal门户控制器
 * 根据用户类型返回不同的Portal页数据
 */
@RestController
@RequestMapping("/api/portal")
public class PortalController {
    
    private static final Logger logger = LoggerFactory.getLogger(PortalController.class);
    
    @Autowired
    private PortalService portalService;
    
    @Autowired
    private UserGroupService userGroupService;
    
    /**
     * 获取Portal页数据
     * 根据用户类型返回不同的数据
     * @return Portal页数据
     */
    @GetMapping("/data")
    public Result<Map<String, Object>> getPortalData() {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            Map<String, Object> data = portalService.getPortalData();
            return Result.success(data);
        } catch (Exception e) {
            logger.error("获取Portal数据失败: {}", e.getMessage(), e);
            return Result.error("获取Portal数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public Result<Map<String, Object>> getCurrentUserInfo() {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            String username = StpUtil.getLoginIdAsString();
            Map<String, Object> userInfo = portalService.getUserInfo(username);
            return Result.success(userInfo);
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage(), e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理员统计数据
     * @return 统计数据
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getAdminStatistics() {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可查看");
        }
        
        try {
            Map<String, Object> stats = portalService.getAdminStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取统计数据失败: {}", e.getMessage(), e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有报表列表（管理员用）
     * @return 报表列表
     */
    @GetMapping("/admin/reports")
    public Result<List<Map<String, Object>>> getAllReports() {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            List<Map<String, Object>> reports = portalService.getAllReports();
            return Result.success(reports);
        } catch (Exception e) {
            logger.error("获取报表列表失败: {}", e.getMessage(), e);
            return Result.error("获取报表列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户可访问的报表列表
     * @return 报表列表
     */
    @GetMapping("/reports/accessible")
    public Result<List<Map<String, Object>>> getAccessibleReports() {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            List<Map<String, Object>> reports = portalService.getAllReports();
            
            if (!isAdmin()) {
                String userId = userGroupService.getCurrentUserId();
                reports = portalService.getAccessibleReports(userId);
            }
            
            return Result.success(reports);
        } catch (Exception e) {
            logger.error("获取可访问报表失败: {}", e.getMessage(), e);
            return Result.error("获取可访问报表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取报表详情
     * @param reportId 报表ID
     * @return 报表详情
     */
    @GetMapping("/report/{reportId}")
    public Result<Map<String, Object>> getReportDetail(@PathVariable String reportId) {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            Map<String, Object> report = portalService.getReportDetail(reportId);
            if (report == null) {
                return Result.notFound("报表不存在");
            }
            
            return Result.success(report);
        } catch (Exception e) {
            logger.error("获取报表详情失败: {}", e.getMessage(), e);
            return Result.error("获取报表详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索报表
     * @param keyword 关键词
     * @return 报表列表
     */
    @GetMapping("/reports/search")
    public Result<List<Map<String, Object>>> searchReports(@RequestParam String keyword) {
        try {
            if (!StpUtil.isLogin()) {
                return Result.unauthorized("请先登录");
            }
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.badRequest("搜索关键词不能为空");
            }
            
            List<Map<String, Object>> reports = portalService.searchReports(keyword.trim());
            return Result.success(reports);
        } catch (Exception e) {
            logger.error("搜索报表失败: {}", e.getMessage(), e);
            return Result.error("搜索报表失败: " + e.getMessage());
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
