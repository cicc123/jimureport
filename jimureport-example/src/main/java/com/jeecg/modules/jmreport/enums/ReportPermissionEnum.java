package com.jeecg.modules.jmreport.enums;

/**
 * 报表权限类型枚举
 * 定义报表可分配的权限类型
 */
public enum ReportPermissionEnum {
    
    VIEW("view", "查看"),
    EDIT("edit", "编辑"),
    DELETE("delete", "删除");
    
    private final String code;
    private final String desc;
    
    ReportPermissionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static ReportPermissionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReportPermissionEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
