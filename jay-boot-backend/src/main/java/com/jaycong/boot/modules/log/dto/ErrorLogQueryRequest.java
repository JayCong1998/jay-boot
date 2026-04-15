package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class ErrorLogQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String keyword;

    private String requestPath;

    private Long userId;

    private String startTime;

    private String endTime;

    public ErrorLogQueryRequest() {
    }

    public ErrorLogQueryRequest(Integer page, Integer pageSize, String keyword, String requestPath,
                                Long userId, String startTime, String endTime) {
        this.page = page != null ? page : 1;
        this.pageSize = pageSize != null ? pageSize : 20;
        this.keyword = keyword;
        this.requestPath = requestPath;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
