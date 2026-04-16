package com.jaycong.boot.modules.ratelimit.aspect;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.ratelimit.annotation.RateLimit;
import com.jaycong.boot.modules.ratelimit.config.RateLimitProperties;
import com.jaycong.boot.modules.ratelimit.exception.RateLimitException;
import com.jaycong.boot.modules.ratelimit.service.RateLimitService;
import com.jaycong.boot.modules.ratelimit.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    
    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    
    public RateLimitAspect(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }
    
    @Around("@annotation(com.jaycong.boot.modules.ratelimit.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!properties.isEnabled()) {
            return point.proceed();
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        if (rateLimit == null) {
            return point.proceed();
        }
        
        // 获取配置参数
        int qps = rateLimit.qps() > 0 ? rateLimit.qps() : properties.getDefaultQps();
        int burst = rateLimit.burst() > 0 ? rateLimit.burst() : properties.getDefaultBurst();
        String message = rateLimit.message();
        
        // 获取请求路径
        HttpServletRequest request = getCurrentRequest();
        String path = request != null ? request.getRequestURI() : "unknown";
        
        // 获取客户端 IP
        String clientIp = request != null ? ClientIpUtils.getClientIp(request) : "unknown";
        
        // 获取当前用户 ID
        Long userId = null;
        try {
            userId = LoginContext.currentPrincipal()
                    .map(LoginPrincipal::userId)
                    .orElse(null);
        } catch (Exception ignored) {
            // 忽略获取用户信息的异常
        }
        
        // 根据维度执行限流判断
        boolean allowed = checkRateLimit(rateLimit.dimension(), clientIp, userId, path, qps, burst);
        
        if (!allowed) {
            int retryAfter = rateLimitService.calculateRetryAfter(0, qps);
            log.warn("限流触发: ip={}, userId={}, path={}, qps={}, burst={}", 
                    clientIp, userId, path, qps, burst);
            throw new RateLimitException(message, retryAfter);
        }
        
        return point.proceed();
    }
    
    private boolean checkRateLimit(RateLimit.Dimension dimension, String clientIp, 
                                    Long userId, String path, int qps, int burst) {
        boolean ipAllowed = true;
        boolean userAllowed = true;
        
        if (dimension == RateLimit.Dimension.IP || dimension == RateLimit.Dimension.IP_USER) {
            String ipKey = "ip:" + clientIp + ":" + path;
            RateLimitService.RateLimitResult ipResult = rateLimitService.tryAcquire(ipKey, qps, burst);
            ipAllowed = ipResult.allowed();
        }
        
        if (dimension == RateLimit.Dimension.USER || dimension == RateLimit.Dimension.IP_USER) {
            if (userId != null) {
                String userKey = "user:" + userId + ":" + path;
                RateLimitService.RateLimitResult userResult = rateLimitService.tryAcquire(userKey, qps, burst);
                userAllowed = userResult.allowed();
            }
            // 未登录用户不进行用户维度限流
        }
        
        // IP_USER 模式下，两者都通过才算通过
        if (dimension == RateLimit.Dimension.IP_USER) {
            return ipAllowed && userAllowed;
        }
        
        // 单维度模式下，对应维度通过即可
        if (dimension == RateLimit.Dimension.IP) {
            return ipAllowed;
        }
        
        return userAllowed;
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
