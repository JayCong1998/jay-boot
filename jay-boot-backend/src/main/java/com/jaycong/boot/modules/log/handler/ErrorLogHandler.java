package com.jaycong.boot.modules.log.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;

import com.jaycong.boot.modules.log.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorLogHandler {

    private final ErrorLogService errorLogService;
    private final LogProperties logProperties;
    private final ObjectMapper objectMapper;

    public ErrorLogHandler(ErrorLogService errorLogService, LogProperties logProperties, ObjectMapper objectMapper) {
        this.errorLogService = errorLogService;
        this.logProperties = logProperties;
        this.objectMapper = objectMapper;
    }

    public void recordError(Throwable ex) {
        if (!logProperties.getError().isEnabled()) {
            return;
        }

        // 只记录已登录用户的异常日志
        if (LoginContext.currentPrincipal().isEmpty()) {
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        RequestContext context = RequestContext.current();

        ErrorLogEntity entity = new ErrorLogEntity();
        entity.setRequestId(context != null ? context.getRequestId() : null);
        entity.setUserId(context != null ? context.getUserId() : null);
        entity.setUsername(context != null ? context.getUsername() : null);
        entity.setRequestPath(request != null ? request.getRequestURI() : null);
        entity.setRequestParams(request != null ? extractRequestParams(request) : null);
        entity.setClientIp(request != null ? getClientIp(request) : null);
        entity.setExceptionClass(ex.getClass().getName());
        entity.setExceptionMessage(ex.getMessage());
        entity.setStackTrace(getStackTrace(ex));
        entity.setCreatorId(context != null && context.getUserId() != null ? context.getUserId() : 0L);
        entity.setCreatorName(context != null && context.getUsername() != null ? context.getUsername() : "system");
        entity.setUpdaterId(entity.getCreatorId());
        entity.setUpdaterName(entity.getCreatorName());

        errorLogService.save(entity);
    }

    private String extractRequestParams(HttpServletRequest request) {
        try {
            var paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                return null;
            }
            Map<String, Object> params = new HashMap<>();
            for (var entry : paramMap.entrySet()) {
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

    private String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
