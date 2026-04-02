-- H2 兼容测试 Schema（MySQL 语法兼容模式）

-- 用户表
CREATE TABLE sys_user (
    id VARCHAR(32) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    sex CHAR(1) DEFAULT '2',
    avatar VARCHAR(200) DEFAULT NULL,
    dept_id VARCHAR(32) DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    login_ip VARCHAR(50) DEFAULT NULL,
    login_date TIMESTAMP DEFAULT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (username)
);

-- 部门表
CREATE TABLE sys_department (
    id VARCHAR(32) NOT NULL,
    dept_name VARCHAR(50) NOT NULL,
    parent_id VARCHAR(32) DEFAULT '0',
    ancestors VARCHAR(500) DEFAULT NULL,
    order_num INT DEFAULT 0,
    leader VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 角色表
CREATE TABLE sys_role (
    id VARCHAR(32) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_key VARCHAR(100) NOT NULL,
    role_sort INT NOT NULL DEFAULT 0,
    data_scope CHAR(1) DEFAULT '1',
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
);

-- 菜单表
CREATE TABLE sys_menu (
    id VARCHAR(32) NOT NULL,
    menu_name VARCHAR(50) NOT NULL,
    parent_id VARCHAR(32) DEFAULT '0',
    order_num INT DEFAULT 0,
    path VARCHAR(200) DEFAULT NULL,
    component VARCHAR(200) DEFAULT NULL,
    is_frame CHAR(1) DEFAULT '1',
    is_cache CHAR(1) DEFAULT '0',
    menu_type CHAR(1) DEFAULT NULL,
    visible CHAR(1) DEFAULT '0',
    status CHAR(1) DEFAULT '0',
    perms VARCHAR(100) DEFAULT NULL,
    icon VARCHAR(100) DEFAULT '#',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
);

-- 用户角色关联表
CREATE TABLE sys_user_role (
    user_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    role_id VARCHAR(32) NOT NULL,
    menu_id VARCHAR(32) NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 报表权限表
CREATE TABLE jimu_report_permission (
    id VARCHAR(32) NOT NULL,
    report_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) DEFAULT NULL,
    user_id VARCHAR(32) DEFAULT NULL,
    permission_type VARCHAR(20) NOT NULL,
    data_scope VARCHAR(20) DEFAULT 'all',
    data_scope_rule VARCHAR(2000) DEFAULT NULL,
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 数据权限表
CREATE TABLE jimu_data_permission (
    id VARCHAR(32) NOT NULL,
    report_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) DEFAULT NULL,
    user_id VARCHAR(32) DEFAULT NULL,
    table_name VARCHAR(100) NOT NULL,
    condition_field VARCHAR(100) NOT NULL,
    condition_type VARCHAR(20) NOT NULL,
    condition_value VARCHAR(500) DEFAULT NULL,
    value_type VARCHAR(20) DEFAULT 'static',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 填报表单配置表
CREATE TABLE jimu_fill_form_config (
    id VARCHAR(32) NOT NULL,
    report_id VARCHAR(32) NOT NULL,
    form_name VARCHAR(100) NOT NULL,
    form_desc VARCHAR(500) DEFAULT NULL,
    submit_type VARCHAR(20) DEFAULT 'database',
    submit_config VARCHAR(2000) DEFAULT NULL,
    allow_draft CHAR(1) DEFAULT '0',
    allow_duplicate CHAR(1) DEFAULT '0',
    duplicate_check_fields VARCHAR(500) DEFAULT NULL,
    success_message VARCHAR(200) DEFAULT '提交成功',
    redirect_url VARCHAR(500) DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (report_id)
);

-- 填报表单字段配置表
CREATE TABLE jimu_fill_form_field (
    id VARCHAR(32) NOT NULL,
    form_id VARCHAR(32) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    data_type VARCHAR(20) DEFAULT 'string',
    data_source_type VARCHAR(20) DEFAULT 'manual',
    data_source_config VARCHAR(2000) DEFAULT NULL,
    default_value VARCHAR(500) DEFAULT NULL,
    default_value_type VARCHAR(20) DEFAULT 'static',
    is_required CHAR(1) DEFAULT '0',
    validation_rule VARCHAR(2000) DEFAULT NULL,
    placeholder VARCHAR(200) DEFAULT NULL,
    help_text VARCHAR(500) DEFAULT NULL,
    min_value DECIMAL(20,6) DEFAULT NULL,
    max_value DECIMAL(20,6) DEFAULT NULL,
    min_length INT DEFAULT NULL,
    max_length INT DEFAULT NULL,
    order_num INT DEFAULT 0,
    is_visible CHAR(1) DEFAULT '1',
    is_readonly CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 填报数据草稿表
CREATE TABLE jimu_fill_draft (
    id VARCHAR(32) NOT NULL,
    form_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    draft_data VARCHAR(4000) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (form_id, user_id)
);

-- 填报提交记录表
CREATE TABLE jimu_fill_submit_record (
    id VARCHAR(32) NOT NULL,
    form_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    submit_data VARCHAR(4000) NOT NULL,
    submit_status VARCHAR(20) DEFAULT 'success',
    error_message VARCHAR(2000) DEFAULT NULL,
    ip_address VARCHAR(50) DEFAULT NULL,
    user_agent VARCHAR(500) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 报表字段默认值配置表
CREATE TABLE jimu_report_field_default (
    id VARCHAR(32) NOT NULL,
    report_id VARCHAR(32) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_label VARCHAR(100) DEFAULT NULL,
    default_type VARCHAR(20) NOT NULL,
    default_value VARCHAR(500) DEFAULT NULL,
    is_hidden CHAR(1) DEFAULT '0',
    is_readonly CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 登录日志表
CREATE TABLE sys_login_log (
    id VARCHAR(32) NOT NULL,
    username VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL,
    ip_address VARCHAR(50) DEFAULT NULL,
    login_location VARCHAR(100) DEFAULT NULL,
    browser VARCHAR(100) DEFAULT NULL,
    os VARCHAR(100) DEFAULT NULL,
    message VARCHAR(500) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 操作日志表
CREATE TABLE sys_oper_log (
    id VARCHAR(32) NOT NULL,
    title VARCHAR(100) DEFAULT NULL,
    business_type INT DEFAULT 0,
    method VARCHAR(200) DEFAULT NULL,
    request_method VARCHAR(20) DEFAULT NULL,
    operator_type INT DEFAULT 0,
    oper_name VARCHAR(50) DEFAULT NULL,
    dept_name VARCHAR(100) DEFAULT NULL,
    oper_url VARCHAR(500) DEFAULT NULL,
    oper_ip VARCHAR(50) DEFAULT NULL,
    oper_location VARCHAR(100) DEFAULT NULL,
    oper_param VARCHAR(2000) DEFAULT NULL,
    json_result VARCHAR(2000) DEFAULT NULL,
    status INT DEFAULT 0,
    error_msg VARCHAR(2000) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 系统配置表
CREATE TABLE sys_config (
    id VARCHAR(32) NOT NULL,
    config_name VARCHAR(100) DEFAULT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(2000) DEFAULT NULL,
    config_type CHAR(1) DEFAULT 'N',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (config_key)
);
