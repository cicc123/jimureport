package com.jimureport.enhancement.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 填报表单字段DTO
 */
@Data
public class FillFormFieldDTO {

    /**
     * 字段ID
     */
    private String id;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段标签
     */
    private String fieldLabel;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据来源类型
     */
    private String dataSourceType;

    /**
     * 数据来源配置
     */
    private String dataSourceConfig;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 默认值类型
     */
    private String defaultValueType;

    /**
     * 是否必填
     */
    private String isRequired;

    /**
     * 校验规则
     */
    private String validationRule;

    /**
     * 占位提示
     */
    private String placeholder;

    /**
     * 帮助文本
     */
    private String helpText;

    /**
     * 最小值
     */
    private BigDecimal minValue;

    /**
     * 最大值
     */
    private BigDecimal maxValue;

    /**
     * 最小长度
     */
    private Integer minLength;

    /**
     * 最大长度
     */
    private Integer maxLength;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 是否可见
     */
    private String isVisible;

    /**
     * 是否只读
     */
    private String isReadonly;
}
