package com.jaycong.boot.modules.auth.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录上下文工具类，提供静态方法获取当前登录用户信息
 */
@Slf4j
public final class LoginContext {

    private LoginContext() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前登录用户的主体信息
     *
     * @return 登录主体信息，未登录时返回 Optional.empty()
     */
    public static Optional<LoginPrincipal> currentPrincipal() {
        return currentPrincipalFromSession();
    }

    /**
     * 获取当前登录用户的主体信息，如果未登录则抛出异常
     *
     * @return 登录主体信息
     * @throws BusinessException 如果用户未登录
     */
    public static LoginPrincipal requirePrincipal() {
        return currentPrincipal()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录"));
    }

    /**
     * 获取当前登录用户的 ID，如果未登录则抛出异常
     *
     * @return 用户 ID
     * @throws BusinessException 如果用户未登录
     */
    public static Long requireUserId() {
        return requirePrincipal().userId();
    }

    /**
     * 获取当前登录用户的用户名，如果未登录则抛出异常
     *
     * @return 用户名
     * @throws BusinessException 如果用户未登录
     */
    public static String requireUsername() {
        return requirePrincipal().username();
    }

    /**
     * 获取当前登录用户的角色，如果未登录则抛出异常
     *
     * @return 用户角色
     * @throws BusinessException 如果用户未登录
     */
    public static String requireRole() {
        return requirePrincipal().role();
    }

    /**
     * 从 Session 中获取当前登录用户的主体信息
     *
     * @return 登录主体信息，未登录或 Session 中无信息时返回 Optional.empty()
     */
    public static Optional<LoginPrincipal> currentPrincipalFromSession() {
        if (!StpUtil.isLogin()) {
            return Optional.empty();
        }

        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return Optional.empty();
        }

        SaSession tokenSession = getTokenSession();
        LoginPrincipal principal = readPrincipal(tokenSession);
        if (principal != null) {
            return validatePrincipal(principal, loginId, tokenSession);
        }

        SaSession legacySession = StpUtil.getSessionByLoginId(loginId);
        principal = readPrincipal(legacySession);
        if (principal != null) {
            return validatePrincipal(principal, loginId, legacySession);
        }
        return Optional.empty();
    }

    private static LoginPrincipal readPrincipal(SaSession session) {
        if (session == null) {
            return null;
        }
        try {
            Object principalValue = session.get(LoginSessionKeys.LOGIN_PRINCIPAL);
            if (principalValue instanceof LoginPrincipal principal) {
                return resolvePrincipal(principal, session);
            }
            if (principalValue instanceof Map<?, ?> map) {
                return fromMap(map, session);
            }
            return null;
        } catch (Exception ex) {
            log.warn("解析登录上下文 principal 失败，回退为空", ex);
            return null;
        }
    }

    private static LoginPrincipal fromMap(Map<?, ?> map, SaSession session) {
        try {
            Object userIdValue = map.get("userId");
            Long userId = resolveLong(userIdValue);
            if (userId == null) {
                return null;
            }
            return new LoginPrincipal(
                    userId,
                    toString(map.get("username")),
                    toString(map.get("email")),
                    toString(map.get("role")),
                    resolveClientType(session),
                    firstNonNullString(map.get("loginIp"), readString(session, LoginSessionKeys.LOGIN_IP)),
                    firstNonNullString(map.get("loginUa"), readString(session, LoginSessionKeys.LOGIN_UA)),
                    resolveLoginTime(map.get("loginTime"), session)
            );
        } catch (Exception ex) {
            log.warn("解析登录上下文 map 失败，回退为空", ex);
            return null;
        }
    }

    private static LoginPrincipal resolvePrincipal(LoginPrincipal principal, SaSession session) {
        if (principal == null) {
            return null;
        }
        LoginClientType clientType = resolveClientType(session);
        if (principal.clientType() == clientType) {
            return principal;
        }
        return new LoginPrincipal(
                principal.userId(),
                principal.username(),
                principal.email(),
                principal.role(),
                clientType,
                principal.loginIp(),
                principal.loginUa(),
                principal.loginTime() != null ? principal.loginTime() : resolveLoginTime(session)
        );
    }

    private static Optional<LoginPrincipal> validatePrincipal(LoginPrincipal principal, Object loginId, SaSession session) {
        LoginPrincipal resolvedPrincipal = resolvePrincipal(principal, session);
        Long currentLoginId = resolveLong(loginId);
        if (currentLoginId == null || resolvedPrincipal == null) {
            return Optional.empty();
        }
        if (!Objects.equals(resolvedPrincipal.userId(), currentLoginId)) {
            log.warn("登录上下文 principal.userId 与 loginId 不一致，忽略脏 principal: principalUserId={}, loginId={}",
                    resolvedPrincipal.userId(), currentLoginId);
            return Optional.empty();
        }
        return Optional.of(resolvedPrincipal);
    }



    private static LoginClientType resolveClientType(SaSession session) {
        if (session == null) {
            return LoginClientType.ADMIN;
        }
        try {
            Object clientTypeValue = session.get(LoginSessionKeys.CLIENT_TYPE);
            LoginClientType clientType = resolveClientType(clientTypeValue);
            if (clientType != LoginClientType.ADMIN || clientTypeValue != null) {
                return clientType;
            }
            return LoginClientType.ADMIN;
        } catch (Exception ex) {
            log.warn("解析登录上下文 clientType 失败，默认 ADMIN", ex);
            return LoginClientType.ADMIN;
        }
    }

    private static LoginClientType resolveClientType(Object value) {
        if (value instanceof LoginClientType clientType) {
            return clientType;
        }
        return LoginClientType.resolve(value == null ? null : value.toString());
    }

    private static Long resolveLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private static LocalDateTime resolveLoginTime(Object value, SaSession session) {
        LocalDateTime loginTime = resolveLocalDateTime(value);
        if (loginTime != null) {
            return loginTime;
        }
        return resolveLoginTime(session);
    }

    private static LocalDateTime resolveLocalDateTime(Object value) {
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        try {
            if (value instanceof Number number) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), ZoneId.systemDefault());
            }
            if (value instanceof String text && !text.isBlank()) {
                return LocalDateTime.parse(text);
            }
        } catch (Exception ex) {
            log.warn("解析登录上下文时间失败，回退为空", ex);
        }
        return null;
    }

    private static LocalDateTime resolveLoginTime(SaSession session) {
        if (session == null) {
            return null;
        }
        try {
            Object loginTimeValue = session.get(LoginSessionKeys.LOGIN_TIME);
            LocalDateTime loginTime = resolveLocalDateTime(loginTimeValue);
            if (loginTime != null) {
                return loginTime;
            }
            long createTime = session.getCreateTime();
            if (createTime > 0) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneId.systemDefault());
            }
            return null;
        } catch (Exception ex) {
            log.warn("解析登录上下文时间失败，回退为空", ex);
            return null;
        }
    }

    private static SaSession getTokenSession() {
        return StpUtil.getTokenSession();
    }

    private static String readString(SaSession session, String key) {
        if (session == null) {
            return null;
        }
        return toString(session.get(key));
    }

    private static String firstNonNullString(Object first, String second) {
        String value = toString(first);
        return value != null ? value : second;
    }

    private static String toString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isEmpty() ? null : text;
    }
}
