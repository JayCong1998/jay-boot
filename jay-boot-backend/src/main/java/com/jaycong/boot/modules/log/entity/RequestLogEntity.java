package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("request_logs")
public class RequestLogEntity extends BaseEntity {

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
}
