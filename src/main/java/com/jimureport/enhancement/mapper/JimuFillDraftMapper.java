package com.jimureport.enhancement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimureport.enhancement.entity.JimuFillDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 填报草稿Mapper接口
 */
@Mapper
public interface JimuFillDraftMapper extends BaseMapper<JimuFillDraft> {

    /**
     * 根据表单ID和用户ID查询草稿
     */
    JimuFillDraft selectByFormAndUser(@Param("formId") String formId, @Param("userId") String userId);
}
