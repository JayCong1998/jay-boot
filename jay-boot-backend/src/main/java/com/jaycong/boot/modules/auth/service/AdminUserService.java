package com.jaycong.boot.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.auth.dto.AdminUserCreateRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserItemView;
import com.jaycong.boot.modules.auth.dto.AdminUserPageRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserPageResponse;
import com.jaycong.boot.modules.auth.dto.AdminUserPasswordResetRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserRole;
import com.jaycong.boot.modules.auth.dto.AdminUserStatus;
import com.jaycong.boot.modules.auth.dto.AdminUserStatusUpdateRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserUpdateRequest;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService {

    private static final String ROLE_ADMIN = "admin";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminUserPageResponse page(AdminUserPageRequest request) {
        ensureAdminOperator();
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(UserEntity::getUsername, keyword).or().like(UserEntity::getEmail, keyword));
        }
        if (request.role() != null) {
            wrapper.eq(UserEntity::getRole, request.role().value());
        }
        if (request.status() != null) {
            wrapper.eq(UserEntity::getStatus, request.status().name());
        }
        wrapper.orderByDesc(UserEntity::getCreatedTime);

        Page<UserEntity> page = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminUserItemView> records = page.getRecords().stream().map(this::toItemView).toList();
        return new AdminUserPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Transactional
    public void create(AdminUserCreateRequest request) {
        ensureAdminOperator();

        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        ensureUsernameUnique(username, null);
        ensureEmailUnique(email, null);

        UserEntity entity = new UserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setRole(request.role().value());
        entity.setStatus(request.status() == null ? AdminUserStatus.ACTIVE.name() : request.status().name());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        userMapper.insert(entity);
    }

    @Transactional
    public void update(Long id, AdminUserUpdateRequest request) {
        ensureAdminOperator();

        UserEntity entity = requireUser(id);
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        ensureUsernameUnique(username, id);
        ensureEmailUnique(email, id);

        String nextRole = request.role().value();
        String nextStatus = request.status().name();
        ensureActiveAdminNotExhausted(entity, nextRole, nextStatus);

        entity.setUsername(username);
        entity.setEmail(email);
        entity.setRole(nextRole);
        entity.setStatus(nextStatus);
        userMapper.updateById(entity);
    }

    @Transactional
    public void updateStatus(Long id, AdminUserStatusUpdateRequest request) {
        Long operatorId = ensureAdminOperator();
        UserEntity entity = requireUser(id);
        String nextStatus = request.status().name();

        if (operatorId.equals(id) && AdminUserStatus.INACTIVE.name().equals(nextStatus)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不允许禁用当前登录账号");
        }

        ensureActiveAdminNotExhausted(entity, entity.getRole(), nextStatus);

        entity.setStatus(nextStatus);
        userMapper.updateById(entity);
    }

    @Transactional
    public void resetPassword(Long id, AdminUserPasswordResetRequest request) {
        ensureAdminOperator();
        UserEntity entity = requireUser(id);
        entity.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(entity);
    }

    private Long ensureAdminOperator() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        UserEntity operator = requireUser(loginId);
        if (!ROLE_ADMIN.equalsIgnoreCase(operator.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅管理员可执行该操作");
        }
        return loginId;
    }

    private UserEntity requireUser(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void ensureUsernameUnique(String username, Long excludeId) {
        UserEntity exists = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
    }

    private void ensureEmailUnique(String email, Long excludeId) {
        UserEntity exists = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
        }
    }

    private void ensureActiveAdminNotExhausted(UserEntity current, String nextRole, String nextStatus) {
        boolean currentActiveAdmin = isActiveAdmin(current.getRole(), current.getStatus());
        boolean nextActiveAdmin = isActiveAdmin(nextRole, nextStatus);
        if (!currentActiveAdmin || nextActiveAdmin) {
            return;
        }

        long activeAdminCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, ROLE_ADMIN)
                .eq(UserEntity::getStatus, STATUS_ACTIVE));
        if (activeAdminCount <= 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "至少保留一个启用中的管理员");
        }
    }

    private boolean isActiveAdmin(String role, String status) {
        return ROLE_ADMIN.equalsIgnoreCase(role) && STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private AdminUserRole parseRole(String role) {
        if (!StringUtils.hasText(role)) {
            return AdminUserRole.USER;
        }
        return AdminUserRole.fromValue(role);
    }

    private AdminUserStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return AdminUserStatus.INACTIVE;
        }
        return AdminUserStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }

    private AdminUserItemView toItemView(UserEntity entity) {
        String createdTime = entity.getCreatedTime() == null ? null : entity.getCreatedTime().toString();
        return new AdminUserItemView(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                parseRole(entity.getRole()),
                parseStatus(entity.getStatus()),
                createdTime
        );
    }
}

