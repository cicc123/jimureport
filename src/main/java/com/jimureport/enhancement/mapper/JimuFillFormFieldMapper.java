package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuFillFormField;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 填报表单字段Mapper接口
 */
@Mapper
public interface JimuFillFormFieldMapper extends BaseMapper<JimuFillFormField> {

    /**
     * 根据表单ID查询字段列表
     */
    List<JimuFillFormField> selectByFormId(@Param("formId") String formId);

    /**
     * 根据表单ID删除字段
     */
    int deleteByFormId(@Param("formId") String formId);
}
