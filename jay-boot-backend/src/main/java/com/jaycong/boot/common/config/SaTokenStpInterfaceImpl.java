package com.jaycong.boot.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 权限验证接口实现。
 * 为 Sa-Token 框架提供当前登录用户的角色和权限信息。
 */
@Component
public class SaTokenStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 当前项目基于角色进行权限控制，暂不实现细粒度权限
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return LoginContext.currentPrincipal()
                .map(LoginPrincipal::role)
                .map(role -> List.of(role))
                .orElse(Collections.emptyList());
    }
}
