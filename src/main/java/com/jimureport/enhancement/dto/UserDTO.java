package com.jimureport.enhancement.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {

    /**
     * 用户ID（修改时必填）
     */
    private String id;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 30, message = "用户名长度必须在2-30个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
    private String username;

    /**
     * 用户昵称
     */
    @Size(max = 30, message = "昵称长度不能超过30个字符")
    private String nickname;

    /**
     * 用户邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 用户性别（0男 1女 2未知）
     */
    @Pattern(regexp = "^[012]$", message = "性别值不正确")
    private String sex;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 密码
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Pattern(regexp = "^[01]$", message = "状态值不正确")
    private String status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 角色ID列表
     */
    private List<String> roleIds;
}
