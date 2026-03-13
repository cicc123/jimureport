package com.jeecg.modules.jmreport.controller;

import com.jeecg.modules.jmreport.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getRoleList() {
        try {
            String sql = "SELECT id, role_name, role_code, tenant_id, status FROM jimu_role ORDER BY id";
            List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql);
            return Result.success(roles);
        } catch (Exception e) {
            logger.error("获取角色列表失败: {}", e.getMessage());
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRoleById(@PathVariable String id) {
        try {
            String sql = "SELECT id, role_name, role_code, description, sort_order, status FROM jimu_role WHERE id = ?";
            Map<String, Object> role = jdbcTemplate.queryForMap(sql, id);
            return Result.success(role);
        } catch (Exception e) {
            logger.error("获取角色失败: {}", e.getMessage());
            return Result.error("获取角色失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Void> createRole(@RequestBody Map<String, Object> role) {
        try {
            String sql = "INSERT INTO jimu_role (id, role_name, role_code, description, sort_order, status, tenant_id, create_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                java.util.UUID.randomUUID().toString(),
                role.get("role_name"),
                role.get("role_code"),
                role.get("description"),
                role.get("sort_order"),
                role.get("status"),
                role.get("tenant_id"),
                role.get("create_by")
            );
            return Result.success();
        } catch (Exception e) {
            logger.error("创建角色失败: {}", e.getMessage());
            return Result.error("创建角色失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<Void> updateRole(@RequestBody Map<String, Object> role) {
        try {
            String sql = "UPDATE jimu_role SET role_name = ?, role_code = ?, description = ?, sort_order = ?, status = ?, update_by = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                role.get("role_name"),
                role.get("role_code"),
                role.get("description"),
                role.get("sort_order"),
                role.get("status"),
                role.get("update_by"),
                role.get("id")
            );
            return Result.success();
        } catch (Exception e) {
            logger.error("更新角色失败: {}", e.getMessage());
            return Result.error("更新角色失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestBody Map<String, String> params) {
        try {
            String id = params.get("id");
            String sql = "DELETE FROM jimu_role WHERE id = ?";
            jdbcTemplate.update(sql, id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除角色失败: {}", e.getMessage());
            return Result.error("删除角色失败: " + e.getMessage());
        }
    }
}
