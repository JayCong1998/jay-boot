package com.jaycong.boot.modules.log.dto;

/**
 * 操作日志视图对象。
 * 用于返回操作日志记录的详细信息。
 */
public record OperationLogVO(
        String id,
        String module,
        String action,
        String detail,
        Long userId,
        String username,
        String clientIp,
        String requestId,
        String createdTime
) {}
