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
import com.jaycong.boot.modules.auth.entity.LoginLogEntity;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.LoginLogMapper;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import com.jaycong.boot.modules.tenant.service.TenantProvisioningService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务，负责注册、登录、会话与密码修改等核心认证流程。
 */
@Service
public class AuthService {

    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final TenantProvisioningService tenantProvisioningService;

    public AuthService(UserMapper userMapper,
                       LoginLogMapper loginLogMapper,
                       PasswordEncoder passwordEncoder,
                       TenantProvisioningService tenantProvisioningService) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.passwordEncoder = passwordEncoder;
        this.tenantProvisioningService = tenantProvisioningService;
    }

    /**
     * 注册用户并初始化默认租户。
     *
     * @param request 注册请求
     * @param context 请求上下文
     * @return 认证令牌响应
     */
    @Transactional
    public AuthTokenResponse register(RegisterRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity existing = findByEmail(normalizedEmail);
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
        }

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(USER_STATUS_ACTIVE);

        userMapper.insert(user);
        Long tenantId = tenantProvisioningService.provisionForUser(user.getId());
        user.setTenantId(tenantId);
        userMapper.updateById(user);

        StpUtil.login(user.getId());
        recordLoginLog(user.getTenantId(), user.getId(), context, true, "REGISTER");
        return buildTokenResponse(user);
    }

    /**
     * 用户登录并写入登录日志。
     *
     * @param request 登录请求
     * @param context 请求上下文
     * @return 认证令牌响应
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthTokenResponse login(LoginRequest request, AuthRequestContext context) {
        String normalizedEmail = normalizeEmail(request.email());
        UserEntity user = findByEmail(normalizedEmail);
        if (user == null) {
            recordLoginLog(0L, null, context, false, "EMAIL_NOT_FOUND");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        if (!USER_STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            recordLoginLog(user.getTenantId(), user.getId(), context, false, "USER_INACTIVE");
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户状态不可用");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordLoginLog(user.getTenantId(), user.getId(), context, false, "PASSWORD_MISMATCH");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "邮箱或密码错误");
        }

        StpUtil.login(user.getId());
        recordLoginLog(user.getTenantId(), user.getId(), context, true, "LOGIN");
        return buildTokenResponse(user);
    }

    /**
     * 当前用户退出登录。
     */
    public void logout() {
        ensureLogin();
        StpUtil.logout();
    }

    /**
     * 获取当前登录会话信息。
     *
     * @return 会话响应
     */
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

    /**
     * 刷新当前登录用户的令牌。
     *
     * @return 认证令牌响应
     */
    public AuthTokenResponse refreshToken() {
        Long loginId = ensureLogin();
        UserEntity user = requireUser(loginId);
        StpUtil.login(loginId);
        return buildTokenResponse(user);
    }

    /**
     * 修改当前用户密码。
     *
     * @param request 修改密码请求
     */
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private UserEntity findByEmail(String email) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .last("limit 1"));
    }

    private void recordLoginLog(Long tenantId, Long userId, AuthRequestContext context, boolean success, String reason) {
        LoginLogEntity log = new LoginLogEntity();
        log.setTenantId(tenantId == null ? 0L : tenantId);
        log.setUserId(userId);
        log.setIp(context.ip());
        log.setUa(context.userAgent());
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

    private AuthUserView toUserView(UserEntity user) {
        return new AuthUserView(user.getId(), user.getTenantId(), user.getEmail(), user.getStatus());
    }
}
