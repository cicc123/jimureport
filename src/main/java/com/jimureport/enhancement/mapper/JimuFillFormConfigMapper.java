package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuFillFormConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 填报表单配置Mapper接口
 */
public interface JimuFillFormConfigMapper extends BaseMapper<JimuFillFormConfig> {

    /**
     * 根据报表ID查询配置
     */
    JimuFillFormConfig selectByReportId(@Param("reportId") String reportId);
}
