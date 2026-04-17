package com.jaycong.boot.modules.log.interceptor;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.log.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求日志拦截器。
 * 为每个请求生成唯一的请求ID，并初始化请求上下文。
 */
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    /**
     * 请求处理前：初始化请求上下文。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return 是否继续执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = generateRequestId();
        Long userId = null;
        String username = null;

        try {
            LoginPrincipal principal = LoginContext.currentPrincipal().orElse(null);
            if (principal != null) {
                userId = principal.userId();
                username = principal.username();
            }
        } catch (Exception ignored) {
            // 忽略登录上下文获取异常
        }

        RequestContext.init(requestId, userId, username);
        response.setHeader("X-Request-Id", requestId);
        return true;
    }

    /**
     * 请求完成后：清理请求上下文。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext.clear();
    }

    /**
     * 生成唯一的请求ID。
     *
     * @return 16位请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
