package com.jimureport.enhancement.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 填报表单字段配置表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jimu_fill_form_field")
public class JimuFillFormField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 表单ID
     */
    @TableField("form_id")
    private String formId;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段标签
     */
    @TableField("field_label")
    private String fieldLabel;

    /**
     * 字段类型(text:文本 textarea:多行文本 number:数字 date:日期 datetime:日期时间 select:下拉框 radio:单选框 checkbox:复选框 file:文件 image:图片 hidden:隐藏)
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 数据类型(string:字符串 integer:整数 decimal:小数 date:日期 datetime:日期时间)
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 数据来源(manual:手动输入 dict:字典 sql:SQL api:接口 user:当前用户 dept:当前部门)
     */
    @TableField("data_source_type")
    private String dataSourceType;

    /**
     * 数据来源配置(JSON格式)
     */
    @TableField("data_source_config")
    private String dataSourceConfig;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 默认值类型(static:静态 user_info:用户信息 dept_info:部门信息)
     */
    @TableField("default_value_type")
    private String defaultValueType;

    /**
     * 是否必填(0否 1是)
     */
    @TableField("is_required")
    private String isRequired;

    /**
     * 校验规则(JSON格式)
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 占位提示
     */
    @TableField("placeholder")
    private String placeholder;

    /**
     * 帮助文本
     */
    @TableField("help_text")
    private String helpText;

    /**
     * 最小值(数字类型)
     */
    @TableField("min_value")
    private BigDecimal minValue;

    /**
     * 最大值(数字类型)
     */
    @TableField("max_value")
    private BigDecimal maxValue;

    /**
     * 最小长度
     */
    @TableField("min_length")
    private Integer minLength;

    /**
     * 最大长度
     */
    @TableField("max_length")
    private Integer maxLength;

    /**
     * 显示顺序
     */
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 是否可见(0否 1是)
     */
    @TableField("is_visible")
    private String isVisible;

    /**
     * 是否只读(0否 1是)
     */
    @TableField("is_readonly")
    private String isReadonly;

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
