package com.jimureport.enhancement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jimureport.enhancement.entity.*;
import com.jimureport.enhancement.mapper.*;
import com.jimureport.enhancement.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final JimuReportPermissionMapper reportPermissionMapper;
    private final JimuDataPermissionMapper dataPermissionMapper;

    @Override
    public Set<String> getUserPermissions(String userId) {
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);

        Set<String> permissions = new HashSet<>();

        // 如果是超级管理员，返回所有权限
        for (SysRole role : roles) {
            if ("admin".equals(role.getRoleKey())) {
                permissions.add("*:*:*");
                return permissions;
            }
        }

        // 查询角色对应的菜单权限
        for (SysRole role : roles) {
            List<SysMenu> menus = menuMapper.selectMenusByRoleId(role.getId());
            for (SysMenu menu : menus) {
                if (StringUtils.hasText(menu.getPerms())) {
                    permissions.add(menu.getPerms());
                }
            }
        }

        return permissions;
    }

    @Override
    public List<SysRole> getUserRoles(String userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public List<SysMenu> getUserMenus(String userId) {
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);

        // 如果是超级管理员，返回所有菜单
        for (SysRole role : roles) {
            if ("admin".equals(role.getRoleKey())) {
                return menuMapper.selectMenuTree();
            }
        }

        // 查询角色对应的菜单
        Set<String> menuIds = new HashSet<>();
        for (SysRole role : roles) {
            List<String> ids = roleMenuMapper.selectMenuIdsByRoleId(role.getId());
            menuIds.addAll(ids);
        }

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建菜单树
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, "0")
                        .orderByAsc(SysMenu::getOrderNum)
        );

        return buildMenuTree(allMenus, "0");
    }

    @Override
    public boolean hasPermission(String userId, String permission) {
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains("*:*:*") || permissions.contains(permission);
    }

    @Override
    public boolean canAccessReport(String userId, String reportId, String permissionType) {
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);

        // 如果是超级管理员，直接返回true
        for (SysRole role : roles) {
            if ("admin".equals(role.getRoleKey())) {
                return true;
            }
        }

        // 查询报表权限
        LambdaQueryWrapper<JimuReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JimuReportPermission::getReportId, reportId);
        wrapper.eq(JimuReportPermission::getPermissionType, permissionType);
        wrapper.and(w -> w
                .eq(JimuReportPermission::getUserId, userId)
                .or()
                .in(JimuReportPermission::getRoleId, roles.stream().map(SysRole::getId).collect(Collectors.toList()))
        );

        return reportPermissionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<SysRole> getRoleList() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDelFlag, "0")
                        .orderByAsc(SysRole::getRoleSort)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(SysRole role, List<String> menuIds) {
        role.setDelFlag("0");
        roleMapper.insert(role);

        // 保存角色菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (String menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(SysRole role, List<String> menuIds) {
        roleMapper.updateById(role);

        // 删除原有菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, role.getId())
        );

        // 保存新的菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (String menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        return roleMapper.selectById(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查是否有用户使用该角色
        long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
        );
        if (userCount > 0) {
            throw new RuntimeException("该角色已被用户使用，无法删除");
        }

        // 逻辑删除
        role.setDelFlag("2");
        roleMapper.updateById(role);

        // 删除角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );

        return true;
    }

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, "0")
                        .orderByAsc(SysMenu::getOrderNum)
        );
        return buildMenuTree(allMenus, "0");
    }

    @Override
    public List<String> getAllMenuPermissions() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, "0")
                        .isNotNull(SysMenu::getPerms)
                        .ne(SysMenu::getPerms, "")
        );
        List<String> permissions = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (menu.getPerms() != null && !menu.getPerms().isEmpty()) {
                permissions.add(menu.getPerms());
            }
        }
        return permissions;
    }

    @Override
    public List<String> getRoleMenuIds(String roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignReportPermission(JimuReportPermission permission) {
        // 检查是否已存在相同权限
        LambdaQueryWrapper<JimuReportPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JimuReportPermission::getReportId, permission.getReportId());
        wrapper.eq(JimuReportPermission::getPermissionType, permission.getPermissionType());

        if (permission.getRoleId() != null) {
            wrapper.eq(JimuReportPermission::getRoleId, permission.getRoleId());
        }
        if (permission.getUserId() != null) {
            wrapper.eq(JimuReportPermission::getUserId, permission.getUserId());
        }

        JimuReportPermission existPermission = reportPermissionMapper.selectOne(wrapper);
        if (existPermission != null) {
            // 更新已有权限
            existPermission.setDataScope(permission.getDataScope());
            existPermission.setDataScopeRule(permission.getDataScopeRule());
            reportPermissionMapper.updateById(existPermission);
        } else {
            // 新增权限
            reportPermissionMapper.insert(permission);
        }
    }

    @Override
    public void removeReportPermission(String permissionId) {
        reportPermissionMapper.deleteById(permissionId);
    }

    @Override
    public List<JimuReportPermission> getReportPermissions(String reportId) {
        return reportPermissionMapper.selectByReportId(reportId);
    }

    @Override
    public String getDataPermissionCondition(String userId, String reportId, String tableName) {
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);

        // 如果是超级管理员，无数据限制
        for (SysRole role : roles) {
            if ("admin".equals(role.getRoleKey())) {
                return null;
            }
        }

        // 查询数据权限配置
        List<JimuDataPermission> dataPermissions = new ArrayList<>();

        // 查询用户级数据权限
        List<JimuDataPermission> userPermissions = dataPermissionMapper.selectList(
                new LambdaQueryWrapper<JimuDataPermission>()
                        .eq(JimuDataPermission::getReportId, reportId)
                        .eq(JimuDataPermission::getUserId, userId)
                        .eq(JimuDataPermission::getTableName, tableName)
        );
        dataPermissions.addAll(userPermissions);

        // 查询角色级数据权限
        for (SysRole role : roles) {
            List<JimuDataPermission> rolePermissions = dataPermissionMapper.selectList(
                    new LambdaQueryWrapper<JimuDataPermission>()
                            .eq(JimuDataPermission::getReportId, reportId)
                            .eq(JimuDataPermission::getRoleId, role.getId())
                            .eq(JimuDataPermission::getTableName, tableName)
            );
            dataPermissions.addAll(rolePermissions);
        }

        if (dataPermissions.isEmpty()) {
            return null;
        }

        // 构建数据权限条件
        StringBuilder condition = new StringBuilder();
        for (JimuDataPermission dp : dataPermissions) {
            if (condition.length() > 0) {
                condition.append(" AND ");
            }

            String value = resolveConditionValue(dp.getValueType(), dp.getConditionValue(), userId);
            String operator = getOperator(dp.getConditionType());

            condition.append(dp.getConditionField())
                    .append(" ")
                    .append(operator)
                    .append(" ")
                    .append(formatValue(dp.getConditionType(), value));
        }

        return condition.toString();
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, String parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 解析条件值
     */
    private String resolveConditionValue(String valueType, String conditionValue, String userId) {
        switch (valueType) {
            case "user_id":
                return userId;
            case "dept_id":
                // 查询用户部门
                SysUser user = userMapper.selectById(userId);
                return user != null ? user.getDeptId() : "";
            default:
                return conditionValue;
        }
    }

    /**
     * 获取SQL操作符
     */
    private String getOperator(String conditionType) {
        switch (conditionType) {
            case "eq":
                return "=";
            case "like":
                return "LIKE";
            case "gt":
                return ">";
            case "lt":
                return "<";
            case "in":
                return "IN";
            default:
                return "=";
        }
    }

    /**
     * 格式化值
     */
    private String formatValue(String conditionType, String value) {
        if ("like".equals(conditionType)) {
            return "'%" + value + "%'";
        } else if ("in".equals(conditionType)) {
            return "(" + value + ")";
        } else {
            return "'" + value + "'";
        }
    }
}
