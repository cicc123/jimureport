package com.jeecg.modules.jmreport.dto;

import java.io.Serializable;

/**
 * 用户组创建/更新请求DTO
 */
public class UserGroupDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String groupName;
    private String groupCode;
    private Integer groupType;
    private String description;
    private Integer status;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String getGroupCode() {
        return groupCode;
    }
    
    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
    
    public Integer getGroupType() {
        return groupType;
    }
    
    public void setGroupType(Integer groupType) {
        this.groupType = groupType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}
