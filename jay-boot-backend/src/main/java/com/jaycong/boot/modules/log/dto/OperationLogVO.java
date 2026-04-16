package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class OperationLogVO {
    private String id;
    private String module;
    private String action;
    private String detail;
    private Long userId;
    private String username;
    private String clientIp;
    private String requestId;
    private String createdTime;
}
