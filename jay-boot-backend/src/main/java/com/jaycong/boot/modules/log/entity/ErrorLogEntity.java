package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错误日志实体类。
 * 用于记录系统运行时发生的异常信息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("error_logs")
public class ErrorLogEntity extends BaseEntity {

    /** 请求ID，用于关联请求链路 */
    private String requestId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 请求路径 */
    private String requestPath;

    /** 请求参数（JSON格式） */
    private String requestParams;

    /** 客户端IP地址 */
    private String clientIp;

    /** 异常类名 */
    private String exceptionClass;

    /** 异常消息 */
    private String exceptionMessage;

    /** 堆栈跟踪信息 */
    private String stackTrace;
}
