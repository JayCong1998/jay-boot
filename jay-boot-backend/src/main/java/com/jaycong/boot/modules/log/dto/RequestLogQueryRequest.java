package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class RequestLogQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String keyword;

    private String method;

    private Integer statusCode;

    private Long userId;

    private String startTime;

    private String endTime;

    public RequestLogQueryRequest() {
    }

    public RequestLogQueryRequest(Integer page, Integer pageSize, String keyword, String method,
                                  Integer statusCode, Long userId, String startTime, String endTime) {
        this.page = page != null ? page : 1;
        this.pageSize = pageSize != null ? pageSize : 20;
        this.keyword = keyword;
        this.method = method;
        this.statusCode = statusCode;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
