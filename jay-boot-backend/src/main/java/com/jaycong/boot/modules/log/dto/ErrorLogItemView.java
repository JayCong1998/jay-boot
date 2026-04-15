package com.jaycong.boot.modules.log.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorLogItemView {

    private Long id;

    private String requestId;

    private Long userId;

    private String username;

    private String requestPath;

    private String requestParams;

    private String clientIp;

    private String exceptionClass;

    private String exceptionMessage;

    private String stackTrace;

    private LocalDateTime createdTime;
}
