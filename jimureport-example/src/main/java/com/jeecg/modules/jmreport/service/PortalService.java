package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Portal门户服务
 * 根据用户类型返回不同的Portal页数据
 */
@Service
public class PortalService {
    
    private static final Logger logger = LoggerFactory.getLogger(PortalService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserGroupService userGroupService;
    
    @Autowired
    private ReportPermissionService reportPermissionService;
    
    /**
     * 获取Portal页数据
     * @return Portal页数据
     */
    public Map<String, Object> getPortalData() {
        String username = StpUtil.getLoginIdAsString();
        return getPortalData(username);
    }
    
    /**
     * 获取Portal页数据
     * @param username 用户名
     * @return Portal页数据
     */
    public Map<String, Object> getPortalData(String username) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> userInfo = getUserInfo(username);
        result.put("user", userInfo);
        
        boolean isAdmin = userGroupService.isAdmin(username);
        result.put("isAdmin", isAdmin);
        
        List<Map<String, Object>> reports;
        if (isAdmin) {
            reports = getAllReports();
        } else {
            String userId = (String) userInfo.get("id");
            reports = reportPermissionService.getAccessibleReports(userId);
        }
        result.put("reports", reports);
        result.put("reportCount", reports.size());
        
        if (isAdmin) {
            result.put("userCount", getUserCount());
            result.put("groupCount", getGroupCount());
        }
        
        return result;
    }
    
    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    public Map<String, Object> getUserInfo(String username) {
        String sql = "SELECT id, username, real_name, is_admin, tenant_id, status FROM jimu_user WHERE username = ?";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, username);
        if (users.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Object> userInfo = new HashMap<>(users.get(0));
        
        String userId = (String) userInfo.get("id");
        List<Map<String, Object>> groups = userGroupService.getUserGroups(userId)
                .stream()
                .map(group -> {
                    Map<String, Object> g = new HashMap<>();
                    g.put("id", group.getId());
                    g.put("name", group.getGroupName());
                    g.put("code", group.getGroupCode());
                    g.put("type", group.getGroupType());
                    return g;
                })
                .toList();
        userInfo.put("groups", groups);
        
        return userInfo;
    }
    
    /**
     * 获取所有报表（管理员用）
     * @return 报表列表
     */
    public List<Map<String, Object>> getAllReports() {
        String sql = "SELECT id, code, name, create_by, create_time, update_time, view_count, " +
                "(SELECT COUNT(*) FROM jimu_report_permission rp WHERE rp.report_id = jimu_report.id) as permission_count " +
                "FROM jimu_report WHERE del_flag = 0 ORDER BY create_time DESC";
        return jdbcTemplate.queryForList(sql);
    }
    
    /**
     * 获取报表详情
     * @param reportId 报表ID
     * @return 报表详情
     */
    public Map<String, Object> getReportDetail(String reportId) {
        String sql = "SELECT * FROM jimu_report WHERE id = ? AND del_flag = 0";
        List<Map<String, Object>> reports = jdbcTemplate.queryForList(sql, reportId);
        if (reports.isEmpty()) {
            return null;
        }
        
        Map<String, Object> report = new HashMap<>(reports.get(0));
        
        List<Map<String, Object>> permissions = reportPermissionService.getReportPermissions(reportId);
        report.put("permissions", permissions);
        
        return report;
    }
    
    /**
     * 获取用户数量
     * @return 用户数量
     */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM jimu_user WHERE status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * 获取用户组数量
     * @return 用户组数量
     */
    public int getGroupCount() {
        String sql = "SELECT COUNT(*) FROM jimu_user_group WHERE status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * 获取报表数量
     * @return 报表数量
     */
    public int getReportCount() {
        String sql = "SELECT COUNT(*) FROM jimu_report WHERE del_flag = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
    
    /**
     * 获取管理员Portal统计数据
     * @return 统计数据
     */
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", getUserCount());
        stats.put("groupCount", getGroupCount());
        stats.put("reportCount", getReportCount());
        
        String recentSql = "SELECT id, name, create_by, create_time FROM jimu_report WHERE del_flag = 0 ORDER BY create_time DESC LIMIT 5";
        stats.put("recentReports", jdbcTemplate.queryForList(recentSql));
        
        String popularSql = "SELECT id, name, view_count FROM jimu_report WHERE del_flag = 0 ORDER BY view_count DESC LIMIT 5";
        stats.put("popularReports", jdbcTemplate.queryForList(popularSql));
        
        return stats;
    }
    
    /**
     * 获取用户可访问的报表列表
     * @param userId 用户ID
     * @return 报表列表
     */
    public List<Map<String, Object>> getAccessibleReports(String userId) {
        return reportPermissionService.getAccessibleReports(userId);
    }
    
    /**
     * 搜索报表
     * @param keyword 关键词
     * @return 报表列表
     */
    public List<Map<String, Object>> searchReports(String keyword) {
        String userId = userGroupService.getCurrentUserId();
        boolean isAdmin = userGroupService.isAdmin();
        
        if (isAdmin) {
            String sql = "SELECT id, code, name, create_by, create_time, view_count FROM jimu_report " +
                    "WHERE del_flag = 0 AND (name LIKE ? OR code LIKE ?) ORDER BY create_time DESC";
            String likeKeyword = "%" + keyword + "%";
            return jdbcTemplate.queryForList(sql, likeKeyword, likeKeyword);
        } else {
            List<Map<String, Object>> accessibleReports = reportPermissionService.getAccessibleReports(userId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> report : accessibleReports) {
                String name = (String) report.get("name");
                String code = (String) report.get("code");
                if ((name != null && name.contains(keyword)) || (code != null && code.contains(keyword))) {
                    result.add(report);
                }
            }
            return result;
        }
    }
}
