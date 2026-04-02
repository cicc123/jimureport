package com.jimureport.enhancement.config;

import cn.dev33.satoken.stp.StpInterface;
import com.jimureport.enhancement.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限数据提供接口
 * 为 @SaCheckPermission 提供用户权限和角色数据
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysPermissionService permissionService;

    /**
     * 返回当前用户拥有的权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String id = loginId.toString();
        String userId = id.contains("::") ? id.split("::", 2)[0] : id;

        try {
            return new ArrayList<>(permissionService.getUserPermissions(userId));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 返回当前用户拥有的角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String id = loginId.toString();
        String userId = id.contains("::") ? id.split("::", 2)[0] : id;

        try {
            var roles = permissionService.getUserRoles(userId);
            List<String> roleKeys = new ArrayList<>();
            for (var role : roles) {
                roleKeys.add(role.getRoleKey());
            }
            return roleKeys;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
