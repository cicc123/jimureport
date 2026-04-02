package com.jimureport.enhancement.service;

import com.jimureport.enhancement.entity.JimuReportPermission;
import com.jimureport.enhancement.entity.SysMenu;
import com.jimureport.enhancement.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface SysPermissionService {

    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(String userId);

    /**
     * 获取用户角色列表
     */
    List<SysRole> getUserRoles(String userId);

    /**
     * 获取用户菜单列表
     */
    List<SysMenu> getUserMenus(String userId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(String userId, String permission);

    /**
     * 检查用户是否能访问指定报表
     */
    boolean canAccessReport(String userId, String reportId, String permissionType);

    /**
     * 获取角色列表
     */
    List<SysRole> getRoleList();

    /**
     * 新增角色
     */
    SysRole addRole(SysRole role, List<String> menuIds);

    /**
     * 修改角色
     */
    SysRole updateRole(SysRole role, List<String> menuIds);

    /**
     * 删除角色
     */
    boolean deleteRole(String roleId);

    /**
     * 获取菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 获取所有菜单权限（扁平列表）
     */
    List<String> getAllMenuPermissions();

    /**
     * 获取角色菜单ID列表
     */
    List<String> getRoleMenuIds(String roleId);

    /**
     * 分配报表权限
     */
    void assignReportPermission(JimuReportPermission permission);

    /**
     * 移除报表权限
     */
    void removeReportPermission(String permissionId);

    /**
     * 获取报表权限列表
     */
    List<JimuReportPermission> getReportPermissions(String reportId);

    /**
     * 获取用户的数据权限条件
     */
    String getDataPermissionCondition(String userId, String reportId, String tableName);
}
