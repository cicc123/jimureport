package com.jimureport.enhancement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jimureport.enhancement.entity.JimuFillFormConfig;
import com.jimureport.enhancement.entity.JimuFillFormField;
import com.jimureport.enhancement.entity.JimuFillDraft;
import com.jimureport.enhancement.entity.JimuFillSubmitRecord;

import java.util.List;
import java.util.Map;

/**
 * 填报表单服务接口
 */
public interface JimuFillFormService {

    /**
     * 获取表单配置
     */
    JimuFillFormConfig getFormConfig(String reportId);

    /**
     * 获取表单字段列表
     */
    List<JimuFillFormField> getFormFields(String formId);

    /**
     * 创建表单配置
     */
    JimuFillFormConfig createFormConfig(JimuFillFormConfig config, List<JimuFillFormField> fields);

    /**
     * 更新表单配置
     */
    JimuFillFormConfig updateFormConfig(JimuFillFormConfig config, List<JimuFillFormField> fields);

    /**
     * 删除表单配置
     */
    boolean deleteFormConfig(String formId);

    /**
     * 提交表单数据
     */
    JimuFillSubmitRecord submitFormData(String formId, String userId, Map<String, Object> data);

    /**
     * 保存草稿
     */
    JimuFillDraft saveDraft(String formId, String userId, Map<String, Object> data);

    /**
     * 获取草稿
     */
    JimuFillDraft getDraft(String formId, String userId);

    /**
     * 删除草稿
     */
    boolean deleteDraft(String formId, String userId);

    /**
     * 查询提交记录
     */
    IPage<JimuFillSubmitRecord> getSubmitRecords(int pageNum, int pageSize, String formId, String userId);

    /**
     * 导出填报数据
     */
    List<Map<String, Object>> exportFormData(String formId);

    /**
     * 获取表单配置列表
     */
    IPage<JimuFillFormConfig> getFormConfigPage(int pageNum, int pageSize, String keyword);
}
