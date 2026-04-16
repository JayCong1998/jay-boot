package com.jaycong.boot.modules.lock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 */
@Configuration
@EnableConfigurationProperties({DistributedLockProperties.class})
public class RedissonConfig {
    
    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        
        String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty() 
                        ? redisProperties.getPassword() 
                        : null)
                .setDatabase(redisProperties.getDatabase())
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000);
        
        return Redisson.create(config);
    }
}
