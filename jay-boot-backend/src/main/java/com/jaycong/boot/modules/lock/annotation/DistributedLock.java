package com.jaycong.boot.modules.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 标注在方法上，实现分布式互斥控制
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    
    /**
     * 锁的 key，支持完整 SpEL 表达式
     * 示例: "'order:' + #orderId" 或 "'payment:' + #request.userId"
     */
    String key();
    
    /**
     * 锁等待时间，0 表示不等待立即失败
     * 默认值 0，可通过配置文件修改默认值
     */
    long waitTime() default 0;
    
    /**
     * 锁持有时间（毫秒），-1 表示持有直到方法执行完成（自动续期）
     * 默认值 -1，可通过配置文件修改默认值
     */
    long leaseTime() default -1;
    
    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
    
    /**
     * 锁类型
     */
    LockType lockType() default LockType.REENTRANT;
    
    /**
     * 获取锁失败的提示消息
     */
    String message() default "操作过于频繁，请稍后再试";
    
    /**
     * 锁类型枚举
     */
    enum LockType {
        /** 可重入锁（默认） */
        REENTRANT,
        /** 公平锁 */
        FAIR,
        /** 读写锁 - 读锁 */
        READ,
        /** 读写锁 - 写锁 */
        WRITE
    }
}
