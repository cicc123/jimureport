package com.jeecg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 配置首页
        registry.addViewController("/").setViewName("forward:/index.html");
        // 配置用户管理页面
        registry.addViewController("/user/list").setViewName("forward:/user/list.html");
        // 配置用户组管理页面
        registry.addViewController("/group/list").setViewName("forward:/group/list.html");
        // 配置角色管理页面
        registry.addViewController("/role/list").setViewName("forward:/role/list.html");
        // 配置权限管理页面
        registry.addViewController("/permission/list").setViewName("forward:/permission/list.html");
        registry.addViewController("/permission/index").setViewName("forward:/permission/index.html");
        // 配置表单分配页面
        registry.addViewController("/formAssign/index").setViewName("forward:/formAssign/index.html");
        // 配置门户页面
        registry.addViewController("/portal/index").setViewName("forward:/portal/index.html");
    }
}
