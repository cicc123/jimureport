package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuReportPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报表权限Mapper接口
 */
@Mapper
public interface JimuReportPermissionMapper extends BaseMapper<JimuReportPermission> {

    /**
     * 根据报表ID查询权限列表
     */
    List<JimuReportPermission> selectByReportId(@Param("reportId") String reportId);

    /**
     * 根据用户ID查询权限列表
     */
    List<JimuReportPermission> selectByUserId(@Param("userId") String userId);
}
