package com.jeecg.modules.jmreport.enums;

public enum ResourceTypeEnum {
    MENU(1, "菜单"),
    DIRECTORY(2, "目录"),
    REPORT(3, "报表");
    
    private final int code;
    private final String name;
    
    ResourceTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public static ResourceTypeEnum fromCode(int code) {
        for (ResourceTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
