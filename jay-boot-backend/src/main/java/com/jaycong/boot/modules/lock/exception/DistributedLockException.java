package com.jaycong.boot.modules.lock.exception;

/**
 * 分布式锁获取失败异常
 */
public class DistributedLockException extends RuntimeException {
    
    public DistributedLockException(String message) {
        super(message);
    }
}
