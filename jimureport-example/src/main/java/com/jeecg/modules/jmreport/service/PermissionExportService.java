package com.jeecg.modules.jmreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PermissionExportService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionExportService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String exportPermissions(String tenantId) throws Exception {
        Map<String, Object> exportData = new LinkedHashMap<>();

        exportData.put("roles", exportRoles(tenantId));
        exportData.put("permissions", exportAllPermissions());
        exportData.put("rolePermissions", exportRolePermissions(tenantId));
        exportData.put("userGroups", exportUserGroups(tenantId));
        exportData.put("resources", exportResources(tenantId));
        exportData.put("resourcePermissions", exportResourcePermissions(tenantId));
        exportData.put("dataPermissions", exportDataPermissions(tenantId));
        exportData.put("exportTime", new Date());
        exportData.put("tenantId", tenantId);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
    }

    private List<Map<String, Object>> exportRoles(String tenantId) {
        String sql = "SELECT id, role_name, role_code, description, sort_order, status FROM jimu_role WHERE tenant_id = ? OR tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    private List<Map<String, Object>> exportAllPermissions() {
        String sql = "SELECT id, perm_code, perm_name, perm_type, parent_id, sort_order, description FROM jimu_permission";
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> exportRolePermissions(String tenantId) {
        String sql = "SELECT rp.role_id, rp.perm_id FROM jimu_role_permission rp " +
                "JOIN jimu_role r ON rp.role_id = r.id WHERE r.tenant_id = ? OR r.tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    private List<Map<String, Object>> exportUserGroups(String tenantId) {
        String sql = "SELECT id, group_name, group_code, group_type, parent_id, description, sort_order, status FROM jimu_user_group WHERE tenant_id = ? OR tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    private List<Map<String, Object>> exportResources(String tenantId) {
        String sql = "SELECT id, resource_name, resource_code, resource_type, parent_id, path, icon, sort_order, status, description FROM jimu_resource WHERE tenant_id = ? OR tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    private List<Map<String, Object>> exportResourcePermissions(String tenantId) {
        String sql = "SELECT resource_id, target_type, target_id, permission_bits FROM jimu_resource_permission WHERE tenant_id = ? OR tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    private List<Map<String, Object>> exportDataPermissions(String tenantId) {
        String sql = "SELECT resource_id, target_type, target_id, filter_type, filter_rule FROM jimu_data_permission WHERE tenant_id = ? OR tenant_id IS NULL";
        return jdbcTemplate.queryForList(sql, tenantId);
    }

    @Transactional
    public void importPermissions(String jsonData, String tenantId, String ip) throws Exception {
        Map<String, Object> importData = objectMapper.readValue(jsonData, Map.class);

        if (importData.containsKey("roles")) {
            importRoles((List<Map<String, Object>>) importData.get("roles"), tenantId);
        }

        if (importData.containsKey("permissions")) {
            importPermissions((List<Map<String, Object>>) importData.get("permissions"));
        }

        if (importData.containsKey("rolePermissions")) {
            importRolePermissions((List<Map<String, Object>>) importData.get("rolePermissions"));
        }

        if (importData.containsKey("userGroups")) {
            importUserGroups((List<Map<String, Object>>) importData.get("userGroups"), tenantId);
        }

        if (importData.containsKey("resources")) {
            importResources((List<Map<String, Object>>) importData.get("resources"), tenantId);
        }

        if (importData.containsKey("resourcePermissions")) {
            importResourcePermissions((List<Map<String, Object>>) importData.get("resourcePermissions"), tenantId);
        }

        if (importData.containsKey("dataPermissions")) {
            importDataPermissions((List<Map<String, Object>>) importData.get("dataPermissions"), tenantId);
        }

        auditLogService.logFormOperation("system", "导入权限配置", 
                String.format("租户: %s, 导入数据项: %d", tenantId, importData.size()), ip);
    }

    private void importRoles(List<Map<String, Object>> roles, String tenantId) {
        for (Map<String, Object> role : roles) {
            String sql = "INSERT IGNORE INTO jimu_role (id, role_name, role_code, tenant_id, description, sort_order, status, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, 'import')";
            jdbcTemplate.update(sql, 
                    role.get("id"), role.get("role_name"), role.get("role_code"),
                    tenantId, role.get("description"), role.get("sort_order"), role.get("status"));
        }
    }

    private void importPermissions(List<Map<String, Object>> permissions) {
        for (Map<String, Object> perm : permissions) {
            String sql = "INSERT IGNORE INTO jimu_permission (id, perm_code, perm_name, perm_type, parent_id, sort_order, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                    perm.get("id"), perm.get("perm_code"), perm.get("perm_name"),
                    perm.get("perm_type"), perm.get("parent_id"), perm.get("sort_order"), perm.get("description"));
        }
    }

    private void importRolePermissions(List<Map<String, Object>> rolePerms) {
        for (Map<String, Object> rp : rolePerms) {
            String sql = "INSERT IGNORE INTO jimu_role_permission (id, role_id, perm_id, create_by) VALUES (?, ?, ?, 'import')";
            jdbcTemplate.update(sql, UUID.randomUUID().toString(), rp.get("role_id"), rp.get("perm_id"));
        }
    }

    private void importUserGroups(List<Map<String, Object>> groups, String tenantId) {
        for (Map<String, Object> group : groups) {
            String sql = "INSERT IGNORE INTO jimu_user_group (id, group_name, group_code, group_type, parent_id, tenant_id, description, sort_order, status, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'import')";
            jdbcTemplate.update(sql,
                    group.get("id"), group.get("group_name"), group.get("group_code"),
                    group.get("group_type"), group.get("parent_id"), tenantId,
                    group.get("description"), group.get("sort_order"), group.get("status"));
        }
    }

    private void importResources(List<Map<String, Object>> resources, String tenantId) {
        for (Map<String, Object> resource : resources) {
            String sql = "INSERT IGNORE INTO jimu_resource (id, resource_name, resource_code, resource_type, parent_id, tenant_id, path, icon, sort_order, status, description, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'import')";
            jdbcTemplate.update(sql,
                    resource.get("id"), resource.get("resource_name"), resource.get("resource_code"),
                    resource.get("resource_type"), resource.get("parent_id"), tenantId,
                    resource.get("path"), resource.get("icon"), resource.get("sort_order"),
                    resource.get("status"), resource.get("description"));
        }
    }

    private void importResourcePermissions(List<Map<String, Object>> resourcePerms, String tenantId) {
        for (Map<String, Object> rp : resourcePerms) {
            String sql = "INSERT IGNORE INTO jimu_resource_permission (id, resource_id, target_type, target_id, permission_bits, tenant_id, create_by) VALUES (?, ?, ?, ?, ?, ?, 'import')";
            jdbcTemplate.update(sql, UUID.randomUUID().toString(),
                    rp.get("resource_id"), rp.get("target_type"), rp.get("target_id"),
                    rp.get("permission_bits"), tenantId);
        }
    }

    private void importDataPermissions(List<Map<String, Object>> dataPerms, String tenantId) {
        for (Map<String, Object> dp : dataPerms) {
            String sql = "INSERT IGNORE INTO jimu_data_permission (id, resource_id, target_type, target_id, filter_type, filter_rule, tenant_id, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, 'import')";
            jdbcTemplate.update(sql, UUID.randomUUID().toString(),
                    dp.get("resource_id"), dp.get("target_type"), dp.get("target_id"),
                    dp.get("filter_type"), dp.get("filter_rule"), tenantId);
        }
    }
}
