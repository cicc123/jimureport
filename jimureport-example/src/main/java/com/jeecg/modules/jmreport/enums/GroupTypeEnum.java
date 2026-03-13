package com.jeecg.modules.jmreport.enums;

/**
 * 用户组类型枚举
 * 用于区分管理员组和普通用户组
 */
public enum GroupTypeEnum {
    
    ADMIN(1, "管理员组"),
    NORMAL(2, "普通用户组");
    
    private final int code;
    private final String desc;
    
    GroupTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static GroupTypeEnum getByCode(int code) {
        for (GroupTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
    
    public static boolean isValidCode(int code) {
        return getByCode(code) != null;
    }
    
    public static boolean isAdminGroup(int code) {
        return ADMIN.getCode() == code;
    }
}
