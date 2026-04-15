package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RequestLogItemView {

    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String method;

    private String path;

    private String queryString;

    private String requestParams;

    private Integer statusCode;

    private Integer durationMs;

    private String clientIp;

    private String userAgent;

    private LocalDateTime createdTime;
}
