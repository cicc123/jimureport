package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuDataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据权限Mapper接口
 */
public interface JimuDataPermissionMapper extends BaseMapper<JimuDataPermission> {

    /**
     * 根据报表ID查询数据权限
     */
    List<JimuDataPermission> selectByReportId(@Param("reportId") String reportId);
}
