package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

/**
 * 错误日志视图对象。
 * 用于返回错误日志的详细信息。
 *
 * @param id               日志ID
 * @param requestId        请求ID
 * @param userId           用户ID
 * @param username         用户名
 * @param requestPath      请求路径
 * @param requestParams    请求参数
 * @param clientIp         客户端IP
 * @param exceptionClass   异常类名
 * @param exceptionMessage 异常消息
 * @param stackTrace       堆栈跟踪
 * @param createdTime      创建时间
 */
public record ErrorLogItemView(
        Long id,
        String requestId,
        Long userId,
        String username,
        String requestPath,
        String requestParams,
        String clientIp,
        String exceptionClass,
        String exceptionMessage,
        String stackTrace,
        LocalDateTime createdTime
) {}
