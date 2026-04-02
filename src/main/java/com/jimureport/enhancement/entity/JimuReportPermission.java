package com.jimureport.enhancement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 报表权限表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("jimu_report_permission")
public class JimuReportPermission implements Serializable {

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
     * 权限类型(view:查看 design:设计 fill:填报)
     */
    @TableField("permission_type")
    private String permissionType;

    /**
     * 数据范围(all:全部 dept:部门 self:个人 custom:自定义)
     */
    @TableField("data_scope")
    private String dataScope;

    /**
     * 数据范围规则(JSON格式)
     */
    @TableField("data_scope_rule")
    private String dataScopeRule;

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
