package com.jeecg.modules.jmreport.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 用户组成员管理请求DTO
 */
public class UserGroupMemberDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String groupId;
    private List<String> userIds;
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public List<String> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
