package com.jaycong.boot.modules.ratelimit.service;

import com.jaycong.boot.modules.ratelimit.config.RateLimitProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 限流服务
 * 使用 Redis + Lua 脚本实现令牌桶算法
 */
@Service
public class RateLimitService {
    
    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    
    /**
     * 令牌桶 Lua 脚本
     * KEYS[1]: 限流 key
     * ARGV[1]: 桶容量 (burst)
     * ARGV[2]: 每秒补充令牌数 (qps)
     * ARGV[3]: 当前时间戳 (毫秒)
     * ARGV[4]: 请求令牌数 (默认1)
     * 返回: [是否成功(1/0), 剩余令牌数]
     */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local burst = tonumber(ARGV[1])
            local qps = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            
            -- 获取当前令牌数和上次更新时间
            local info = redis.call('HMGET', key, 'tokens', 'last_time')
            local tokens = tonumber(info[1])
            local lastTime = tonumber(info[2])
            
            -- 初始化
            if tokens == nil then
                tokens = burst
                lastTime = now
            end
            
            -- 计算时间间隔（秒）
            local interval = (now - lastTime) / 1000.0
            if interval < 0 then
                interval = 0
            end
            
            -- 补充令牌
            local filled = tokens + interval * qps
            if filled > burst then
                filled = burst
            end
            
            -- 尝试获取令牌
            local allowed = 0
            local remaining = filled
            if filled >= requested then
                filled = filled - requested
                allowed = 1
                remaining = filled
            end
            
            -- 更新状态
            redis.call('HMSET', key, 'tokens', filled, 'last_time', now)
            redis.call('PEXPIRE', key, math.ceil(burst / qps * 2 * 1000))
            
            return {allowed, math.floor(remaining)}
            """;
    
    public RateLimitService(StringRedisTemplate redisTemplate, RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }
    
    /**
     * 尝试获取令牌
     *
     * @param dimensionKey 限流维度 key (如 ip:127.0.0.1:/api/login)
     * @param qps          每秒补充令牌数
     * @param burst        桶容量
     * @return 限流结果
     */
    public RateLimitResult tryAcquire(String dimensionKey, int qps, int burst) {
        String key = properties.getRedisKeyPrefix() + ":" + dimensionKey;
        long now = System.currentTimeMillis();
        
        DefaultRedisScript<List> script = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT, List.class);
        List<Long> result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                String.valueOf(burst),
                String.valueOf(qps),
                String.valueOf(now),
                "1"
        );
        
        if (result == null || result.size() < 2) {
            return new RateLimitResult(false, 0, qps, burst);
        }
        
        boolean allowed = result.get(0) != null && result.get(0) == 1L;
        int remaining = result.get(1) != null ? result.get(1).intValue() : 0;
        
        return new RateLimitResult(allowed, remaining, qps, burst);
    }
    
    /**
     * 计算建议重试等待时间（秒）
     *
     * @param remaining 剩余令牌数
     * @param qps       每秒补充令牌数
     * @return 等待秒数
     */
    public int calculateRetryAfter(int remaining, int qps) {
        if (remaining > 0 || qps <= 0) {
            return 1;
        }
        // 需要等待 1 个令牌补充
        return (int) Math.ceil(1.0 / qps);
    }
    
    /**
     * 限流结果
     */
    public record RateLimitResult(boolean allowed, int remaining, int qps, int burst) {
    }
}
