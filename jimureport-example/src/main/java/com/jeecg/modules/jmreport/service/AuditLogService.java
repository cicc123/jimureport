package com.jeecg.modules.jmreport.service;

import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {

    private Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 记录登录日志
     */
    public void logLogin(String username, String ip, boolean success, String message) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            // 尝试获取用户ID，如果不存在使用占位符
            String userId = "unknown";
            try {
                String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                List<String> results = jdbcTemplate.queryForList(userIdSql, String.class, username);
                if (!results.isEmpty()) {
                    userId = results.get(0);
                }
            } catch (Exception e) {
                // 用户不存在，使用unknown
            }
            
            jdbcTemplate.update(sql, id, userId, username, "1", "登录", success ? "登录成功" : "登录失败: " + message, ip);
        } catch (Exception e) {
            logger.error("记录登录日志失败：", e);
        }
    }

    /**
     * 记录密码修改日志
     */
    public void logPasswordChange(String username, String ip, boolean success, String message) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            // 获取用户ID
            String userId = null;
            try {
                String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);
            } catch (Exception e) {
                // 用户不存在，使用空值
            }
            
            jdbcTemplate.update(sql, id, userId, username, "1", "修改密码", success ? "密码修改成功" : "密码修改失败: " + message, ip);
        } catch (Exception e) {
            logger.error("记录密码修改日志失败：", e);
        }
    }

    /**
     * 记录用户操作日志
     */
    public void logUserOperation(String username, String ip, String operation, String content) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            // 获取用户ID
            String userId = null;
            try {
                String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);
            } catch (Exception e) {
                // 用户不存在，使用空值
            }
            
            jdbcTemplate.update(sql, id, userId, username, "1", operation, content, ip);
        } catch (Exception e) {
            logger.error("记录用户操作日志失败：", e);
        }
    }

    /**
     * 记录角色操作日志
     */
    public void logRoleOperation(String username, String ip, String operation, String content) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            // 获取用户ID
            String userId = null;
            try {
                String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);
            } catch (Exception e) {
                // 用户不存在，使用空值
            }
            
            jdbcTemplate.update(sql, id, userId, username, "1", operation, content, ip);
        } catch (Exception e) {
            logger.error("记录角色操作日志失败：", e);
        }
    }

    /**
     * 记录权限操作日志
     */
    public void logPermissionOperation(String username, String ip, String operation, String content) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            // 获取用户ID
            String userId = null;
            try {
                String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);
            } catch (Exception e) {
                // 用户不存在，使用空值
            }
            
            jdbcTemplate.update(sql, id, userId, username, "1", operation, content, ip);
        } catch (Exception e) {
            logger.error("记录权限操作日志失败：", e);
        }
    }

    /**
     * 记录表单操作日志
     * @param formId 表单ID
     * @param operation 操作类型
     * @param content 操作内容
     * @param ip IP地址
     */
    public void logFormOperation(String formId, String operation, String content, String ip) {
        String id = UUID.randomUUID().toString();
        String sql = "INSERT INTO jimu_audit_log (id, user_id, username, tenant_id, operation, content, ip, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try {
            String username = "anonymous";
            String userId = null;
            
            try {
                if (StpUtil.isLogin()) {
                    username = StpUtil.getLoginIdAsString();
                    String userIdSql = "SELECT id FROM jimu_user WHERE username = ?";
                    userId = jdbcTemplate.queryForObject(userIdSql, String.class, username);
                }
            } catch (Exception e) {
                logger.warn("获取用户信息失败: {}", e.getMessage());
            }
            
            String fullContent = "表单ID: " + formId + " - " + content;
            jdbcTemplate.update(sql, id, userId, username, "1", operation, fullContent, ip);
        } catch (Exception e) {
            logger.error("记录表单操作日志失败：", e);
        }
    }

    /**
     * 记录审计日志（测试用）
     */
    public void log(String operation, String content, String ip) {
        String username = StpUtil.getLoginIdAsString();
        logUserOperation(username, ip, operation, content);
    }

    /**
     * 获取审计日志列表（测试用）
     */
    public List<Map<String, Object>> getAuditLogs(int page, int pageSize, String username, String operation, String startTime, String endTime) {
        StringBuilder sql = new StringBuilder("SELECT * FROM jimu_audit_log WHERE 1=1");
        
        if (username != null && !username.isEmpty()) {
            sql.append(" AND username = ?");
        }
        if (operation != null && !operation.isEmpty()) {
            sql.append(" AND operation = ?");
        }
        
        sql.append(" ORDER BY create_time DESC");
        sql.append(" LIMIT ? OFFSET ?");
        
        int offset = (page - 1) * pageSize;
        
        if (username != null && !username.isEmpty() && operation != null && !operation.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), username, operation, pageSize, offset);
        } else if (username != null && !username.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), username, pageSize, offset);
        } else if (operation != null && !operation.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), operation, pageSize, offset);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), pageSize, offset);
        }
    }
}
