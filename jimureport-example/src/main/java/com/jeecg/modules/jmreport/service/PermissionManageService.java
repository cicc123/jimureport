package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.entity.DataPermission;
import com.jeecg.modules.jmreport.entity.Resource;
import com.jeecg.modules.jmreport.entity.ResourcePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionManageService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionManageService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditLogService auditLogService;

    public List<Resource> getResourceTree(String tenantId) {
        String sql = "SELECT * FROM jimu_resource WHERE tenant_id = ? OR tenant_id IS NULL ORDER BY sort_order";
        List<Resource> resources = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Resource.class), tenantId);
        return buildResourceTree(resources, null);
    }

    private List<Resource> buildResourceTree(List<Resource> allResources, String parentId) {
        return allResources.stream()
                .filter(r -> Objects.equals(r.getParentId(), parentId))
                .peek(r -> {
                })
                .collect(Collectors.toList());
    }

    public List<Resource> getAccessibleResources(String username) {
        if (isAdmin(username)) {
            return getResourceTree(getCurrentTenantId());
        }

        String userId = getUserId(username);
        List<String> roleIds = getUserRoleIds(userId);
        List<String> groupIds = getUserGroupIds(userId);

        String sql = "SELECT DISTINCT r.* FROM jimu_resource r " +
                "LEFT JOIN jimu_resource_permission rp ON r.id = rp.resource_id " +
                "WHERE (rp.target_type = 1 AND rp.target_id = ?) " +
                (roleIds.isEmpty() ? "" : "OR (rp.target_type = 2 AND rp.target_id IN (" + String.join(",", Collections.nCopies(roleIds.size(), "?")) + ")) ") +
                (groupIds.isEmpty() ? "" : "OR (rp.target_type = 3 AND rp.target_id IN (" + String.join(",", Collections.nCopies(groupIds.size(), "?")) + ")) ") +
                "ORDER BY r.sort_order";

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.addAll(roleIds);
        params.addAll(groupIds);

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Resource.class), params.toArray());
    }

    @Transactional
    public void assignResourcePermission(String resourceId, int targetType, String targetId, int permissionBits, String ip) {
        String sql = "SELECT id FROM jimu_resource_permission WHERE resource_id = ? AND target_type = ? AND target_id = ?";
        List<Map<String, Object>> existing = jdbcTemplate.queryForList(sql, resourceId, targetType, targetId);

        String username = getCurrentUsername();
        String tenantId = getCurrentTenantId();

        if (existing.isEmpty()) {
            String insertSql = "INSERT INTO jimu_resource_permission (id, resource_id, target_type, target_id, permission_bits, tenant_id, create_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, UUID.randomUUID().toString(), resourceId, targetType, targetId, permissionBits, tenantId, username);
        } else {
            String updateSql = "UPDATE jimu_resource_permission SET permission_bits = ?, update_by = ? WHERE resource_id = ? AND target_type = ? AND target_id = ?";
            jdbcTemplate.update(updateSql, permissionBits, username, resourceId, targetType, targetId);
        }

        String targetName = getTargetName(targetType, targetId);
        String resourceName = getResourceName(resourceId);
        auditLogService.logFormOperation(resourceId, "分配资源权限", 
                String.format("资源: %s, 目标: %s, 权限位: %d", resourceName, targetName, permissionBits), ip);
    }

    @Transactional
    public void batchAssignResourcePermission(List<String> resourceIds, int targetType, String targetId, int permissionBits, String ip) {
        for (String resourceId : resourceIds) {
            assignResourcePermission(resourceId, targetType, targetId, permissionBits, ip);
        }
    }

    @Transactional
    public void revokeResourcePermission(String resourceId, int targetType, String targetId, String ip) {
        String sql = "DELETE FROM jimu_resource_permission WHERE resource_id = ? AND target_type = ? AND target_id = ?";
        jdbcTemplate.update(sql, resourceId, targetType, targetId);

        String targetName = getTargetName(targetType, targetId);
        String resourceName = getResourceName(resourceId);
        auditLogService.logFormOperation(resourceId, "撤销资源权限", 
                String.format("资源: %s, 目标: %s", resourceName, targetName), ip);
    }

    public boolean checkResourcePermission(String resourceId, String username, int requiredPerm) {
        if (isAdmin(username)) {
            return true;
        }

        String userId = getUserId(username);
        List<String> roleIds = getUserRoleIds(userId);
        List<String> groupIds = getUserGroupIds(userId);

        String sql = "SELECT permission_bits FROM jimu_resource_permission WHERE resource_id = ? AND " +
                "((target_type = 1 AND target_id = ?) " +
                (roleIds.isEmpty() ? "" : "OR (target_type = 2 AND target_id IN (" + String.join(",", Collections.nCopies(roleIds.size(), "?")) + ")) ") +
                (groupIds.isEmpty() ? "" : "OR (target_type = 3 AND target_id IN (" + String.join(",", Collections.nCopies(groupIds.size(), "?")) + "))") +
                ")";

        List<Object> params = new ArrayList<>();
        params.add(resourceId);
        params.add(userId);
        params.addAll(roleIds);
        params.addAll(groupIds);

        List<Integer> permissionBits = jdbcTemplate.queryForList(sql, params.toArray(), Integer.class);
        return permissionBits.stream().anyMatch(bits -> (bits & requiredPerm) == requiredPerm);
    }

    public int getUserResourcePermissionBits(String resourceId, String username) {
        if (isAdmin(username)) {
            return ResourcePermission.PERM_ALL;
        }

        String userId = getUserId(username);
        List<String> roleIds = getUserRoleIds(userId);
        List<String> groupIds = getUserGroupIds(userId);

        String sql = "SELECT permission_bits FROM jimu_resource_permission WHERE resource_id = ? AND " +
                "((target_type = 1 AND target_id = ?) " +
                (roleIds.isEmpty() ? "" : "OR (target_type = 2 AND target_id IN (" + String.join(",", Collections.nCopies(roleIds.size(), "?")) + ")) ") +
                (groupIds.isEmpty() ? "" : "OR (target_type = 3 AND target_id IN (" + String.join(",", Collections.nCopies(groupIds.size(), "?")) + "))") +
                ")";

        List<Object> params = new ArrayList<>();
        params.add(resourceId);
        params.add(userId);
        params.addAll(roleIds);
        params.addAll(groupIds);

        List<Integer> permissionBits = jdbcTemplate.queryForList(sql, params.toArray(), Integer.class);
        return permissionBits.stream().mapToInt(Integer::intValue).reduce(0, (a, b) -> a | b);
    }

    @Transactional
    public void setDataPermission(String resourceId, int targetType, String targetId, int filterType, String filterRule, String ip) {
        String sql = "DELETE FROM jimu_data_permission WHERE resource_id = ? AND target_type = ? AND target_id = ?";
        jdbcTemplate.update(sql, resourceId, targetType, targetId);

        String insertSql = "INSERT INTO jimu_data_permission (id, resource_id, target_type, target_id, filter_type, filter_rule, tenant_id, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String username = getCurrentUsername();
        String tenantId = getCurrentTenantId();
        jdbcTemplate.update(insertSql, UUID.randomUUID().toString(), resourceId, targetType, targetId, filterType, filterRule, tenantId, username);

        String targetName = getTargetName(targetType, targetId);
        String resourceName = getResourceName(resourceId);
        auditLogService.logFormOperation(resourceId, "设置数据权限", 
                String.format("资源: %s, 目标: %s, 过滤类型: %d", resourceName, targetName, filterType), ip);
    }

    public String buildDataFilterSql(String resourceId, String username) {
        if (isAdmin(username)) {
            return null;
        }

        String userId = getUserId(username);
        List<String> roleIds = getUserRoleIds(userId);
        List<String> groupIds = getUserGroupIds(userId);

        String sql = "SELECT filter_type, filter_rule FROM jimu_data_permission WHERE resource_id = ? AND " +
                "((target_type = 1 AND target_id = ?) " +
                (roleIds.isEmpty() ? "" : "OR (target_type = 2 AND target_id IN (" + String.join(",", Collections.nCopies(roleIds.size(), "?")) + ")) ") +
                (groupIds.isEmpty() ? "" : "OR (target_type = 3 AND target_id IN (" + String.join(",", Collections.nCopies(groupIds.size(), "?")) + "))") +
                ")";

        List<Object> params = new ArrayList<>();
        params.add(resourceId);
        params.add(userId);
        params.addAll(roleIds);
        params.addAll(groupIds);

        List<Map<String, Object>> permissions = jdbcTemplate.queryForList(sql, params.toArray());
        if (permissions.isEmpty()) {
            return "1 = 0";
        }

        List<String> conditions = new ArrayList<>();
        for (Map<String, Object> perm : permissions) {
            int filterType = (Integer) perm.get("filter_type");
            String filterRule = (String) perm.get("filter_rule");

            switch (filterType) {
                case DataPermission.FILTER_TYPE_USER:
                    conditions.add(filterRule.replace("${username}", username).replace("${userId}", userId));
                    break;
                case DataPermission.FILTER_TYPE_DEPT:
                    String deptId = getUserDeptId(userId);
                    conditions.add(filterRule.replace("${deptId}", deptId != null ? deptId : ""));
                    break;
                case DataPermission.FILTER_TYPE_CUSTOM:
                case DataPermission.FILTER_TYPE_SQL:
                    conditions.add(filterRule);
                    break;
            }
        }

        return conditions.isEmpty() ? null : "(" + String.join(" OR ", conditions) + ")";
    }

    public List<Map<String, Object>> getResourcePermissions(String resourceId) {
        String sql = "SELECT rp.*, " +
                "CASE rp.target_type " +
                "  WHEN 1 THEN (SELECT username FROM jimu_user WHERE id = rp.target_id) " +
                "  WHEN 2 THEN (SELECT role_name FROM jimu_role WHERE id = rp.target_id) " +
                "  WHEN 3 THEN (SELECT group_name FROM jimu_user_group WHERE id = rp.target_id) " +
                "END AS target_name " +
                "FROM jimu_resource_permission rp WHERE rp.resource_id = ?";
        return jdbcTemplate.queryForList(sql, resourceId);
    }

    public List<Map<String, Object>> getDataPermissions(String resourceId) {
        String sql = "SELECT dp.*, " +
                "CASE dp.target_type " +
                "  WHEN 1 THEN (SELECT username FROM jimu_user WHERE id = dp.target_id) " +
                "  WHEN 2 THEN (SELECT role_name FROM jimu_role WHERE id = dp.target_id) " +
                "  WHEN 3 THEN (SELECT group_name FROM jimu_user_group WHERE id = dp.target_id) " +
                "END AS target_name " +
                "FROM jimu_data_permission dp WHERE dp.resource_id = ?";
        return jdbcTemplate.queryForList(sql, resourceId);
    }

    private boolean isAdmin(String username) {
        try {
            String sql = "SELECT is_admin FROM jimu_user WHERE username = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, username);
            return user != null && "1".equals(user.get("is_admin").toString());
        } catch (Exception e) {
            return false;
        }
    }

    private String getUserId(String username) {
        String sql = "SELECT id FROM jimu_user WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, String.class, username);
    }

    private List<String> getUserRoleIds(String userId) {
        String sql = "SELECT role_id FROM jimu_user_role WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    private List<String> getUserGroupIds(String userId) {
        String sql = "SELECT group_id FROM jimu_user_group_member WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    private String getUserDeptId(String userId) {
        String sql = "SELECT dept_id FROM jimu_user WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsString() : "system";
        } catch (Exception e) {
            return "system";
        }
    }

    private String getCurrentTenantId() {
        try {
            String username = getCurrentUsername();
            String sql = "SELECT tenant_id FROM jimu_user WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, String.class, username);
        } catch (Exception e) {
            return "1";
        }
    }

    private String getTargetName(int targetType, String targetId) {
        try {
            String sql = "";
            switch (targetType) {
                case 1:
                    sql = "SELECT username FROM jimu_user WHERE id = ?";
                    break;
                case 2:
                    sql = "SELECT role_name FROM jimu_role WHERE id = ?";
                    break;
                case 3:
                    sql = "SELECT group_name FROM jimu_user_group WHERE id = ?";
                    break;
            }
            return jdbcTemplate.queryForObject(sql, String.class, targetId);
        } catch (Exception e) {
            return targetId;
        }
    }

    private String getResourceName(String resourceId) {
        try {
            String sql = "SELECT resource_name FROM jimu_resource WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, resourceId);
        } catch (Exception e) {
            return resourceId;
        }
    }
}
