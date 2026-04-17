package com.jaycong.boot.modules.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体类。
 * 用于记录用户的关键业务操作行为。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_logs")
public class OperationLogEntity extends BaseEntity {

    /** 模块名称（如：用户管理、套餐管理） */
    private String module;

    /** 操作类型（如：创建、删除、修改） */
    private String action;

    /** 操作详情（支持SpEL表达式的模板） */
    private String detail;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 客户端IP地址 */
    private String clientIp;

    /** 请求ID，用于关联请求链路 */
    private String requestId;
}
