package com.jaycong.boot.modules.ratelimit.exception;

import com.jaycong.boot.common.exception.ErrorCode;

/**
 * 限流异常
 */
public class RateLimitException extends RuntimeException {
    
    private final int code;
    private final int retryAfter;
    
    public RateLimitException(String message) {
        super(message);
        this.code = ErrorCode.TOO_MANY_REQUESTS.getCode();
        this.retryAfter = 1;
    }
    
    public RateLimitException(String message, int retryAfter) {
        super(message);
        this.code = ErrorCode.TOO_MANY_REQUESTS.getCode();
        this.retryAfter = retryAfter;
    }
    
    public int getCode() {
        return code;
    }
    
    public int getRetryAfter() {
        return retryAfter;
    }
}
