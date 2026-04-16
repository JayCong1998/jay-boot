package com.jaycong.boot.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.user.dto.AdminUserCreateRequest;
import com.jaycong.boot.modules.user.dto.AdminUserItemView;
import com.jaycong.boot.modules.user.dto.AdminUserPageRequest;
import com.jaycong.boot.modules.user.dto.AdminUserPasswordResetRequest;
import com.jaycong.boot.common.constant.enums.AdminUserRole;
import com.jaycong.boot.common.constant.enums.AdminUserStatus;
import com.jaycong.boot.modules.user.dto.AdminUserStatusUpdateRequest;
import com.jaycong.boot.modules.user.dto.AdminUserUpdateRequest;
import com.jaycong.boot.modules.user.entity.UserEntity;
import com.jaycong.boot.modules.user.mapper.UserMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService {

    private static final String ROLE_SUPER_ADMIN = "super_admin";
    private static final String ROLE_ADMIN = "admin";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<AdminUserItemView> page(AdminUserPageRequest request) {
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
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Transactional
    @OperationLog(module = "用户管理", action = "创建", detail = "创建用户：#{#request.email}")
    public void create(AdminUserCreateRequest request) {
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
    @OperationLog(module = "用户管理", action = "修改", detail = "修改用户ID：#{#id}")
    public void update(Long id, AdminUserUpdateRequest request) {
        UserEntity entity = requireUser(id);
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        ensureUsernameUnique(username, id);
        ensureEmailUnique(email, id);

        String nextRole = request.role().value();
        String nextStatus = request.status().name();
        ensureActiveManagementNotExhausted(entity, nextRole, nextStatus);

        entity.setUsername(username);
        entity.setEmail(email);
        entity.setRole(nextRole);
        entity.setStatus(nextStatus);
        userMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "用户管理", action = "修改状态", detail = "用户#{#id}状态改为#{#request.status.name()}")
    public void updateStatus(Long id, AdminUserStatusUpdateRequest request) {
        Long operatorId = LoginContext.requireUserId();
        UserEntity entity = requireUser(id);
        String nextStatus = request.status().name();

        if (operatorId.equals(id) && AdminUserStatus.INACTIVE.name().equals(nextStatus)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Current account cannot be disabled");
        }

        ensureActiveManagementNotExhausted(entity, entity.getRole(), nextStatus);

        entity.setStatus(nextStatus);
        userMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "用户管理", action = "重置密码", detail = "重置用户ID：#{#id}的密码")
    public void resetPassword(Long id, AdminUserPasswordResetRequest request) {
        UserEntity entity = requireUser(id);
        entity.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(entity);
    }

    private UserEntity requireUser(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        return user;
    }

    private void ensureUsernameUnique(String username, Long excludeId) {
        UserEntity exists = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
    }

    private void ensureEmailUnique(String email, Long excludeId) {
        UserEntity exists = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .last("limit 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already exists");
        }
    }

    private void ensureActiveManagementNotExhausted(UserEntity current, String nextRole, String nextStatus) {
        boolean currentActiveManagement = isActiveManagementRole(current.getRole(), current.getStatus());
        boolean nextActiveManagement = isActiveManagementRole(nextRole, nextStatus);
        if (!currentActiveManagement || nextActiveManagement) {
            return;
        }

        long activeManagementCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .in(UserEntity::getRole, ROLE_SUPER_ADMIN, ROLE_ADMIN)
                .eq(UserEntity::getStatus, STATUS_ACTIVE));
        if (activeManagementCount <= 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "At least one active manager must remain");
        }
    }

    private boolean isActiveManagementRole(String role, String status) {
        return isManagementRole(role) && STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    private boolean isManagementRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        String normalizedRole = role.trim().toLowerCase(Locale.ROOT);
        return ROLE_SUPER_ADMIN.equals(normalizedRole) || ROLE_ADMIN.equals(normalizedRole);
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

