package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs")
public class OperationLogEntity extends BaseEntity {

    private String module;

    private String action;

    private String detail;

    private Long userId;

    private String username;

    private String clientIp;

    private String requestId;
}
