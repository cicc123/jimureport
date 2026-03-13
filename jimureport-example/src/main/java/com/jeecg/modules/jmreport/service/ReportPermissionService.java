package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.dto.ReportPermissionAssignDTO;
import com.jeecg.modules.jmreport.dto.ReportPermissionRevokeDTO;
import com.jeecg.modules.jmreport.entity.ReportPermission;
import com.jeecg.modules.jmreport.enums.ReportPermissionEnum;
import com.jeecg.modules.jmreport.enums.TargetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 报表权限分配服务
 * 提供报表权限的新增、删除、查询功能
 */
@Service
public class ReportPermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportPermissionService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserGroupService userGroupService;
    
    /**
     * 分配报表权限
     * @param dto 权限分配请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermission(ReportPermissionAssignDTO dto) {
        validateAssignRequest(dto);
        
        String username = userGroupService.getCurrentUsername();
        String tenantId = getTenantId();
        
        for (String targetId : dto.getTargetIds()) {
            String checkSql = "SELECT COUNT(*) FROM jimu_report_permission WHERE report_id = ? AND target_type = ? AND target_id = ? AND permission_type = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, 
                    dto.getReportId(), dto.getTargetType(), targetId, dto.getPermissionType());
            
            if (count != null && count > 0) {
                logger.info("权限已存在，跳过: reportId={}, targetType={}, targetId={}, permType={}", 
                        dto.getReportId(), dto.getTargetType(), targetId, dto.getPermissionType());
                continue;
            }
            
            String id = UUID.randomUUID().toString().replace("-", "");
            String sql = "INSERT INTO jimu_report_permission (id, report_id, target_type, target_id, permission_type, tenant_id, create_by, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
            jdbcTemplate.update(sql, id, dto.getReportId(), dto.getTargetType(), targetId, 
                    dto.getPermissionType(), tenantId, username);
        }
    }
    
    /**
     * 撤销报表权限
     * @param dto 权限撤销请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokePermission(ReportPermissionRevokeDTO dto) {
        if (dto.getReportId() == null || dto.getReportId().trim().isEmpty()) {
            throw new IllegalArgumentException("报表ID不能为空");
        }
        if (dto.getTargetIds() == null || dto.getTargetIds().isEmpty()) {
            throw new IllegalArgumentException("目标ID列表不能为空");
        }
        
        StringBuilder sql = new StringBuilder("DELETE FROM jimu_report_permission WHERE report_id = ? AND target_type = ?");
        
        if (dto.getPermissionType() != null && !dto.getPermissionType().trim().isEmpty()) {
            sql.append(" AND permission_type = ?");
        }
        
        sql.append(" AND target_id IN (");
        String placeholders = String.join(",", Collections.nCopies(dto.getTargetIds().size(), "?"));
        sql.append(placeholders).append(")");
        
        List<Object> params = new ArrayList<>();
        params.add(dto.getReportId());
        params.add(dto.getTargetType());
        if (dto.getPermissionType() != null && !dto.getPermissionType().trim().isEmpty()) {
            params.add(dto.getPermissionType());
        }
        params.addAll(dto.getTargetIds());
        
        jdbcTemplate.update(sql.toString(), params.toArray());
    }
    
    /**
     * 获取报表的权限分配列表
     * @param reportId 报表ID
     * @return 权限分配列表
     */
    public List<Map<String, Object>> getReportPermissions(String reportId) {
        String sql = "SELECT p.*, " +
                "CASE WHEN p.target_type = 1 THEN u.username ELSE g.group_name END as target_name, " +
                "CASE WHEN p.target_type = 1 THEN u.real_name ELSE g.description END as target_desc " +
                "FROM jimu_report_permission p " +
                "LEFT JOIN jimu_user u ON p.target_type = 1 AND p.target_id = u.id " +
                "LEFT JOIN jimu_user_group g ON p.target_type = 2 AND p.target_id = g.id " +
                "WHERE p.report_id = ? " +
                "ORDER BY p.target_type, p.create_time";
        return jdbcTemplate.queryForList(sql, reportId);
    }
    
    /**
     * 检查用户是否有报表权限
     * @param reportId 报表ID
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return true-有权限, false-无权限
     */
    public boolean hasReportPermission(String reportId, String userId, String permissionType) {
        if (isAdmin(userId)) {
            return true;
        }
        
        String userPermSql = "SELECT COUNT(*) FROM jimu_report_permission WHERE report_id = ? AND target_type = 1 AND target_id = ? AND permission_type = ?";
        Integer userPermCount = jdbcTemplate.queryForObject(userPermSql, Integer.class, reportId, userId, permissionType);
        if (userPermCount != null && userPermCount > 0) {
            return true;
        }
        
        List<String> groupIds = userGroupService.getUserGroupIds(userId);
        if (!groupIds.isEmpty()) {
            String groupPermSql = "SELECT COUNT(*) FROM jimu_report_permission WHERE report_id = ? AND target_type = 2 AND target_id IN (?) AND permission_type = ?";
            String inClause = String.join("','", groupIds);
            String sql = "SELECT COUNT(*) FROM jimu_report_permission WHERE report_id = ? AND target_type = 2 AND target_id IN ('" + inClause + "') AND permission_type = ?";
            Integer groupPermCount = jdbcTemplate.queryForObject(sql, Integer.class, reportId, permissionType);
            return groupPermCount != null && groupPermCount > 0;
        }
        
        return false;
    }
    
    /**
     * 获取用户可访问的报表ID列表
     * 实现权限优先级: 用户直接权限 > 用户组权限
     * @param userId 用户ID
     * @return 报表ID列表
     */
    public List<String> getAccessibleReportIds(String userId) {
        if (isAdmin(userId)) {
            String sql = "SELECT id FROM jimu_report WHERE del_flag = 0";
            return jdbcTemplate.queryForList(sql, String.class);
        }
        
        Set<String> reportIds = new LinkedHashSet<>();
        
        String userPermSql = "SELECT DISTINCT report_id FROM jimu_report_permission WHERE target_type = 1 AND target_id = ?";
        List<String> userReports = jdbcTemplate.queryForList(userPermSql, String.class, userId);
        reportIds.addAll(userReports);
        
        List<String> groupIds = userGroupService.getUserGroupIds(userId);
        if (!groupIds.isEmpty()) {
            String inClause = String.join("','", groupIds);
            String groupPermSql = "SELECT DISTINCT report_id FROM jimu_report_permission WHERE target_type = 2 AND target_id IN ('" + inClause + "')";
            List<String> groupReports = jdbcTemplate.queryForList(groupPermSql, String.class);
            for (String reportId : groupReports) {
                if (!reportIds.contains(reportId)) {
                    reportIds.add(reportId);
                }
            }
        }
        
        return new ArrayList<>(reportIds);
    }
    
    /**
     * 获取用户可访问的报表详情列表
     * @param userId 用户ID
     * @return 报表详情列表
     */
    public List<Map<String, Object>> getAccessibleReports(String userId) {
        List<String> reportIds = getAccessibleReportIds(userId);
        if (reportIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        String inClause = String.join("','", reportIds);
        String sql = "SELECT id, code, name, create_by, create_time, view_count FROM jimu_report WHERE id IN ('" + inClause + "') AND del_flag = 0 ORDER BY create_time DESC";
        return jdbcTemplate.queryForList(sql);
    }
    
    /**
     * 获取当前用户可访问的报表列表
     * @return 报表列表
     */
    public List<Map<String, Object>> getCurrentUserAccessibleReports() {
        String userId = userGroupService.getCurrentUserId();
        return getAccessibleReports(userId);
    }
    
    /**
     * 批量分配报表权限给用户
     * @param reportIds 报表ID列表
     * @param userIds 用户ID列表
     * @param permissionType 权限类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignToUsers(List<String> reportIds, List<String> userIds, String permissionType) {
        for (String reportId : reportIds) {
            ReportPermissionAssignDTO dto = new ReportPermissionAssignDTO();
            dto.setReportId(reportId);
            dto.setTargetType(TargetTypeEnum.USER.getCode());
            dto.setTargetIds(userIds);
            dto.setPermissionType(permissionType);
            assignPermission(dto);
        }
    }
    
    /**
     * 批量分配报表权限给用户组
     * @param reportIds 报表ID列表
     * @param groupIds 用户组ID列表
     * @param permissionType 权限类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignToGroups(List<String> reportIds, List<String> groupIds, String permissionType) {
        for (String reportId : reportIds) {
            ReportPermissionAssignDTO dto = new ReportPermissionAssignDTO();
            dto.setReportId(reportId);
            dto.setTargetType(TargetTypeEnum.GROUP.getCode());
            dto.setTargetIds(groupIds);
            dto.setPermissionType(permissionType);
            assignPermission(dto);
        }
    }
    
    /**
     * 验证权限分配请求
     */
    private void validateAssignRequest(ReportPermissionAssignDTO dto) {
        if (dto.getReportId() == null || dto.getReportId().trim().isEmpty()) {
            throw new IllegalArgumentException("报表ID不能为空");
        }
        if (dto.getTargetIds() == null || dto.getTargetIds().isEmpty()) {
            throw new IllegalArgumentException("目标ID列表不能为空");
        }
        if (!TargetTypeEnum.isValidCode(dto.getTargetType())) {
            throw new IllegalArgumentException("无效的目标类型: " + dto.getTargetType());
        }
        if (dto.getPermissionType() == null || !ReportPermissionEnum.isValidCode(dto.getPermissionType())) {
            throw new IllegalArgumentException("无效的权限类型: " + dto.getPermissionType());
        }
    }
    
    /**
     * 检查用户是否为管理员
     */
    private boolean isAdmin(String userId) {
        String sql = "SELECT is_admin FROM jimu_user WHERE id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userId);
        if (result.isEmpty()) {
            return false;
        }
        Object isAdminObj = result.get(0).get("is_admin");
        return isAdminObj != null && "1".equals(isAdminObj.toString());
    }
    
    /**
     * 获取当前租户ID
     */
    private String getTenantId() {
        try {
            String username = userGroupService.getCurrentUsername();
            String sql = "SELECT tenant_id FROM jimu_user WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, String.class, username);
        } catch (Exception e) {
            return "1";
        }
    }
}
