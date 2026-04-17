package com.jaycong.boot.modules.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;

import com.jaycong.boot.modules.log.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求日志切面类。
 * 通过AOP自动拦截所有RestController的请求，记录访问日志。
 */
@Aspect
@Component
public class RequestLogAspect {

    private final RequestLogService requestLogService;
    private final LogProperties logProperties;
    private final ObjectMapper objectMapper;

    public RequestLogAspect(RequestLogService requestLogService, LogProperties logProperties, ObjectMapper objectMapper) {
        this.requestLogService = requestLogService;
        this.logProperties = logProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 环绕通知：拦截所有RestController的请求并记录日志。
     *
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!logProperties.getRequest().isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        RequestContext context = RequestContext.current();
        if (context == null) {
            return joinPoint.proceed();
        }

        Object result = null;
        int statusCode = 200;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            statusCode = 500;
            throw ex;
        } finally {
            try {
                saveRequestLog(request, context, statusCode);
            } catch (Exception ignored) {
                // 日志记录失败不影响业务
            }
        }

        return result;
    }

    /**
     * 保存请求日志。
     *
     * @param request    HTTP请求
     * @param context    请求上下文
     * @param statusCode 响应状态码
     */
    private void saveRequestLog(HttpServletRequest request, RequestContext context, int statusCode) {
        RequestLogEntity entity = new RequestLogEntity();
        entity.setRequestId(context.getRequestId());
        entity.setUserId(context.getUserId());
        entity.setUsername(context.getUsername());
        entity.setMethod(request.getMethod());
        entity.setPath(request.getRequestURI());
        entity.setQueryString(request.getQueryString());
        entity.setRequestParams(extractRequestParams(request));
        entity.setStatusCode(statusCode);
        entity.setDurationMs((int) context.getDurationMillis());
        entity.setClientIp(getClientIp(request));
        entity.setUserAgent(request.getHeader("User-Agent"));
        entity.setCreatorId(context.getUserId() != null ? context.getUserId() : 0L);
        entity.setCreatorName(context.getUsername() != null ? context.getUsername() : "system");
        entity.setUpdaterId(entity.getCreatorId());
        entity.setUpdaterName(entity.getCreatorName());

        requestLogService.save(entity);
    }

    /**
     * 提取请求参数。
     *
     * @param request HTTP请求
     * @return JSON格式的请求参数
     */
    private String extractRequestParams(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String[] values = entry.getValue();
                if (values.length == 1) {
                    params.put(entry.getKey(), values[0]);
                } else {
                    params.put(entry.getKey(), values);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址。
     * 支持反向代理场景（X-Forwarded-For、X-Real-IP）。
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前HTTP请求。
     *
     * @return 当前HTTP请求，如果不存在则返回null
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
