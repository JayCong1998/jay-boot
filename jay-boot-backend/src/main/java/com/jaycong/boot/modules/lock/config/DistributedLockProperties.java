package com.jaycong.boot.modules.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性
 */
@Data
@ConfigurationProperties(prefix = "distributed-lock")
public class DistributedLockProperties {
    
    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;
    
    /**
     * 锁 key 前缀
     */
    private String keyPrefix = "lock";
    
    /**
     * 默认等待时间（毫秒）
     */
    private long defaultWaitTime = 0;
    
    /**
     * 默认持有时间（毫秒），-1 表示自动续期
     */
    private long defaultLeaseTime = -1;
}
