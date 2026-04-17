package com.jaycong.boot.modules.log.dto;

/**
 * 请求日志查询请求。
 * 用于分页查询请求日志列表。
 *
 * @param page       页码（默认1）
 * @param pageSize   每页大小（默认20）
 * @param keyword    关键词（搜索请求路径）
 * @param method     请求方法
 * @param statusCode 响应状态码
 * @param userId     用户ID
 * @param startTime  开始时间
 * @param endTime    结束时间
 */
public record RequestLogQueryRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        String method,
        Integer statusCode,
        Long userId,
        String startTime,
        String endTime
) {
    /**
     * 默认构造函数，提供默认分页参数。
     */
    public RequestLogQueryRequest {
        page = page != null ? page : 1;
        pageSize = pageSize != null ? pageSize : 20;
    }
}
