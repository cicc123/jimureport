package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuReportFieldDefault;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报表字段默认值Mapper接口
 */
@Mapper
public interface JimuReportFieldDefaultMapper extends BaseMapper<JimuReportFieldDefault> {

    /**
     * 根据报表ID查询字段默认值列表
     */
    List<JimuReportFieldDefault> selectByReportId(@Param("reportId") String reportId);

    /**
     * 根据报表ID删除字段默认值
     */
    int deleteByReportId(@Param("reportId") String reportId);
}
