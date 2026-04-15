package com.jaycong.boot.modules.log.config;

import com.jaycong.boot.modules.log.interceptor.RequestLogInterceptor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;

    public LogAutoConfiguration(RequestLogInterceptor requestLogInterceptor) {
        this.requestLogInterceptor = requestLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/auth/**", "/api/health/**");
    }
}
