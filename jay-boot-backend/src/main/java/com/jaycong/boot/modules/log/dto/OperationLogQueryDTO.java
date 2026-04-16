package com.jaycong.boot.modules.log.dto;

import lombok.Data;

@Data
public class OperationLogQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 20;
    private String module;
    private Long userId;
    private String startTime;
    private String endTime;
}
