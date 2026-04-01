package com.jimureport.enhancement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据权限表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jimu_data_permission")
public class JimuDataPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 报表ID
     */
    @TableField("report_id")
    private String reportId;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private String roleId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 表名
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 条件字段
     */
    @TableField("condition_field")
    private String conditionField;

    /**
     * 条件类型(eq:等于 like:包含 gt:大于 lt:小于 in:包含于)
     */
    @TableField("condition_type")
    private String conditionType;

    /**
     * 条件值
     */
    @TableField("condition_value")
    private String conditionValue;

    /**
     * 值类型(static:静态 user_id:用户ID dept_id:部门ID)
     */
    @TableField("value_type")
    private String valueType;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
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
    private Date updateTime;
}
