package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("error_logs")
public class ErrorLogEntity extends BaseEntity {

    private String requestId;

    private Long userId;

    private String username;

    private String requestPath;

    private String requestParams;

    private String clientIp;

    private String exceptionClass;

    private String exceptionMessage;

    private String stackTrace;
}
