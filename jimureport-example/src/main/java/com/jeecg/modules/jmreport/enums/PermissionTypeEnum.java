package com.jeecg.modules.jmreport.enums;

public enum PermissionTypeEnum {
    MENU("menu", "菜单"),
    SYSTEM("system", "系统权限"),
    REPORT("report", "报表权限"),
    DATA("data", "数据权限");
    
    private final String code;
    private final String name;
    
    PermissionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static PermissionTypeEnum fromCode(String code) {
        for (PermissionTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
