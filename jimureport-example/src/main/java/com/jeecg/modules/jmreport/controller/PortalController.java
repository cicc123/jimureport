package com.jeecg.modules.jmreport.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jeecg.modules.jmreport.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 报表门户控制器
 */
@Controller
public class PortalController {

    private Logger logger = LoggerFactory.getLogger(PortalController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取报表列表
     */
    @GetMapping("/api/portal/reports")
    @ResponseBody
    public List<Map<String, Object>> getReports() {
        if (!StpUtil.isLogin()) {
            return Collections.emptyList();
        }
        
        try {
            // 暂时让所有用户都能看到所有报表，以便测试
            String sql = "SELECT id, name, description, create_time FROM jimu_report WHERE status = 1";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            logger.error("获取报表列表失败：" + e.getMessage());
            return Collections.emptyList();
        }
    }
}
