package com.jaycong.boot.modules.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订阅实体，映射 subscriptions 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subscriptions")
public class SubscriptionEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    private Long planId;

    private String status;

    private LocalDateTime trialEndAt;

    private LocalDateTime currentPeriodEnd;

    private LocalDateTime effectiveTime;

    private LocalDateTime cancelTime;
}
