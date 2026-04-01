package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据角色ID查询用户列表
     */
    List<SysUser> selectUsersByRoleId(@Param("roleId") String roleId);

    /**
     * 根据部门ID查询用户列表
     */
    List<SysUser> selectUsersByDeptId(@Param("deptId") String deptId);
}
