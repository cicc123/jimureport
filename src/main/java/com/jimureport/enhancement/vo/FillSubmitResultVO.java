package com.jimureport.enhancement.vo;

import lombok.Data;

import java.util.List;

/**
 * 填报提交结果VO
 */
@Data
public class FillSubmitResultVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 错误字段列表
     */
    private List<FieldError> fieldErrors;

    /**
     * 字段错误
     */
    @Data
    public static class FieldError {
        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 错误消息
         */
        private String errorMessage;
    }
}
