package com.jimureport.enhancement.dto;

import lombok.Data;

import java.util.Map;

/**
 * 填报提交DTO
 */
@Data
public class FillSubmitDTO {

    /**
     * 表单ID
     */
    private String formId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 提交数据
     */
    private Map<String, Object> data;

    /**
     * 是否保存为草稿
     */
    private Boolean saveAsDraft;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;
}
