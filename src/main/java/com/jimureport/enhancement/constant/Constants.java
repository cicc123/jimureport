package com.jimureport.enhancement.constant;

/**
 * 系统常量
 */
public class Constants {

    /**
     * 通用状态：正常
     */
    public static final String STATUS_NORMAL = "0";

    /**
     * 通用状态：停用
     */
    public static final String STATUS_DISABLED = "1";

    /**
     * 删除标志：存在
     */
    public static final String DEL_FLAG_NORMAL = "0";

    /**
     * 删除标志：删除
     */
    public static final String DEL_FLAG_DELETED = "2";

    /**
     * 是否：是
     */
    public static final String YES = "1";

    /**
     * 是否：否
     */
    public static final String NO = "0";

    /**
     * 菜单类型：目录
     */
    public static final String MENU_TYPE_DIR = "M";

    /**
     * 菜单类型：菜单
     */
    public static final String MENU_TYPE_MENU = "C";

    /**
     * 菜单类型：按钮
     */
    public static final String MENU_TYPE_BUTTON = "F";

    /**
     * 数据权限范围：全部
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 数据权限范围：自定义
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 数据权限范围：本部门
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 数据权限范围：本部门及以下
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 数据权限范围：仅本人
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 用户类型：普通用户
     */
    public static final String USER_TYPE_NORMAL = "sys_user";

    /**
     * 超级管理员角色标识
     */
    public static final String SUPER_ADMIN_ROLE_KEY = "admin";

    /**
     * 权限类型：查看
     */
    public static final String PERMISSION_TYPE_VIEW = "view";

    /**
     * 权限类型：设计
     */
    public static final String PERMISSION_TYPE_DESIGN = "design";

    /**
     * 权限类型：填报
     */
    public static final String PERMISSION_TYPE_FILL = "fill";

    /**
     * 权限类型：导出
     */
    public static final String PERMISSION_TYPE_EXPORT = "export";

    /**
     * 权限类型：删除
     */
    public static final String PERMISSION_TYPE_DELETE = "delete";

    /**
     * 值类型：静态
     */
    public static final String VALUE_TYPE_STATIC = "static";

    /**
     * 值类型：用户ID
     */
    public static final String VALUE_TYPE_USER_ID = "user_id";

    /**
     * 值类型：部门ID
     */
    public static final String VALUE_TYPE_DEPT_ID = "dept_id";

    /**
     * 条件类型：等于
     */
    public static final String CONDITION_TYPE_EQ = "eq";

    /**
     * 条件类型：包含
     */
    public static final String CONDITION_TYPE_LIKE = "like";

    /**
     * 条件类型：大于
     */
    public static final String CONDITION_TYPE_GT = "gt";

    /**
     * 条件类型：小于
     */
    public static final String CONDITION_TYPE_LT = "lt";

    /**
     * 条件类型：包含于
     */
    public static final String CONDITION_TYPE_IN = "in";

    /**
     * 性别：男
     */
    public static final String SEX_MALE = "0";

    /**
     * 性别：女
     */
    public static final String SEX_FEMALE = "1";

    /**
     * 性别：未知
     */
    public static final String SEX_UNKNOWN = "2";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 请求头：Token
     */
    public static final String HEADER_TOKEN = "Authorization";

    /**
     * 请求头：Token（积木报表）
     */
    public static final String HEADER_JM_TOKEN = "X-Access-Token";

    /**
     * Redis Key 前缀：登录失败
     */
    public static final String REDIS_KEY_LOGIN_FAIL = "login:fail:";

    /**
     * Redis Key 前缀：用户Token
     */
    public static final String REDIS_KEY_USER_TOKEN = "user:token:";

    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * 用户名最小长度
     */
    public static final int USERNAME_MIN_LENGTH = 4;

    /**
     * 用户名最大长度
     */
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 账号锁定时间（分钟）
     */
    public static final int ACCOUNT_LOCK_MINUTES = 30;

    /**
     * JWT 过期时间（毫秒）：24小时
     */
    public static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000;

    /**
     * 分页默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 分页默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 分页最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
}
