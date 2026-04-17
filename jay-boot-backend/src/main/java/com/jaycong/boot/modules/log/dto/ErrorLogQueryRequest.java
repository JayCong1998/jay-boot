package com.jaycong.boot.modules.log.dto;

/**
 * 错误日志查询请求。
 * 用于分页查询错误日志列表。
 *
 * @param page       页码（默认1）
 * @param pageSize   每页大小（默认20）
 * @param keyword    关键词（搜索异常类名或消息）
 * @param requestPath 请求路径
 * @param userId     用户ID
 * @param startTime  开始时间
 * @param endTime    结束时间
 */
public record ErrorLogQueryRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        String requestPath,
        Long userId,
        String startTime,
        String endTime
) {
    /**
     * 默认构造函数，提供默认分页参数。
     */
    public ErrorLogQueryRequest {
        page = page != null ? page : 1;
        pageSize = pageSize != null ? pageSize : 20;
    }
}
