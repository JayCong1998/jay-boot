package com.jaycong.boot.modules.lock.aspect;

import com.jaycong.boot.modules.lock.annotation.DistributedLock;
import com.jaycong.boot.modules.lock.config.DistributedLockProperties;
import com.jaycong.boot.modules.lock.exception.DistributedLockException;
import com.jaycong.boot.modules.lock.util.SpelKeyParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面
 */
@Aspect
@Component
public class DistributedLockAspect {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);
    
    private final RedissonClient redissonClient;
    private final DistributedLockProperties properties;
    
    public DistributedLockAspect(RedissonClient redissonClient, DistributedLockProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
    }
    
    @Around("@annotation(com.jaycong.boot.modules.lock.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!properties.isEnabled()) {
            return point.proceed();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        
        if (distributedLock == null) {
            return point.proceed();
        }
        
        // 解析 SpEL 表达式获取实际 key
        String lockKey = SpelKeyParser.parse(distributedLock.key(), point);
        String fullKey = properties.getKeyPrefix() + ":" + lockKey;
        
        // 获取配置参数
        long waitTime = distributedLock.waitTime() > 0 
                ? distributedLock.waitTime() 
                : properties.getDefaultWaitTime();
        long leaseTime = distributedLock.leaseTime() > 0 
                ? distributedLock.leaseTime() 
                : properties.getDefaultLeaseTime();
        TimeUnit timeUnit = distributedLock.timeUnit();
        
        // 获取锁对象
        RLock lock = getLock(fullKey, distributedLock.lockType());
        
        // 尝试获取锁
        boolean acquired = tryAcquireLock(lock, waitTime, leaseTime, timeUnit);
        
        if (!acquired) {
            log.warn("分布式锁获取失败: key={}, waitTime={}ms", fullKey, waitTime);
            throw new DistributedLockException(distributedLock.message());
        }
        
        log.debug("分布式锁获取成功: key={}", fullKey);
        
        try {
            // 执行业务方法
            return point.proceed();
        } finally {
            // 释放锁（仅当前线程持有）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("分布式锁释放成功: key={}", fullKey);
            }
        }
    }
    
    /**
     * 根据锁类型获取对应的锁对象
     */
    private RLock getLock(String key, DistributedLock.LockType lockType) {
        return switch (lockType) {
            case FAIR -> redissonClient.getFairLock(key);
            case READ -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.readLock();
            }
            case WRITE -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.writeLock();
            }
            default -> redissonClient.getLock(key);
        };
    }
    
    /**
     * 尝试获取锁
     */
    private boolean tryAcquireLock(RLock lock, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        if (leaseTime > 0) {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } else {
            // leaseTime <= 0 时使用自动续期
            return lock.tryLock(waitTime, timeUnit);
        }
    }
}
