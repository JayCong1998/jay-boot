package com.jaycong.boot.modules.auth.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.jaycong.boot.modules.user.entity.UserEntity;
import com.jaycong.boot.modules.user.mapper.UserMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoginContext {

    private final UserMapper userMapper;

    public LoginContext(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Optional<LoginPrincipal> currentPrincipal() {
        Optional<LoginPrincipal> sessionPrincipal = currentPrincipalFromSession();
        if (sessionPrincipal.isPresent()) {
            return sessionPrincipal;
        }

        if (!StpUtil.isLogin()) {
            return Optional.empty();
        }

        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            return Optional.empty();
        }

        UserEntity user = userMapper.selectById(resolveUserId(loginId));
        if (user == null) {
            return Optional.empty();
        }

        SaSession tokenSession = getTokenSession();
        SaSession legacySession = StpUtil.getSessionByLoginId(loginId);
        SaSession session = tokenSession != null ? tokenSession : legacySession;
        LoginClientType clientType = resolveClientType(session);
        LoginPrincipal fallbackPrincipal = new LoginPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                clientType,
                readString(session, LoginSessionKeys.LOGIN_IP),
                readString(session, LoginSessionKeys.LOGIN_UA),
                resolveLoginTime(session)
        );
        bindPrincipal(session, fallbackPrincipal);
        return Optional.of(fallbackPrincipal);
    }

    public Optional<LoginPrincipal> currentPrincipalFromSession() {
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

    private LoginPrincipal readPrincipal(SaSession session) {
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

    private LoginPrincipal fromMap(Map<?, ?> map, SaSession session) {
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

    private LoginPrincipal resolvePrincipal(LoginPrincipal principal, SaSession session) {
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

    private Optional<LoginPrincipal> validatePrincipal(LoginPrincipal principal, Object loginId, SaSession session) {
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

    private void bindPrincipal(SaSession session, LoginPrincipal principal) {
        if (session == null || principal == null) {
            return;
        }
        session.set(LoginSessionKeys.LOGIN_PRINCIPAL, principal);
        session.set(LoginSessionKeys.LOGIN_IP, principal.loginIp());
        session.set(LoginSessionKeys.LOGIN_UA, principal.loginUa());
        session.set(LoginSessionKeys.LOGIN_TYPE, principal.clientType() == null ? null : principal.clientType().name());
        session.set(LoginSessionKeys.CLIENT_TYPE, principal.clientType() == null ? null : principal.clientType().name());
        session.set(LoginSessionKeys.LOGIN_TIME, principal.loginTime());
    }

    private LoginClientType resolveClientType(SaSession session) {
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

    private LoginClientType resolveClientType(Object value) {
        if (value instanceof LoginClientType clientType) {
            return clientType;
        }
        return LoginClientType.resolve(value == null ? null : value.toString());
    }

    private Long resolveUserId(Object loginId) {
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (Exception ex) {
            return null;
        }
    }

    private Long resolveLong(Object value) {
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

    private LocalDateTime resolveLoginTime(Object value, SaSession session) {
        LocalDateTime loginTime = resolveLocalDateTime(value);
        if (loginTime != null) {
            return loginTime;
        }
        return resolveLoginTime(session);
    }

    private LocalDateTime resolveLocalDateTime(Object value) {
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

    private LocalDateTime resolveLoginTime(SaSession session) {
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

    private SaSession getTokenSession() {
        return StpUtil.getTokenSession();
    }

    private String readString(SaSession session, String key) {
        if (session == null) {
            return null;
        }
        return toString(session.get(key));
    }

    private String firstNonNullString(Object first, String second) {
        String value = toString(first);
        return value != null ? value : second;
    }

    private String toString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isEmpty() ? null : text;
    }
}
