package com.jeecg.modules.jmreport.service;

import com.jeecg.modules.jmreport.satoken.exception.AjaxJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserGroupService {

    private Logger logger = LoggerFactory.getLogger(UserGroupService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取用户组列表
     */
    public List<Map<String, Object>> getUserGroupList(String groupName, Integer status, Integer page, Integer pageSize) {
        StringBuilder sql = new StringBuilder("SELECT id, group_name, group_code, tenant_id, description, status, create_time FROM jimu_user_group WHERE 1=1");
        
        if (groupName != null && !groupName.isEmpty()) {
            sql.append(" AND group_name LIKE ?");
        }
        if (status != null) {
            sql.append(" AND status = ?");
        }
        
        sql.append(" ORDER BY create_time DESC");
        sql.append(" LIMIT ? OFFSET ?");
        
        int offset = (page - 1) * pageSize;
        
        if (groupName != null && !groupName.isEmpty() && status != null) {
            return jdbcTemplate.queryForList(sql.toString(), "%" + groupName + "%", status, pageSize, offset);
        } else if (groupName != null && !groupName.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), "%" + groupName + "%", pageSize, offset);
        } else if (status != null) {
            return jdbcTemplate.queryForList(sql.toString(), status, pageSize, offset);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), pageSize, offset);
        }
    }

    /**
     * 获取用户组总数
     */
    public int getUserGroupCount(String groupName, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM jimu_user_group WHERE 1=1");
        
        if (groupName != null && !groupName.isEmpty()) {
            sql.append(" AND group_name LIKE ?");
        }
        if (status != null) {
            sql.append(" AND status = ?");
        }
        
        if (groupName != null && !groupName.isEmpty() && status != null) {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, "%" + groupName + "%", status);
        } else if (groupName != null && !groupName.isEmpty()) {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, "%" + groupName + "%");
        } else if (status != null) {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class, status);
        } else {
            return jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        }
    }

    /**
     * 根据ID获取用户组
     */
    public Map<String, Object> getUserGroupById(String id) {
        String sql = "SELECT id, group_name, group_code, tenant_id, description, status FROM jimu_user_group WHERE id = ?";
        try {
            return jdbcTemplate.queryForMap(sql, id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户组中的用户列表
     */
    public List<Map<String, Object>> getUsersInGroup(String groupId) {
        String sql = "SELECT u.id, u.username, u.real_name, u.status FROM jimu_user u JOIN jimu_user_group_user gu ON u.id = gu.user_id WHERE gu.group_id = ?";
        return jdbcTemplate.queryForList(sql, groupId);
    }

    /**
     * 获取不在用户组中的用户列表
     */
    public List<Map<String, Object>> getUsersNotInGroup(String groupId, String username) {
        StringBuilder sql = new StringBuilder("SELECT id, username, real_name, status FROM jimu_user WHERE id NOT IN (SELECT user_id FROM jimu_user_group_user WHERE group_id = ?)");
        
        if (username != null && !username.isEmpty()) {
            sql.append(" AND username LIKE ?");
            return jdbcTemplate.queryForList(sql.toString(), groupId, "%" + username + "%");
        } else {
            return jdbcTemplate.queryForList(sql.toString(), groupId);
        }
    }

    /**
     * 创建用户组
     */
    public AjaxJson createUserGroup(Map<String, Object> groupData) {
        // 验证必填字段
        if (!groupData.containsKey("group_name") || groupData.get("group_name") == null || groupData.get("group_name").toString().isEmpty()) {
            return AjaxJson.error("用户组名称不能为空");
        }
        if (!groupData.containsKey("group_code") || groupData.get("group_code") == null || groupData.get("group_code").toString().isEmpty()) {
            return AjaxJson.error("用户组编码不能为空");
        }
        
        String groupName = groupData.get("group_name").toString();
        String groupCode = groupData.get("group_code").toString();
        String tenantId = groupData.containsKey("tenant_id") ? groupData.get("tenant_id").toString() : "1";
        String description = groupData.containsKey("description") ? groupData.get("description").toString() : "";
        
        // 检查用户组编码是否已存在
        String checkSql = "SELECT COUNT(*) FROM jimu_user_group WHERE group_code = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, groupCode);
        if (count > 0) {
            return AjaxJson.error("用户组编码已存在");
        }
        
        // 创建用户组
        String id = UUID.randomUUID().toString();
        String insertSql = "INSERT INTO jimu_user_group (id, group_name, group_code, tenant_id, description, status) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, id, groupName, groupCode, tenantId, description, 1);
        
        // 如果有用户信息，关联用户
        if (groupData.containsKey("user_ids") && groupData.get("user_ids") != null) {
            List<String> userIds = (List<String>) groupData.get("user_ids");
            for (String userId : userIds) {
                String guId = UUID.randomUUID().toString();
                String insertGuSql = "INSERT INTO jimu_user_group_user (id, group_id, user_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertGuSql, guId, id, userId);
            }
        }
        
        return AjaxJson.success("用户组创建成功");
    }

    /**
     * 更新用户组
     */
    public AjaxJson updateUserGroup(String id, Map<String, Object> groupData) {
        // 检查用户组是否存在
        Map<String, Object> existingGroup = getUserGroupById(id);
        if (existingGroup == null) {
            return AjaxJson.error("用户组不存在");
        }
        
        // 构建更新语句
        StringBuilder updateSql = new StringBuilder("UPDATE jimu_user_group SET ");
        Object[] params = new Object[10]; // 预分配参数数组
        int paramIndex = 0;
        
        if (groupData.containsKey("group_name")) {
            updateSql.append("group_name = ?,");
            params[paramIndex++] = groupData.get("group_name");
        }
        if (groupData.containsKey("description")) {
            updateSql.append("description = ?,");
            params[paramIndex++] = groupData.get("description");
        }
        if (groupData.containsKey("status")) {
            updateSql.append("status = ?,");
            params[paramIndex++] = groupData.get("status");
        }
        
        // 移除最后一个逗号
        if (updateSql.toString().endsWith(",")) {
            updateSql = new StringBuilder(updateSql.substring(0, updateSql.length() - 1));
        }
        
        updateSql.append(" WHERE id = ?");
        params[paramIndex++] = id;
        
        // 执行更新
        if (paramIndex > 1) { // 至少有一个更新字段
            Object[] actualParams = new Object[paramIndex];
            System.arraycopy(params, 0, actualParams, 0, paramIndex);
            jdbcTemplate.update(updateSql.toString(), actualParams);
        }
        
        // 更新用户关联
        if (groupData.containsKey("user_ids")) {
            // 删除旧的用户关联
            String deleteSql = "DELETE FROM jimu_user_group_user WHERE group_id = ?";
            jdbcTemplate.update(deleteSql, id);
            
            // 添加新的用户关联
            List<String> userIds = (List<String>) groupData.get("user_ids");
            for (String userId : userIds) {
                String guId = UUID.randomUUID().toString();
                String insertGuSql = "INSERT INTO jimu_user_group_user (id, group_id, user_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertGuSql, guId, id, userId);
            }
        }
        
        return AjaxJson.success("用户组更新成功");
    }

    /**
     * 删除用户组
     */
    public AjaxJson deleteUserGroup(String id) {
        // 检查用户组是否存在
        Map<String, Object> existingGroup = getUserGroupById(id);
        if (existingGroup == null) {
            return AjaxJson.error("用户组不存在");
        }
        
        // 开始事务
        try {
            // 删除用户组用户关联
            String deleteGuSql = "DELETE FROM jimu_user_group_user WHERE group_id = ?";
            jdbcTemplate.update(deleteGuSql, id);
            
            // 删除用户组
            String deleteGroupSql = "DELETE FROM jimu_user_group WHERE id = ?";
            jdbcTemplate.update(deleteGroupSql, id);
            
            return AjaxJson.success("用户组删除成功");
        } catch (Exception e) {
            logger.error("删除用户组失败：", e);
            return AjaxJson.error("删除用户组失败");
        }
    }

    /**
     * 批量删除用户组
     */
    public AjaxJson batchDeleteUserGroup(List<String> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return AjaxJson.error("请选择要删除的用户组");
        }
        
        try {
            for (String id : groupIds) {
                // 检查用户组是否存在
                Map<String, Object> existingGroup = getUserGroupById(id);
                if (existingGroup != null) {
                    // 删除用户组用户关联
                    String deleteGuSql = "DELETE FROM jimu_user_group_user WHERE group_id = ?";
                    jdbcTemplate.update(deleteGuSql, id);
                    
                    // 删除用户组
                    String deleteGroupSql = "DELETE FROM jimu_user_group WHERE id = ?";
                    jdbcTemplate.update(deleteGroupSql, id);
                }
            }
            
            return AjaxJson.success("批量删除用户组成功");
        } catch (Exception e) {
            logger.error("批量删除用户组失败：", e);
            return AjaxJson.error("批量删除用户组失败");
        }
    }

    /**
     * 更新用户组状态
     */
    public AjaxJson updateUserGroupStatus(String id, Integer status) {
        // 检查用户组是否存在
        Map<String, Object> existingGroup = getUserGroupById(id);
        if (existingGroup == null) {
            return AjaxJson.error("用户组不存在");
        }
        
        // 更新状态
        String updateSql = "UPDATE jimu_user_group SET status = ? WHERE id = ?";
        jdbcTemplate.update(updateSql, status, id);
        
        return AjaxJson.success("用户组状态更新成功");
    }
}
