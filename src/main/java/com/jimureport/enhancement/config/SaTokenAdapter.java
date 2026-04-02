package com.jimureport.enhancement.config;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 操作适配器
 * 封装 StpUtil 静态调用，便于单元测试时 mock
 */
@Component
public class SaTokenAdapter {

    public Object getLoginId() {
        return StpUtil.getLoginId();
    }

    public Object getLoginIdByToken(String token) {
        return StpUtil.getLoginIdByToken(token);
    }

    public boolean isLogin() {
        return StpUtil.isLogin();
    }
}
