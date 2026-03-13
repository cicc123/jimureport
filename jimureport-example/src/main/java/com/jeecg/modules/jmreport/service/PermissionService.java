package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PermissionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 检查用户是否具有指定权限
     */
    public boolean hasPermission(String permCode) {
        String username = StpUtil.getLoginIdAsString();
        return hasPermission(username, permCode);
    }

    /**
     * 检查指定用户是否具有指定权限
     */
    public boolean hasPermission(String username, String permCode) {
        // 系统管理员拥有所有权限
        String adminSql = "SELECT is_admin FROM jimu_user WHERE username = ?";
        Map<String, Object> user = jdbcTemplate.queryForMap(adminSql, username);
        if (user != null && user.get("is_admin") != null && user.get("is_admin").toString().equals("1")) {
            return true;
        }

        // 查询用户的权限
        String sql = "" +
                "SELECT p.perm_code " +
                "FROM jimu_user u " +
                "JOIN jimu_user_role ur ON u.id = ur.user_id " +
                "JOIN jimu_role r ON ur.role_id = r.id " +
                "JOIN jimu_role_permission rp ON r.id = rp.role_id " +
                "JOIN jimu_permission p ON rp.perm_id = p.id " +
                "WHERE u.username = ? AND p.perm_code = ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, username, permCode);
        return !result.isEmpty();
    }

    /**
     * 获取用户的所有权限
     */
    public List<String> getUserPermissions(String username) {
        // 系统管理员拥有所有权限
        String adminSql = "SELECT is_admin FROM jimu_user WHERE username = ?";
        Map<String, Object> user = jdbcTemplate.queryForMap(adminSql, username);
        if (user != null && user.get("is_admin") != null && user.get("is_admin").toString().equals("1")) {
            String allPermsSql = "SELECT perm_code FROM jimu_permission";
            return jdbcTemplate.queryForList(allPermsSql, String.class);
        }

        // 查询用户的权限
        String sql = "" +
                "SELECT DISTINCT p.perm_code " +
                "FROM jimu_user u " +
                "JOIN jimu_user_role ur ON u.id = ur.user_id " +
                "JOIN jimu_role r ON ur.role_id = r.id " +
                "JOIN jimu_role_permission rp ON r.id = rp.role_id " +
                "JOIN jimu_permission p ON rp.perm_id = p.id " +
                "WHERE u.username = ?";

        return jdbcTemplate.queryForList(sql, String.class, username);
    }

    /**
     * 检查用户是否具有表单权限
     */
    public boolean hasFormPermission(String formId, String permType) {
        String username = StpUtil.getLoginIdAsString();
        return hasFormPermission(username, formId, permType);
    }

    /**
     * 检查指定用户是否具有表单权限
     */
    public boolean hasFormPermission(String username, String formId, String permType) {
        // 系统管理员拥有所有表单权限
        String adminSql = "SELECT is_admin FROM jimu_user WHERE username = ?";
        Map<String, Object> user = jdbcTemplate.queryForMap(adminSql, username);
        if (user != null && user.get("is_admin") != null && user.get("is_admin").toString().equals("1")) {
            return true;
        }

        // 获取用户ID
        String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
        String userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);

        // 获取用户的角色
        String roleSql = "SELECT role_id FROM jimu_user_role WHERE user_id = ?";
        List<String> roleIds = jdbcTemplate.queryForList(roleSql, String.class, userId);

        // 检查用户是否直接拥有表单权限
        String userPermSql = "" +
                "SELECT perms " +
                "FROM jimu_form_permission " +
                "WHERE form_id = ? AND user_id = ?";
        List<Map<String, Object>> userPerms = jdbcTemplate.queryForList(userPermSql, formId, userId);
        for (Map<String, Object> perm : userPerms) {
            String perms = perm.get("perms").toString();
            if (perms.contains(permType)) {
                return true;
            }
        }

        // 检查用户的角色是否拥有表单权限
        for (String roleId : roleIds) {
            String rolePermSql = "" +
                    "SELECT perms " +
                    "FROM jimu_form_permission " +
                    "WHERE form_id = ? AND role_id = ?";
            List<Map<String, Object>> rolePerms = jdbcTemplate.queryForList(rolePermSql, formId, roleId);
            for (Map<String, Object> perm : rolePerms) {
                String perms = perm.get("perms").toString();
                if (perms.contains(permType)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取用户可访问的表单列表
     */
    public List<Map<String, Object>> getAccessibleForms(String username) {
        // 系统管理员可以访问所有表单
        String adminSql = "SELECT is_admin FROM jimu_user WHERE username = ?";
        Map<String, Object> user = jdbcTemplate.queryForMap(adminSql, username);
        if (user != null && user.get("is_admin") != null && user.get("is_admin").toString().equals("1")) {
            // 这里应该查询所有表单，简化处理
            return List.of();
        }

        // 获取用户ID
        String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
        String userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);

        // 获取用户的角色
        String roleSql = "SELECT role_id FROM jimu_user_role WHERE user_id = ?";
        List<String> roleIds = jdbcTemplate.queryForList(roleSql, String.class, userId);

        // 查询用户可访问的表单
        // 这里需要根据实际的表单表结构来编写SQL
        // 简化处理，返回空列表
        return List.of();
    }
}
