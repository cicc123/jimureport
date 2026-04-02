package com.jimureport.enhancement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jimureport.enhancement.entity.SysUser;
import com.jimureport.enhancement.entity.SysUserRole;
import com.jimureport.enhancement.mapper.SysUserMapper;
import com.jimureport.enhancement.mapper.SysUserRoleMapper;
import cn.dev33.satoken.stp.StpUtil;
import com.jimureport.enhancement.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(String username, String password) {
        // 查询用户
        SysUser user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if ("1".equals(user.getStatus())) {
            throw new RuntimeException("用户已被停用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // Sa-Token 登录（loginId 格式: userId::username）
        String loginId = user.getId() + "::" + user.getUsername();
        StpUtil.login(loginId);

        // 更新登录信息
        user.setLoginDate(new Date());
        userMapper.updateById(user);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(SysUser user) {
        // 检查用户名是否已存在
        SysUser existUser = getUserByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("0");
        user.setDelFlag("0");
        user.setId(UUID.randomUUID().toString().replace("-", ""));

        userMapper.insert(user);

        return user;
    }

    @Override
    public IPage<SysUser> getUserPage(int pageNum, int pageSize, String keyword, String status, String deptId) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(SysUser::getDelFlag, "0");

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getPhone, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword)
            );
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(SysUser::getStatus, status);
        }

        if (StringUtils.hasText(deptId)) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);

        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public SysUser getUserById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDelFlag, "0")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser addUser(SysUser user) {
        // 检查用户名是否已存在
        SysUser existUser = getUserByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("0");
        user.setDelFlag("0");
        if (!StringUtils.hasText(user.getId())) {
            user.setId(UUID.randomUUID().toString().replace("-", ""));
        }

        userMapper.insert(user);

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUser(SysUser user) {
        SysUser existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不允许修改密码
        user.setPassword(null);

        userMapper.updateById(user);

        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 逻辑删除
        user.setDelFlag("2");
        userMapper.updateById(user);

        // 删除用户角色关联
        userRoleMapper.deleteByUserId(id);

        return true;
    }

    @Override
    public boolean changeUserStatus(String id, String status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(String userId, List<String> roleIds) {
        // 删除原有角色关联
        userRoleMapper.deleteByUserId(userId);

        // 保存新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        return true;
    }

    @Override
    public SysUser updateProfile(SysUser user) {
        SysUser existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只允许修改部分字段
        existUser.setRealName(user.getRealName());
        existUser.setEmail(user.getEmail());
        existUser.setPhone(user.getPhone());
        existUser.setSex(user.getSex());
        existUser.setAvatar(user.getAvatar());

        userMapper.updateById(existUser);

        return userMapper.selectById(user.getId());
    }

    @Override
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        return true;
    }

    @Override
    public boolean resetPassword(String userId, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        return true;
    }
}
