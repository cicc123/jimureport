package com.jeecg.modules.jmreport.enums;

/**
 * 权限目标类型枚举
 * 用于区分权限分配的目标是用户还是用户组
 */
public enum TargetTypeEnum {
    
    USER(1, "用户"),
    GROUP(2, "用户组");
    
    private final int code;
    private final String desc;
    
    TargetTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static TargetTypeEnum getByCode(int code) {
        for (TargetTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
    
    public static boolean isValidCode(int code) {
        return getByCode(code) != null;
    }
}
