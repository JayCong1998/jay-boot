package com.jaycong.boot.modules.rbac.config;

import cn.dev33.satoken.stp.StpInterface;
import com.jaycong.boot.modules.rbac.service.RbacService;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 权限提供器，按当前用户动态返回角色与权限。
 */
@Component
public class RbacStpInterfaceImpl implements StpInterface {

    private final RbacService rbacService;

    public RbacStpInterfaceImpl(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = resolveUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return rbacService.listPermissionCodesByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = resolveUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return rbacService.listRoleNamesByUserId(userId);
    }

    private Long resolveUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
