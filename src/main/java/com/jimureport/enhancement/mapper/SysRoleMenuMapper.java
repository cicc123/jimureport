package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单Mapper接口
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<String> selectMenuIdsByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色ID删除关联
     */
    int deleteByRoleId(@Param("roleId") String roleId);
}
