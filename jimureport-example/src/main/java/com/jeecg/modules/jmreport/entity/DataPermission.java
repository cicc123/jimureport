package com.jeecg.modules.jmreport.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DataPermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String resourceId;
    private Integer targetType;
    private String targetId;
    private Integer filterType;
    private String filterRule;
    private String tenantId;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    
    public static final int TARGET_TYPE_USER = 1;
    public static final int TARGET_TYPE_ROLE = 2;
    public static final int TARGET_TYPE_GROUP = 3;
    
    public static final int FILTER_TYPE_USER = 1;
    public static final int FILTER_TYPE_DEPT = 2;
    public static final int FILTER_TYPE_CUSTOM = 3;
    public static final int FILTER_TYPE_SQL = 4;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public Integer getTargetType() {
        return targetType;
    }
    
    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public Integer getFilterType() {
        return filterType;
    }
    
    public void setFilterType(Integer filterType) {
        this.filterType = filterType;
    }
    
    public String getFilterRule() {
        return filterRule;
    }
    
    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getCreateBy() {
        return createBy;
    }
    
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getUpdateBy() {
        return updateBy;
    }
    
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
