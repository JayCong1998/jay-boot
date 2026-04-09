package com.jaycong.boot.modules.billing.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import com.jaycong.boot.modules.billing.domain.SubscriptionStatus;
import com.jaycong.boot.modules.billing.dto.PlanView;
import com.jaycong.boot.modules.billing.dto.SubscriptionCreateRequest;
import com.jaycong.boot.modules.billing.dto.SubscriptionUpdateRequest;
import com.jaycong.boot.modules.billing.dto.SubscriptionView;
import com.jaycong.boot.modules.billing.entity.PlanEntity;
import com.jaycong.boot.modules.billing.entity.SubscriptionEntity;
import com.jaycong.boot.modules.billing.mapper.PlanMapper;
import com.jaycong.boot.modules.billing.mapper.SubscriptionMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Billing 服务，负责套餐查询、订阅创建与状态变更。
 */
@Service
public class BillingService {

    private final PlanMapper planMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;

    public BillingService(PlanMapper planMapper,
                          SubscriptionMapper subscriptionMapper,
                          UserMapper userMapper) {
        this.planMapper = planMapper;
        this.subscriptionMapper = subscriptionMapper;
        this.userMapper = userMapper;
    }

    /**
     * 查询可用套餐列表。
     *
     * @return 套餐视图列表
     */
    public List<PlanView> listPlans() {
        requireCurrentUser();
        List<PlanEntity> plans = planMapper.selectList(new LambdaQueryWrapper<PlanEntity>()
                .eq(PlanEntity::getStatus, "ACTIVE")
                .orderByAsc(PlanEntity::getPrice));
        return plans.stream().map(this::toPlanView).toList();
    }

    /**
     * 为当前租户创建订阅。
     *
     * @param request 创建请求
     * @return 订阅视图
     */
    @Transactional
    public SubscriptionView createSubscription(SubscriptionCreateRequest request) {
        UserEntity user = requireCurrentUser();
        Long tenantId = requireTenantId(user);
        PlanEntity plan = requireActivePlan(request.planId());
        SubscriptionStatus initialStatus = parseInitialStatus(request.initialStatus());

        if (hasEffectiveSubscription(tenantId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前租户已存在有效订阅");
        }

        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setTenantId(tenantId);
        entity.setPlanId(plan.getId());
        entity.setStatus(initialStatus.name());
        entity.setTrialEndAt(request.trialEndAt());
        entity.setCurrentPeriodEnd(request.currentPeriodEnd());
        entity.setEffectiveTime(LocalDateTime.now());
        subscriptionMapper.insert(entity);
        return toSubscriptionView(entity);
    }

    /**
     * 更新指定订阅状态与周期信息。
     *
     * @param subscriptionId 订阅 ID
     * @param request 更新请求
     * @return 更新后的订阅视图
     */
    @Transactional
    public SubscriptionView updateSubscription(Long subscriptionId, SubscriptionUpdateRequest request) {
        UserEntity user = requireCurrentUser();
        Long tenantId = requireTenantId(user);
        SubscriptionEntity entity = requireTenantSubscription(tenantId, subscriptionId);

        SubscriptionStatus from = parseStoredStatus(entity.getStatus());
        SubscriptionStatus to = parseTargetStatus(request.targetStatus());
        if (!canTransition(from, to)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法的订阅状态流转");
        }

        entity.setStatus(to.name());
        entity.setCurrentPeriodEnd(request.currentPeriodEnd());
        if (to == SubscriptionStatus.CANCELED) {
            entity.setCancelTime(request.cancelTime() == null ? LocalDateTime.now() : request.cancelTime());
        }
        subscriptionMapper.updateById(entity);
        return toSubscriptionView(entity);
    }

    /**
     * 判断订阅状态是否允许流转。
     *
     * @param from 当前状态
     * @param to 目标状态
     * @return true 表示允许，false 表示非法
     */
    private boolean canTransition(SubscriptionStatus from, SubscriptionStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case TRIALING -> to == SubscriptionStatus.ACTIVE;
            case ACTIVE -> to == SubscriptionStatus.PAST_DUE || to == SubscriptionStatus.CANCELED;
            case PAST_DUE -> to == SubscriptionStatus.ACTIVE || to == SubscriptionStatus.CANCELED;
            case CANCELED -> false;
        };
    }

    private boolean hasEffectiveSubscription(Long tenantId) {
        Long count = subscriptionMapper.selectCount(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getTenantId, tenantId)
                .in(SubscriptionEntity::getStatus,
                        SubscriptionStatus.TRIALING.name(),
                        SubscriptionStatus.ACTIVE.name(),
                        SubscriptionStatus.PAST_DUE.name()));
        return count != null && count > 0;
    }

    private PlanEntity requireActivePlan(Long planId) {
        PlanEntity plan = planMapper.selectOne(new LambdaQueryWrapper<PlanEntity>()
                .eq(PlanEntity::getId, planId)
                .eq(PlanEntity::getStatus, "ACTIVE")
                .last("limit 1"));
        if (plan == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在或不可用");
        }
        return plan;
    }

    private SubscriptionEntity requireTenantSubscription(Long tenantId, Long subscriptionId) {
        SubscriptionEntity subscription = subscriptionMapper.selectOne(new LambdaQueryWrapper<SubscriptionEntity>()
                .eq(SubscriptionEntity::getId, subscriptionId)
                .eq(SubscriptionEntity::getTenantId, tenantId)
                .last("limit 1"));
        if (subscription == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订阅不存在");
        }
        return subscription;
    }

    private UserEntity requireCurrentUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Long requireTenantId(UserEntity user) {
        if (user.getTenantId() == null || user.getTenantId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前用户未绑定租户");
        }
        return user.getTenantId();
    }

    private SubscriptionStatus parseInitialStatus(String rawStatus) {
        if (!StringUtils.hasText(rawStatus)) {
            return SubscriptionStatus.TRIALING;
        }
        SubscriptionStatus parsed = parseInputStatus(rawStatus, "初始状态不合法");
        if (parsed != SubscriptionStatus.TRIALING && parsed != SubscriptionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "初始状态仅支持 TRIALING 或 ACTIVE");
        }
        return parsed;
    }

    private SubscriptionStatus parseTargetStatus(String rawStatus) {
        return parseInputStatus(rawStatus, "目标状态不合法");
    }

    private SubscriptionStatus parseStoredStatus(String storedStatus) {
        try {
            return SubscriptionStatus.valueOf(storedStatus);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "订阅状态数据异常");
        }
    }

    private SubscriptionStatus parseInputStatus(String rawStatus, String errorMessage) {
        if (!StringUtils.hasText(rawStatus)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, errorMessage);
        }
        try {
            return SubscriptionStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, errorMessage);
        }
    }

    private PlanView toPlanView(PlanEntity plan) {
        return new PlanView(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getBillingCycle(),
                plan.getQuotaJson(),
                plan.getPrice(),
                plan.getStatus()
        );
    }

    private SubscriptionView toSubscriptionView(SubscriptionEntity subscription) {
        return new SubscriptionView(
                subscription.getId(),
                subscription.getTenantId(),
                subscription.getPlanId(),
                subscription.getStatus(),
                subscription.getTrialEndAt(),
                subscription.getCurrentPeriodEnd(),
                subscription.getEffectiveTime(),
                subscription.getCancelTime()
        );
    }
}
