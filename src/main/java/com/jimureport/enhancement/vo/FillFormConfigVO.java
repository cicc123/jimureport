package com.jimureport.enhancement.vo;

import com.jimureport.enhancement.entity.JimuFillFormField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 填报表单配置VO
 */
@Data
public class FillFormConfigVO {

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
     * 提交方式
     */
    private String submitType;

    /**
     * 提交方式名称
     */
    private String submitTypeName;

    /**
     * 提交配置
     */
    private String submitConfig;

    /**
     * 是否允许草稿
     */
    private String allowDraft;

    /**
     * 是否允许重复提交
     */
    private String allowDuplicate;

    /**
     * 重复检查字段
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
     * 状态
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 字段列表
     */
    private List<JimuFillFormField> fields;
}
