package com.jimureport.enhancement.dto;

import lombok.Data;

import java.util.List;

/**
 * 填报表单配置DTO
 */
@Data
public class FillFormConfigDTO {

    /**
     * 配置ID
     */
    private String id;

    /**
     * 报表ID
     */
    private String reportId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单描述
     */
    private String formDesc;

    /**
     * 提交方式(database:数据库 api:接口 java:自定义Java)
     */
    private String submitType;

    /**
     * 提交配置(JSON格式)
     */
    private String submitConfig;

    /**
     * 是否允许草稿(0否 1是)
     */
    private String allowDraft;

    /**
     * 是否允许重复提交(0否 1是)
     */
    private String allowDuplicate;

    /**
     * 重复检查字段(逗号分隔)
     */
    private String duplicateCheckFields;

    /**
     * 成功提示消息
     */
    private String successMessage;

    /**
     * 提交后跳转URL
     */
    private String redirectUrl;

    /**
     * 状态(0正常 1停用)
     */
    private String status;

    /**
     * 字段列表
     */
    private List<FillFormFieldDTO> fields;
}
