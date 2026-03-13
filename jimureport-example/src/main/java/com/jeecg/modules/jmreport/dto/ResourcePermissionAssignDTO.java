package com.jeecg.modules.jmreport.dto;

import java.util.List;

public class ResourcePermissionAssignDTO {
    private String resourceId;
    private int targetType;
    private String targetId;
    private int permissionBits;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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
