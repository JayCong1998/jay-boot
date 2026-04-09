package com.jaycong.boot.modules.tenant.service;

import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.rbac.service.RbacService;
import com.jaycong.boot.modules.tenant.entity.TenantEntity;
import com.jaycong.boot.modules.tenant.entity.UserTenantEntity;
import com.jaycong.boot.modules.tenant.mapper.TenantMapper;
import com.jaycong.boot.modules.tenant.mapper.UserTenantMapper;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租户初始化服务，负责注册后创建默认租户与租户关系。
 */
@Service
public class TenantProvisioningService {

    private static final String DEFAULT_PLAN_CODE = "FREE";
    private static final String OWNER_ROLE = "OWNER";
    private static final int MAX_WORKSPACE_NAME_RETRY = 3;

    private final TenantMapper tenantMapper;
    private final UserTenantMapper userTenantMapper;
    private final RbacService rbacService;

    public TenantProvisioningService(TenantMapper tenantMapper,
                                     UserTenantMapper userTenantMapper,
                                     RbacService rbacService) {
        this.tenantMapper = tenantMapper;
        this.userTenantMapper = userTenantMapper;
        this.rbacService = rbacService;
    }

    /**
     * 为新注册用户创建默认租户，并初始化 OWNER 角色授权。
     *
     * @param userId 用户 ID
     * @return 新创建的租户 ID
     */
    @Transactional
    public Long provisionForUser(Long userId) {
        for (int i = 0; i < MAX_WORKSPACE_NAME_RETRY; i++) {
            TenantEntity tenant = new TenantEntity();
            tenant.setName(generateWorkspaceName());
            tenant.setOwnerUserId(userId);
            tenant.setPlanCode(DEFAULT_PLAN_CODE);

            if (!tryInsertTenant(tenant)) {
                continue;
            }

            UserTenantEntity relation = new UserTenantEntity();
            relation.setTenantId(tenant.getId());
            relation.setUserId(userId);
            relation.setRoleInTenant(OWNER_ROLE);
            userTenantMapper.insert(relation);
            rbacService.bootstrapOwnerRole(tenant.getId(), userId);
            return tenant.getId();
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "租户初始化失败");
    }

    private boolean tryInsertTenant(TenantEntity tenant) {
        try {
            tenantMapper.insert(tenant);
            return true;
        } catch (DuplicateKeyException ex) {
            // 仅在工作区名称冲突时重试。
            return false;
        }
    }

    private String generateWorkspaceName() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            suffix.append(chars.charAt(index));
        }
        return "workspace-" + suffix;
    }
}
