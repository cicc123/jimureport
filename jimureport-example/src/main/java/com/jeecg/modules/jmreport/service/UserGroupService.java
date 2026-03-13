package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.dto.UserGroupDTO;
import com.jeecg.modules.jmreport.dto.UserGroupMemberDTO;
import com.jeecg.modules.jmreport.entity.UserGroup;
import com.jeecg.modules.jmreport.entity.UserGroupMember;
import com.jeecg.modules.jmreport.enums.GroupTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户分组服务
 * 提供用户组的增删改查、用户组成员管理等功能
 */
@Service
public class UserGroupService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 检查当前用户是否为管理员
     * @return true-管理员, false-普通用户
     */
    public boolean isAdmin() {
        String username = StpUtil.getLoginIdAsString();
        return isAdmin(username);
    }
    
    /**
     * 检查指定用户是否为管理员
     * @param username 用户名
     * @return true-管理员, false-普通用户
     */
    public boolean isAdmin(String username) {
        String sql = "SELECT is_admin FROM jimu_user WHERE username = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, username);
        if (result.isEmpty()) {
            return false;
        }
        Object isAdminObj = result.get(0).get("is_admin");
        return isAdminObj != null && "1".equals(isAdminObj.toString());
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public String getCurrentUserId() {
        String username = StpUtil.getLoginIdAsString();
        String sql = "SELECT id FROM jimu_user WHERE username = ?";
        return jdbcTemplate.queryForObject(sql, String.class, username);
    }
    
    /**
     * 获取当前用户名
     * @return 用户名
     */
    public String getCurrentUsername() {
        return StpUtil.getLoginIdAsString();
    }
    
    /**
     * 获取用户所属的所有用户组
     * @param userId 用户ID
     * @return 用户组列表
     */
    public List<UserGroup> getUserGroups(String userId) {
        String sql = "SELECT g.* FROM jimu_user_group g " +
                "JOIN jimu_user_group_member m ON g.id = m.group_id " +
                "WHERE m.user_id = ? AND g.status = 1 " +
                "ORDER BY g.group_type, g.create_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserGroup.class), userId);
    }
    
    /**
     * 获取用户所属的所有用户组ID
     * @param userId 用户ID
     * @return 用户组ID列表
     */
    public List<String> getUserGroupIds(String userId) {
        String sql = "SELECT group_id FROM jimu_user_group_member WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }
    
    /**
     * 检查用户是否属于管理员组
     * @param userId 用户ID
     * @return true-属于管理员组, false-不属于
     */
    public boolean isInAdminGroup(String userId) {
        String sql = "SELECT COUNT(*) FROM jimu_user_group_member m " +
                "JOIN jimu_user_group g ON m.group_id = g.id " +
                "WHERE m.user_id = ? AND g.group_type = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
    
    /**
     * 获取所有用户组列表
     * @return 用户组列表
     */
    public List<UserGroup> getAllGroups() {
        String sql = "SELECT id, group_name, group_code, group_type, tenant_id, description, status, create_by, create_time FROM jimu_user_group WHERE status = 1 ORDER BY group_type, create_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserGroup.class));
    }
    
    /**
     * 根据用户组类型获取用户组列表
     * @param groupType 用户组类型
     * @return 用户组列表
     */
    public List<UserGroup> getGroupsByType(Integer groupType) {
        String sql = "SELECT * FROM jimu_user_group WHERE group_type = ? AND status = 1 ORDER BY create_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserGroup.class), groupType);
    }
    
    /**
     * 根据ID获取用户组
     * @param groupId 用户组ID
     * @return 用户组
     */
    public UserGroup getGroupById(String groupId) {
        String sql = "SELECT * FROM jimu_user_group WHERE id = ?";
        List<UserGroup> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserGroup.class), groupId);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * 创建用户组
     * @param dto 用户组DTO
     * @return 创建的用户组
     */
    @Transactional(rollbackFor = Exception.class)
    public UserGroup createGroup(UserGroupDTO dto) {
        if (dto.getGroupCode() == null || dto.getGroupCode().trim().isEmpty()) {
            throw new IllegalArgumentException("用户组编码不能为空");
        }
        if (dto.getGroupName() == null || dto.getGroupName().trim().isEmpty()) {
            throw new IllegalArgumentException("用户组名称不能为空");
        }
        
        String checkSql = "SELECT COUNT(*) FROM jimu_user_group WHERE group_code = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, dto.getGroupCode());
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户组编码已存在: " + dto.getGroupCode());
        }
        
        String id = UUID.randomUUID().toString().replace("-", "");
        String username = getCurrentUsername();
        
        String sql = "INSERT INTO jimu_user_group (id, group_name, group_code, group_type, description, status, create_by, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, id, dto.getGroupName(), dto.getGroupCode(), 
                dto.getGroupType() != null ? dto.getGroupType() : GroupTypeEnum.NORMAL.getCode(),
                dto.getDescription(),
                dto.getStatus() != null ? dto.getStatus() : 1,
                username);
        
        return getGroupById(id);
    }
    
    /**
     * 更新用户组
     * @param dto 用户组DTO
     * @return 更新后的用户组
     */
    @Transactional(rollbackFor = Exception.class)
    public UserGroup updateGroup(UserGroupDTO dto) {
        if (dto.getId() == null || dto.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("用户组ID不能为空");
        }
        
        UserGroup existing = getGroupById(dto.getId());
        if (existing == null) {
            throw new IllegalArgumentException("用户组不存在: " + dto.getId());
        }
        
        String username = getCurrentUsername();
        
        String sql = "UPDATE jimu_user_group SET group_name = ?, description = ?, status = ?, update_by = ?, update_time = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, dto.getGroupName(), dto.getDescription(), dto.getStatus(), username, dto.getId());
        
        return getGroupById(dto.getId());
    }
    
    /**
     * 删除用户组
     * @param groupId 用户组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String groupId) {
        UserGroup group = getGroupById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("用户组不存在: " + groupId);
        }
        
        if (GroupTypeEnum.ADMIN.getCode() == group.getGroupType()) {
            throw new IllegalArgumentException("不能删除管理员组");
        }
        
        String deleteMembersSql = "DELETE FROM jimu_user_group_member WHERE group_id = ?";
        jdbcTemplate.update(deleteMembersSql, groupId);
        
        String deletePermissionsSql = "DELETE FROM jimu_report_permission WHERE target_type = 2 AND target_id = ?";
        jdbcTemplate.update(deletePermissionsSql, groupId);
        
        String deleteGroupSql = "DELETE FROM jimu_user_group WHERE id = ?";
        jdbcTemplate.update(deleteGroupSql, groupId);
    }
    
    /**
     * 添加用户到用户组
     * @param dto 用户组成员DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(UserGroupMemberDTO dto) {
        if (dto.getGroupId() == null || dto.getGroupId().trim().isEmpty()) {
            throw new IllegalArgumentException("用户组ID不能为空");
        }
        if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            throw new IllegalArgumentException("用户ID列表不能为空");
        }
        
        UserGroup group = getGroupById(dto.getGroupId());
        if (group == null) {
            throw new IllegalArgumentException("用户组不存在: " + dto.getGroupId());
        }
        
        String username = getCurrentUsername();
        String tenantId = getTenantId();
        
        for (String userId : dto.getUserIds()) {
            String checkSql = "SELECT COUNT(*) FROM jimu_user_group_member WHERE user_id = ? AND group_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, dto.getGroupId());
            if (count != null && count > 0) {
                continue;
            }
            
            String id = UUID.randomUUID().toString().replace("-", "");
            String sql = "INSERT INTO jimu_user_group_member (id, user_id, group_id, tenant_id, create_by, create_time) VALUES (?, ?, ?, ?, ?, NOW())";
            jdbcTemplate.update(sql, id, userId, dto.getGroupId(), tenantId, username);
        }
    }
    
    /**
     * 从用户组移除用户
     * @param groupId 用户组ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(String groupId, String userId) {
        String sql = "DELETE FROM jimu_user_group_member WHERE group_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, groupId, userId);
    }
    
    /**
     * 获取用户组成员列表
     * @param groupId 用户组ID
     * @return 成员用户ID列表
     */
    public List<String> getGroupMembers(String groupId) {
        String sql = "SELECT user_id FROM jimu_user_group_member WHERE group_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, groupId);
    }
    
    /**
     * 获取用户组成员详细信息
     * @param groupId 用户组ID
     * @return 成员信息列表
     */
    public List<Map<String, Object>> getGroupMemberDetails(String groupId) {
        String sql = "SELECT u.id, u.username, u.real_name, u.status, m.create_time as join_time " +
                "FROM jimu_user u " +
                "JOIN jimu_user_group_member m ON u.id = m.user_id " +
                "WHERE m.group_id = ? " +
                "ORDER BY m.create_time";
        return jdbcTemplate.queryForList(sql, groupId);
    }
    
    /**
     * 获取当前租户ID
     * @return 租户ID
     */
    private String getTenantId() {
        try {
            String username = StpUtil.getLoginIdAsString();
            String sql = "SELECT tenant_id FROM jimu_user WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, String.class, username);
        } catch (Exception e) {
            return "1";
        }
    }
}
