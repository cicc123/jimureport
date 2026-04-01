package com.jimureport.enhancement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jimureport.enhancement.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface SysUserService {

    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password);

    /**
     * 用户注册
     */
    SysUser register(SysUser user);

    /**
     * 获取用户分页列表
     */
    IPage<SysUser> getUserPage(int pageNum, int pageSize, String keyword, String status, String deptId);

    /**
     * 根据ID获取用户
     */
    SysUser getUserById(String id);

    /**
     * 根据用户名获取用户
     */
    SysUser getUserByUsername(String username);

    /**
     * 新增用户
     */
    SysUser addUser(SysUser user);

    /**
     * 修改用户
     */
    SysUser updateUser(SysUser user);

    /**
     * 删除用户
     */
    boolean deleteUser(String id);

    /**
     * 修改用户状态
     */
    boolean changeUserStatus(String id, String status);

    /**
     * 分配用户角色
     */
    boolean assignUserRoles(String userId, List<String> roleIds);

    /**
     * 修改个人信息
     */
    SysUser updateProfile(SysUser user);

    /**
     * 修改密码
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(String userId, String newPassword);
}
