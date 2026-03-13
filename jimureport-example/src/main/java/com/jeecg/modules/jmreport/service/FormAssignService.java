package com.jeecg.modules.jmreport.service;

import com.jeecg.modules.jmreport.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FormAssignService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 分配表单权限给用户
     */
    public void assignFormToUser(String formId, String userId, String perms) {
        String tenantId = TenantContext.getTenantId();
        
        // 检查是否已存在分配记录
        String checkSql = "SELECT id FROM jimu_form_permission WHERE form_id = ? AND user_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, formId, userId);
        
        if (existing.isEmpty()) {
            // 新增分配记录
            String insertSql = "INSERT INTO jimu_form_permission (id, form_id, user_id, tenant_id, perms) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, UUID.randomUUID().toString(), formId, userId, tenantId, perms);
        } else {
            // 更新现有记录
            String updateSql = "UPDATE jimu_form_permission SET perms = ? WHERE form_id = ? AND user_id = ?";
            jdbcTemplate.update(updateSql, perms, formId, userId);
        }
    }

    /**
     * 分配表单权限给角色
     */
    public void assignFormToRole(String formId, String roleId, String perms) {
        String tenantId = TenantContext.getTenantId();
        
        // 检查是否已存在分配记录
        String checkSql = "SELECT id FROM jimu_form_permission WHERE form_id = ? AND role_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, formId, roleId);
        
        if (existing.isEmpty()) {
            // 新增分配记录
            String insertSql = "INSERT INTO jimu_form_permission (id, form_id, role_id, tenant_id, perms) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, UUID.randomUUID().toString(), formId, roleId, tenantId, perms);
        } else {
            // 更新现有记录
            String updateSql = "UPDATE jimu_form_permission SET perms = ? WHERE form_id = ? AND role_id = ?";
            jdbcTemplate.update(updateSql, perms, formId, roleId);
        }
    }

    /**
     * 批量分配表单权限给用户
     */
    public void batchAssignFormToUsers(String formId, List<String> userIds, String perms) {
        for (String userId : userIds) {
            assignFormToUser(formId, userId, perms);
        }
    }

    /**
     * 批量分配表单权限给角色
     */
    public void batchAssignFormToRoles(String formId, List<String> roleIds, String perms) {
        for (String roleId : roleIds) {
            assignFormToRole(formId, roleId, perms);
        }
    }

    /**
     * 撤销表单权限
     */
    public void revokeFormPermission(String formId, String userId, String roleId) {
        String sql = "DELETE FROM jimu_form_permission WHERE form_id = ?";
        
        if (userId != null) {
            sql += " AND user_id = ?";
            jdbcTemplate.update(sql, formId, userId);
        } else if (roleId != null) {
            sql += " AND role_id = ?";
            jdbcTemplate.update(sql, formId, roleId);
        }
    }

    /**
     * 获取表单的分配记录
     */
    public List<Map<String, Object>> getFormAssignments(String formId) {
        String sql = "" +
                "SELECT fp.id, fp.form_id, fp.user_id, fp.role_id, fp.perms, fp.create_time, " +
                "u.username, u.real_name, " +
                "r.role_name, r.role_code " +
                "FROM jimu_form_permission fp " +
                "LEFT JOIN jimu_user u ON fp.user_id = u.id " +
                "LEFT JOIN jimu_role r ON fp.role_id = r.id " +
                "WHERE fp.form_id = ?";
        
        return jdbcTemplate.queryForList(sql, formId);
    }

    /**
     * 获取用户的表单权限
     */
    public List<Map<String, Object>> getUserFormPermissions(String userId) {
        String sql = "" +
                "SELECT fp.form_id, fp.perms " +
                "FROM jimu_form_permission fp " +
                "WHERE fp.user_id = ?";
        
        return jdbcTemplate.queryForList(sql, userId);
    }

    /**
     * 获取角色的表单权限
     */
    public List<Map<String, Object>> getRoleFormPermissions(String roleId) {
        String sql = "" +
                "SELECT fp.form_id, fp.perms " +
                "FROM jimu_form_permission fp " +
                "WHERE fp.role_id = ?";
        
        return jdbcTemplate.queryForList(sql, roleId);
    }
}
