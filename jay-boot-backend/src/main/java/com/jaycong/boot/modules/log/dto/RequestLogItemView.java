package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

/**
 * 请求日志视图对象。
 * 用于返回请求日志的详细信息。
 *
 * @param id            日志ID
 * @param requestId     请求ID
 * @param userId        用户ID
 * @param username      用户名
 * @param method        请求方法
 * @param path          请求路径
 * @param queryString   查询字符串
 * @param requestParams 请求参数
 * @param statusCode    响应状态码
 * @param durationMs    请求耗时（毫秒）
 * @param clientIp      客户端IP
 * @param userAgent     用户代理
 * @param createdTime   创建时间
 */
public record RequestLogItemView(
        Long id,
        String requestId,
        Long userId,
        String username,
        String method,
        String path,
        String queryString,
        String requestParams,
        Integer statusCode,
        Integer durationMs,
        String clientIp,
        String userAgent,
        LocalDateTime createdTime
) {}
