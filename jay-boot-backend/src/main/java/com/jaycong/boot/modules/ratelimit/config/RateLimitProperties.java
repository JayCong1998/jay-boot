package com.jaycong.boot.modules.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置属性
 */
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    
    /**
     * 是否启用限流
     */
    private boolean enabled = true;
    
    /**
     * 默认每秒补充令牌数
     */
    private int defaultQps = 10;
    
    /**
     * 默认桶容量
     */
    private int defaultBurst = 20;
    
    /**
     * Redis key 前缀
     */
    private String redisKeyPrefix = "rate_limit";
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getDefaultQps() {
        return defaultQps;
    }
    
    public void setDefaultQps(int defaultQps) {
        this.defaultQps = defaultQps;
    }
    
    public int getDefaultBurst() {
        return defaultBurst;
    }
    
    public void setDefaultBurst(int defaultBurst) {
        this.defaultBurst = defaultBurst;
    }
    
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }
    
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }
}
