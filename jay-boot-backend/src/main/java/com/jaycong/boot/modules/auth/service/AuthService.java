package com.jaycong.boot.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.auth.dto.AuthRequestContext;
import com.jaycong.boot.modules.auth.dto.AuthSessionResponse;
import com.jaycong.boot.modules.auth.dto.AuthTokenResponse;
import com.jaycong.boot.modules.auth.dto.AuthUserView;
import com.jaycong.boot.modules.auth.dto.ChangePasswordRequest;
import com.jaycong.boot.modules.auth.dto.LoginRequest;
import com.jaycong.boot.modules.auth.dto.RegisterRequest;
import com.jaycong.boot.modules.auth.dto.SiteAuthSessionResponse;
import com.jaycong.boot.modules.auth.dto.SiteAuthUserView;
import com.jaycong.boot.modules.auth.dto.SiteLoginRequest;
import com.jaycong.boot.modules.auth.dto.SiteRegisterRequest;
import com.jaycong.boot.modules.auth.entity.LoginLogEntity;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.LoginLogMapper;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import java.time.ZoneId;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    private static final String USER_ROLE_ADMIN = "admin";
    private static final String USER_ROLE_USER = "user";
    private static final int AUTO_USERNAME_MAX_LENGTH = 24;
    private static final int AUTO_USERNAME_MAX_RETRY = 50;

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper,
                       LoginLogMapper loginLogMapper,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity existing = findByEmail(normalizedEmail);
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
        }

        UserEntity user = new UserEntity();
        user.setUsername(resolveUniqueAutoUsername(extractEmailPrefix(normalizedEmail)));
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(USER_ROLE_ADMIN);
        user.setStatus(USER_STATUS_ACTIVE);

        userMapper.insert(user);

        StpUtil.login(user.getId());
        recordLoginLog(user.getId(), context, true, "REGISTER");
        return buildTokenResponse(user);
    }

    @Transactional
    public SiteAuthSessionResponse registerForSite(SiteRegisterRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedUsername = normalizeUsername(request.username());
        if (findByEmail(normalizedEmail) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
        }
        if (findByUsername(normalizedUsername) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(USER_ROLE_USER);
        user.setStatus(USER_STATUS_ACTIVE);
        userMapper.insert(user);

        StpUtil.login(user.getId());
        recordLoginLog(user.getId(), context, true, "REGISTER");
        return buildSiteSessionResponse(user);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AuthTokenResponse login(LoginRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = findByEmail(normalizedEmail);
        if (user == null) {
            recordLoginLog(null, context, false, "EMAIL_NOT_FOUND");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        if (!USER_STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            recordLoginLog(user.getId(), context, false, "USER_INACTIVE");
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户状态不可用");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordLoginLog(user.getId(), context, false, "PASSWORD_MISMATCH");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        StpUtil.login(user.getId());
        recordLoginLog(user.getId(), context, true, "LOGIN");
        return buildTokenResponse(user);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public SiteAuthSessionResponse loginForSite(SiteLoginRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = findByEmail(normalizedEmail);
        if (user == null) {
            recordLoginLog(null, context, false, "EMAIL_NOT_FOUND");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        if (!USER_STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            recordLoginLog(user.getId(), context, false, "USER_INACTIVE");
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户状态不可用");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordLoginLog(user.getId(), context, false, "PASSWORD_MISMATCH");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        StpUtil.login(user.getId());
        recordLoginLog(user.getId(), context, true, "LOGIN");
        return buildSiteSessionResponse(user);
    }

    public void logout() {
        ensureLogin();
        StpUtil.logout();
    }

    public AuthSessionResponse session() {
        Long loginId = ensureLogin();
        UserEntity user = requireUser(loginId);
        return new AuthSessionResponse(
                loginId,
                StpUtil.getTokenValue(),
                StpUtil.getTokenTimeout(),
                toUserView(user)
        );
    }

    public AuthTokenResponse refreshToken() {
        Long loginId = ensureLogin();
        UserEntity user = requireUser(loginId);
        StpUtil.login(loginId);
        return buildTokenResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long loginId = ensureLogin();
        UserEntity user = requireUser(loginId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "旧密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    public SiteAuthUserView meByToken(String token) {
        Long loginId = resolveLoginIdByToken(token);
        UserEntity user = requireUser(loginId);
        return toSiteUserView(user);
    }

    public void logoutByToken(String token) {
        String normalizedToken = normalizeToken(token);
        if (!StringUtils.hasText(normalizedToken)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "token 不能为空");
        }
        StpUtil.logoutByTokenValue(normalizedToken);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String normalizeToken(String token) {
        return token == null ? null : token.trim();
    }

    private UserEntity findByEmail(String email) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .last("limit 1"));
    }

    private UserEntity findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .last("limit 1"));
    }

    private void recordLoginLog(Long userId, AuthRequestContext context, boolean success, String reason) {
        LoginLogEntity log = new LoginLogEntity();
        log.setUserId(userId);
        log.setIp(context == null ? null : context.ip());
        log.setUa(context == null ? null : context.userAgent());
        log.setSuccess(success);
        log.setReason(reason);
        loginLogMapper.insert(log);
    }

    private Long ensureLogin() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        return StpUtil.getLoginIdAsLong();
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private AuthTokenResponse buildTokenResponse(UserEntity user) {
        return new AuthTokenResponse(
                StpUtil.getTokenValue(),
                StpUtil.getTokenTimeout(),
                toUserView(user)
        );
    }

    private SiteAuthSessionResponse buildSiteSessionResponse(UserEntity user) {
        return new SiteAuthSessionResponse(StpUtil.getTokenValue(), toSiteUserView(user));
    }

    private AuthUserView toUserView(UserEntity user) {
        return new AuthUserView(user.getId(), user.getEmail(), user.getStatus());
    }

    private SiteAuthUserView toSiteUserView(UserEntity user) {
        String username = StringUtils.hasText(user.getUsername())
                ? user.getUsername()
                : extractEmailPrefix(user.getEmail());
        String createdAt = user.getCreatedTime() == null
                ? null
                : user.getCreatedTime().atZone(ZoneId.systemDefault()).toInstant().toString();
        return new SiteAuthUserView(user.getId(), username, user.getEmail(), createdAt);
    }

    private Long resolveLoginIdByToken(String token) {
        String normalizedToken = normalizeToken(token);
        if (!StringUtils.hasText(normalizedToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        Object loginId = StpUtil.getLoginIdByToken(normalizedToken);
        if (loginId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(loginId.toString());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
    }

    private String resolveUniqueAutoUsername(String rawBase) {
        String base = normalizeUsername(rawBase);
        if (!StringUtils.hasText(base)) {
            base = "user";
        }
        base = truncate(base, AUTO_USERNAME_MAX_LENGTH);
        String candidate = base;
        for (int i = 0; i <= AUTO_USERNAME_MAX_RETRY; i++) {
            if (i > 0) {
                String suffix = String.valueOf(i);
                int maxBaseLength = Math.max(1, AUTO_USERNAME_MAX_LENGTH - suffix.length());
                candidate = truncate(base, maxBaseLength) + suffix;
            }
            if (findByUsername(candidate) == null) {
                return candidate;
            }
        }
        throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
    }

    private String extractEmailPrefix(String email) {
        if (!StringUtils.hasText(email)) {
            return "user";
        }
        int index = email.indexOf('@');
        if (index <= 0) {
            return email;
        }
        return email.substring(0, index);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
