package com.jeecg.modules.jmreport.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 报表权限分配请求DTO
 */
public class ReportPermissionAssignDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String reportId;
    private Integer targetType;
    private List<String> targetIds;
    private String permissionType;
    
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public Integer getTargetType() {
        return targetType;
    }
    
    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }
    
    public List<String> getTargetIds() {
        return targetIds;
    }
    
    public void setTargetIds(List<String> targetIds) {
        this.targetIds = targetIds;
    }
    
    public String getPermissionType() {
        return permissionType;
    }
    
    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }
}
