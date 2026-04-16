package com.jaycong.boot.modules.ratelimit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 限流自动配置
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {
}
