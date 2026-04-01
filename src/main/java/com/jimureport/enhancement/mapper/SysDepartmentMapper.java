package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.SysDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {

    /**
     * 查询部门树
     */
    List<SysDepartment> selectDeptTree();

    /**
     * 根据父ID查询子部门列表
     */
    List<SysDepartment> selectByParentId(@Param("parentId") String parentId);
}
