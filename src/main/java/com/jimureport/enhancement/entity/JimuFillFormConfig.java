package com.jimureport.enhancement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 填报表单配置表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jimu_fill_form_config")
public class JimuFillFormConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 报表ID
     */
    @TableField("report_id")
    private String reportId;

    /**
     * 表单名称
     */
    @TableField("form_name")
    private String formName;

    /**
     * 表单描述
     */
    @TableField("form_desc")
    private String formDesc;

    /**
     * 提交方式(database:数据库 api:接口 java:自定义Java)
     */
    @TableField("submit_type")
    private String submitType;

    /**
     * 提交配置(JSON格式)
     */
    @TableField("submit_config")
    private String submitConfig;

    /**
     * 是否允许草稿(0否 1是)
     */
    @TableField("allow_draft")
    private String allowDraft;

    /**
     * 是否允许重复提交(0否 1是)
     */
    @TableField("allow_duplicate")
    private String allowDuplicate;

    /**
     * 重复检查字段(逗号分隔)
     */
    @TableField("duplicate_check_fields")
    private String duplicateCheckFields;

    /**
     * 成功提示消息
     */
    @TableField("success_message")
    private String successMessage;

    /**
     * 提交后跳转URL
     */
    @TableField("redirect_url")
    private String redirectUrl;

    /**
     * 状态(0正常 1停用)
     */
    @TableField("status")
    private String status;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
