package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.common.Result;
import com.jeecg.modules.jmreport.dto.UserGroupDTO;
import com.jeecg.modules.jmreport.dto.UserGroupMemberDTO;
import com.jeecg.modules.jmreport.entity.UserGroup;
import com.jeecg.modules.jmreport.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户组管理控制器
 * 提供用户组的增删改查、成员管理等功能
 */
@RestController
@RequestMapping("/api/group")
public class UserGroupController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserGroupController.class);
    
    @Autowired
    private UserGroupService userGroupService;
    
    /**
     * 获取所有用户组列表
     * @return 用户组列表
     */
    @GetMapping("/list")
    public Result<List<UserGroup>> getAllGroups() {
        try {
            List<UserGroup> groups = userGroupService.getAllGroups();
            return Result.success(groups);
        } catch (Exception e) {
            logger.error("获取用户组列表失败: {}", e.getMessage(), e);
            return Result.error("获取用户组列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取用户组列表
     * @param groupType 用户组类型
     * @return 用户组列表
     */
    @GetMapping("/list/type/{groupType}")
    public Result<List<UserGroup>> getGroupsByType(@PathVariable Integer groupType) {
        try {
            List<UserGroup> groups = userGroupService.getGroupsByType(groupType);
            return Result.success(groups);
        } catch (Exception e) {
            logger.error("获取用户组列表失败: {}", e.getMessage(), e);
            return Result.error("获取用户组列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取用户组详情
     * @param groupId 用户组ID
     * @return 用户组详情
     */
    @GetMapping("/{groupId}")
    public Result<UserGroup> getGroupById(@PathVariable String groupId) {
        try {
            UserGroup group = userGroupService.getGroupById(groupId);
            if (group == null) {
                return Result.notFound("用户组不存在");
            }
            return Result.success(group);
        } catch (Exception e) {
            logger.error("获取用户组详情失败: {}", e.getMessage(), e);
            return Result.error("获取用户组详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建用户组
     * @param dto 用户组DTO
     * @return 创建的用户组
     */
    @PostMapping("/create")
    public Result<UserGroup> createGroup(@RequestBody UserGroupDTO dto) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            UserGroup group = userGroupService.createGroup(dto);
            return Result.success("创建用户组成功", group);
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("创建用户组失败: {}", e.getMessage(), e);
            return Result.error("创建用户组失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户组
     * @param dto 用户组DTO
     * @return 更新后的用户组
     */
    @PutMapping("/update")
    public Result<UserGroup> updateGroup(@RequestBody UserGroupDTO dto) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            UserGroup group = userGroupService.updateGroup(dto);
            return Result.success("更新用户组成功", group);
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("更新用户组失败: {}", e.getMessage(), e);
            return Result.error("更新用户组失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户组
     * @param groupId 用户组ID
     * @return 操作结果
     */
    @DeleteMapping("/{groupId}")
    public Result<Void> deleteGroup(@PathVariable String groupId) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            userGroupService.deleteGroup(groupId);
            return Result.successMsg("删除用户组成功");
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("删除用户组失败: {}", e.getMessage(), e);
            return Result.error("删除用户组失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加用户到用户组
     * @param dto 用户组成员DTO
     * @return 操作结果
     */
    @PostMapping("/member/add")
    public Result<Void> addMembers(@RequestBody UserGroupMemberDTO dto) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            userGroupService.addMembers(dto);
            return Result.successMsg("添加成员成功");
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}", e.getMessage());
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            logger.error("添加成员失败: {}", e.getMessage(), e);
            return Result.error("添加成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 从用户组移除用户
     * @param groupId 用户组ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/member/{groupId}/{userId}")
    public Result<Void> removeMember(@PathVariable String groupId, @PathVariable String userId) {
        if (!isAdmin()) {
            return Result.forbidden("权限不足，仅管理员可执行此操作");
        }
        
        try {
            userGroupService.removeMember(groupId, userId);
            return Result.successMsg("移除成员成功");
        } catch (Exception e) {
            logger.error("移除成员失败: {}", e.getMessage(), e);
            return Result.error("移除成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户组成员列表
     * @param groupId 用户组ID
     * @return 成员列表
     */
    @GetMapping("/member/{groupId}")
    public Result<List<Map<String, Object>>> getGroupMembers(@PathVariable String groupId) {
        try {
            List<Map<String, Object>> members = userGroupService.getGroupMemberDetails(groupId);
            return Result.success(members);
        } catch (Exception e) {
            logger.error("获取成员列表失败: {}", e.getMessage(), e);
            return Result.error("获取成员列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户所属的用户组
     * @return 用户组列表
     */
    @GetMapping("/current")
    public Result<List<UserGroup>> getCurrentUserGroups() {
        try {
            String userId = userGroupService.getCurrentUserId();
            List<UserGroup> groups = userGroupService.getUserGroups(userId);
            return Result.success(groups);
        } catch (Exception e) {
            logger.error("获取当前用户组失败: {}", e.getMessage(), e);
            return Result.error("获取当前用户组失败: " + e.getMessage());
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
