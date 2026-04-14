package com.jaycong.boot.modules.auth.listener;

import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.jaycong.boot.modules.auth.context.LoginSessionKeys;
import com.jaycong.boot.modules.auth.service.LoginLogService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthSaTokenListener extends SaTokenListenerForSimple {

    private static final String LOGIN_ACTION_LOGIN = "LOGIN";
    private static final String LOGIN_ACTION_REGISTER = "REGISTER";
    private static final String LOGIN_ACTION_LOGOUT = "LOGOUT";
    private static final String LOGIN_ACTION_KICKOUT = "KICKOUT";
    private static final String LOGIN_ACTION_REPLACED = "REPLACED";

    private final LoginLogService loginLogService;

    public AuthSaTokenListener(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        if (!isCurrentLoginType(loginType)) {
            return;
        }

        String loginAction = getExtraAsString(loginModel, LoginSessionKeys.LOGIN_TYPE);
        if (!isExplicitLoginAction(loginAction)) {
            return;
        }

        Long userId = resolveUserId(loginId);
        if (userId == null) {
            return;
        }

        loginLogService.record(
                userId,
                getExtraAsString(loginModel, LoginSessionKeys.LOGIN_IP),
                getExtraAsString(loginModel, LoginSessionKeys.LOGIN_UA),
                true,
                resolveReason(loginModel, loginAction)
        );
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        if (!isCurrentLoginType(loginType)) {
            return;
        }
        recordAction(loginId, LOGIN_ACTION_LOGOUT);
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        if (!isCurrentLoginType(loginType)) {
            return;
        }
        recordAction(loginId, LOGIN_ACTION_KICKOUT);
    }

    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        if (!isCurrentLoginType(loginType)) {
            return;
        }
        recordAction(loginId, LOGIN_ACTION_REPLACED);
    }

    private void recordAction(Object loginId, String reason) {
        Long userId = resolveUserId(loginId);
        if (userId == null) {
            return;
        }
        loginLogService.record(userId, null, null, true, reason);
    }

    private boolean isExplicitLoginAction(String loginAction) {
        return LOGIN_ACTION_LOGIN.equals(loginAction) || LOGIN_ACTION_REGISTER.equals(loginAction);
    }

    private String resolveReason(SaLoginModel loginModel, String fallbackReason) {
        String reason = getExtraAsString(loginModel, LoginSessionKeys.LOGIN_REASON);
        return StringUtils.hasText(reason) ? reason : fallbackReason;
    }

    private boolean isCurrentLoginType(String loginType) {
        return StpUtil.TYPE.equals(loginType);
    }

    private String getExtraAsString(SaLoginModel loginModel, String key) {
        if (loginModel == null) {
            return null;
        }
        Object value = loginModel.getExtra(key);
        return value == null ? null : value.toString();
    }

    private Long resolveUserId(Object loginId) {
        if (loginId instanceof Long longValue) {
            return longValue;
        }
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        if (loginId == null) {
            return null;
        }
        try {
            return Long.parseLong(loginId.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
