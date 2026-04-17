package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请求日志实体类。
 * 用于记录HTTP请求的访问日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("request_logs")
public class RequestLogEntity extends BaseEntity {

    /** 请求ID，唯一标识一次请求 */
    private String requestId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** HTTP请求方法（GET/POST等） */
    private String method;

    /** 请求路径 */
    private String path;

    /** 查询字符串 */
    private String queryString;

    /** 请求参数（JSON格式） */
    private String requestParams;

    /** HTTP响应状态码 */
    private Integer statusCode;

    /** 请求处理耗时（毫秒） */
    private Integer durationMs;

    /** 客户端IP地址 */
    private String clientIp;

    /** User-Agent请求头 */
    private String userAgent;
}
