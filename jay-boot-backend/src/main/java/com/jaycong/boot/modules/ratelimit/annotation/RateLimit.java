package com.jaycong.boot.modules.ratelimit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 限流注解
 * 标注在 Controller 方法上，定义限流规则
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 每秒补充令牌数（QPS）
     * 0 表示使用配置文件默认值
     */
    int qps() default 0;
    
    /**
     * 桶容量（允许突发请求数）
     * 0 表示使用配置文件默认值
     */
    int burst() default 0;
    
    /**
     * 限流维度
     */
    Dimension dimension() default Dimension.IP_USER;
    
    /**
     * 自定义提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流维度枚举
     */
    enum Dimension {
        /** 仅 IP 维度 */
        IP,
        /** 仅用户维度 */
        USER,
        /** IP 和用户双重限流（两者独立） */
        IP_USER
    }
}
