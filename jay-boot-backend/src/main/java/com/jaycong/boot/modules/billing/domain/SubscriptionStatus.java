package com.jaycong.boot.modules.billing.domain;

/**
 * 订阅状态枚举。
 */
public enum SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED
}
