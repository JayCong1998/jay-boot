package com.jaycong.boot.modules.plan.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.common.constant.enums.AdminUserRole;
import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.user.entity.UserEntity;
import com.jaycong.boot.modules.user.mapper.UserMapper;
import com.jaycong.boot.modules.plan.dto.AdminPlanCreateRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanItemView;
import com.jaycong.boot.modules.plan.dto.AdminPlanPageRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanStatusUpdateRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanUpdateRequest;
import com.jaycong.boot.modules.plan.entity.PlanEntity;
import com.jaycong.boot.modules.plan.mapper.PlanMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminPlanService {

    private final PlanMapper planMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final LoginContext loginContext;

    public AdminPlanService(PlanMapper planMapper, UserMapper userMapper, ObjectMapper objectMapper, LoginContext loginContext) {
        this.planMapper = planMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.loginContext = loginContext;
    }

    public PageResult<AdminPlanItemView> page(AdminPlanPageRequest request) {
        ensureAdminOperator();

        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

        LambdaQueryWrapper<PlanEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(PlanEntity::getCode, keyword).or().like(PlanEntity::getName, keyword));
        }
        if (request.status() != null) {
            wrapper.eq(PlanEntity::getStatus, request.status().name());
        }
        if (request.billingCycle() != null) {
            wrapper.eq(PlanEntity::getBillingCycle, request.billingCycle().name());
        }
        wrapper.orderByDesc(PlanEntity::getUpdatedTime);

        Page<PlanEntity> page = planMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminPlanItemView> records = page.getRecords().stream().map(this::toItemView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Transactional
    public void create(AdminPlanCreateRequest request) {
        ensureAdminOperator();
        validateQuotaJson(request.quotaJson());

        PlanEntity entity = new PlanEntity();
        entity.setCode(normalizeCode(request.code()));
        entity.setName(normalizeName(request.name()));
        entity.setBillingCycle(request.billingCycle().name());
        entity.setQuotaJson(request.quotaJson().trim());
        entity.setPrice(request.price());
        entity.setStatus(request.status().name());

        insertPlan(entity);
    }

    @Transactional
    public void update(Long id, AdminPlanUpdateRequest request) {
        ensureAdminOperator();
        validateQuotaJson(request.quotaJson());

        PlanEntity entity = requirePlan(id);
        entity.setName(normalizeName(request.name()));
        entity.setBillingCycle(request.billingCycle().name());
        entity.setQuotaJson(request.quotaJson().trim());
        entity.setPrice(request.price());
        entity.setStatus(request.status().name());

        updatePlan(entity);
    }

    @Transactional
    public void updateStatus(Long id, AdminPlanStatusUpdateRequest request) {
        ensureAdminOperator();
        PlanEntity entity = requirePlan(id);
        entity.setStatus(request.status().name());
        updatePlan(entity);
    }

    private void insertPlan(PlanEntity entity) {
        try {
            planMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "套餐编码已存在");
        }
    }

    private void updatePlan(PlanEntity entity) {
        try {
            planMapper.updateById(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "套餐编码已存在");
        }
    }

    private PlanEntity requirePlan(Long id) {
        PlanEntity entity = planMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }
        return entity;
    }

    private void validateQuotaJson(String quotaJson) {
        if (!StringUtils.hasText(quotaJson)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配额JSON不能为空");
        }
        try {
            objectMapper.readTree(quotaJson);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配额JSON格式不合法");
        }
    }

    private Long ensureAdminOperator() {
        Long loginId = resolveOperatorId();
        UserEntity operator = userMapper.selectById(loginId);
        if (operator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!hasAdminPermission(operator.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可执行该操作");
        }
        return loginId;
    }

    private Long resolveOperatorId() {
        return loginContext.currentPrincipal()
                .map(principal -> principal.userId())
                .orElseGet(() -> {
                    if (!StpUtil.isLogin()) {
                        throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
                    }
                    return StpUtil.getLoginIdAsLong();
                });
    }

    private boolean hasAdminPermission(String roleValue) {
        if (!StringUtils.hasText(roleValue)) {
            return false;
        }
        try {
            AdminUserRole role = AdminUserRole.fromValue(roleValue);
            return role == AdminUserRole.ADMIN || role == AdminUserRole.SUPER_ADMIN;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "套餐编码不能为空");
        }
        return normalized;
    }

    private String normalizeName(String name) {
        String normalized = name == null ? null : name.trim();
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "套餐名称不能为空");
        }
        return normalized;
    }

    private AdminPlanItemView toItemView(PlanEntity entity) {
        String updatedTime = entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toString();
        return new AdminPlanItemView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                parseBillingCycle(entity.getBillingCycle()),
                entity.getQuotaJson(),
                entity.getPrice(),
                parseStatus(entity.getStatus()),
                updatedTime
        );
    }

    private PlanBillingCycle parseBillingCycle(String billingCycle) {
        if (!StringUtils.hasText(billingCycle)) {
            return null;
        }
        return PlanBillingCycle.valueOf(billingCycle.trim().toUpperCase(Locale.ROOT));
    }

    private PlanStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return PlanStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }
}
