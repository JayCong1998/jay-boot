package com.jaycong.boot.common.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 全局登录拦截配置。
 * 统一拦截 /api/**，并放行认证与健康检查接口。
 */
@Configuration
public class SaTokenGlobalInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/api/**")
                .notMatch("/api/auth/**")
                .notMatch("/api/system/ping")
                .check(r -> {
                    // CORS 预检请求不携带业务 token，需直接放行。
                    if ("OPTIONS".equalsIgnoreCase(SaHolder.getRequest().getMethod())) {
                        return;
                    }
                    StpUtil.checkLogin();
                })))
                .addPathPatterns("/api/**");
    }
}
