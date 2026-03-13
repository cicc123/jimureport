package com.jeecg.modules.jmreport.dto;

import java.util.List;

public class BatchPermissionAssignDTO {
    private List<String> resourceIds;
    private int targetType;
    private String targetId;
    private int permissionBits;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getPermissionBits() {
        return permissionBits;
    }

    public void setPermissionBits(int permissionBits) {
        this.permissionBits = permissionBits;
    }
}
