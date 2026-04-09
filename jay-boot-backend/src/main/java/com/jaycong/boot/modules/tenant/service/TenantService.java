package com.jaycong.boot.modules.tenant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import com.jaycong.boot.modules.tenant.dto.TenantCurrentResponse;
import com.jaycong.boot.modules.tenant.dto.TenantUpdateCurrentRequest;
import com.jaycong.boot.modules.tenant.entity.TenantEntity;
import com.jaycong.boot.modules.tenant.mapper.TenantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租户服务，处理当前登录用户所属租户的查询与更新。
 */
@Service
public class TenantService {

    private final UserMapper userMapper;
    private final TenantMapper tenantMapper;

    public TenantService(UserMapper userMapper, TenantMapper tenantMapper) {
        this.userMapper = userMapper;
        this.tenantMapper = tenantMapper;
    }

    /**
     * 获取当前登录用户所属租户信息。
     *
     * @return 当前租户信息
     */
    public TenantCurrentResponse getCurrentTenant() {
        UserEntity user = requireCurrentUser();
        TenantEntity tenant = requireTenant(user.getTenantId());
        return toResponse(tenant);
    }

    /**
     * 更新当前登录用户所属租户名称。
     *
     * @param request 更新请求
     * @return 更新后的租户信息
     */
    @Transactional
    public TenantCurrentResponse updateCurrentTenant(TenantUpdateCurrentRequest request) {
        UserEntity user = requireCurrentUser();
        TenantEntity tenant = requireTenant(user.getTenantId());
        tenant.setName(request.name().trim());
        tenantMapper.updateById(tenant);
        return toResponse(tenant);
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
        if (user.getTenantId() == null || user.getTenantId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前用户未绑定租户");
        }
        return user;
    }

    private TenantEntity requireTenant(Long tenantId) {
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "租户不存在");
        }
        return tenant;
    }

    private TenantCurrentResponse toResponse(TenantEntity tenant) {
        return new TenantCurrentResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getOwnerUserId(),
                tenant.getPlanCode()
        );
    }
}
