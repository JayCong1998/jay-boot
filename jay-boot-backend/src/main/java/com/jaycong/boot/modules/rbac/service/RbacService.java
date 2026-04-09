package com.jaycong.boot.modules.rbac.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.auth.entity.UserEntity;
import com.jaycong.boot.modules.auth.mapper.UserMapper;
import com.jaycong.boot.modules.rbac.dto.PermissionCheckResponse;
import com.jaycong.boot.modules.rbac.dto.RoleAssignPermissionsRequest;
import com.jaycong.boot.modules.rbac.dto.RoleCreateRequest;
import com.jaycong.boot.modules.rbac.dto.RoleResponse;
import com.jaycong.boot.modules.rbac.dto.UserAssignRolesRequest;
import com.jaycong.boot.modules.rbac.entity.PermissionEntity;
import com.jaycong.boot.modules.rbac.entity.RoleEntity;
import com.jaycong.boot.modules.rbac.entity.RolePermissionEntity;
import com.jaycong.boot.modules.rbac.entity.UserRoleEntity;
import com.jaycong.boot.modules.rbac.mapper.PermissionMapper;
import com.jaycong.boot.modules.rbac.mapper.RoleMapper;
import com.jaycong.boot.modules.rbac.mapper.RolePermissionMapper;
import com.jaycong.boot.modules.rbac.mapper.UserRoleMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC 服务，负责角色管理、授权分配、用户角色分配与权限查询。
 */
@Service
public class RbacService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;

    public RbacService(RoleMapper roleMapper,
                       PermissionMapper permissionMapper,
                       RolePermissionMapper rolePermissionMapper,
                       UserRoleMapper userRoleMapper,
                       UserMapper userMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
    }

    /**
     * 应用启动后自动为历史租户补齐 OWNER 角色的内置权限。
     * 仅补缺，不会覆盖已有的自定义授权关系。
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncOwnerBuiltInPermissionsOnStartup() {
        ensureBuiltInPermissions();
        List<PermissionEntity> builtInPermissions = permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                .in(PermissionEntity::getCode, builtInPermissionCodes()));
        if (builtInPermissions.isEmpty()) {
            return;
        }
        Set<Long> builtInPermissionIds = builtInPermissions.stream()
                .map(PermissionEntity::getId)
                .collect(Collectors.toSet());

        List<RoleEntity> ownerRoles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getName, RbacPermissionCatalog.OWNER_ROLE_NAME));
        for (RoleEntity ownerRole : ownerRoles) {
            fillMissingRolePermissions(ownerRole.getTenantId(), ownerRole.getId(), builtInPermissionIds);
        }
    }

    /**
     * 查询当前租户下的角色列表。
     *
     * @return 角色响应列表
     */
    public List<RoleResponse> listCurrentTenantRoles() {
        Long tenantId = requireCurrentTenantId();
        List<RoleEntity> roles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getTenantId, tenantId)
                .orderByAsc(RoleEntity::getName));
        return roles.stream().map(this::toRoleResponse).toList();
    }

    /**
     * 在当前租户下创建角色。
     *
     * @param request 角色创建请求
     * @return 新建角色响应
     */
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        Long tenantId = requireCurrentTenantId();
        String normalizedName = normalizeName(request.name());

        RoleEntity role = new RoleEntity();
        role.setTenantId(tenantId);
        role.setName(normalizedName);
        try {
            roleMapper.insert(role);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色名称已存在");
        }
        return toRoleResponse(role);
    }

    /**
     * 覆盖式分配角色权限。
     *
     * @param roleId 角色 ID
     * @param request 权限分配请求
     */
    @Transactional
    public void assignPermissions(Long roleId, RoleAssignPermissionsRequest request) {
        Long tenantId = requireCurrentTenantId();
        RoleEntity role = requireTenantRole(tenantId, roleId);
        Set<String> normalizedCodes = request.permissionCodes().stream()
                .map(this::normalizePermissionCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalizedCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "权限编码列表不能为空");
        }

        Map<String, PermissionEntity> permissionByCode = permissionMapper.selectList(
                        new LambdaQueryWrapper<PermissionEntity>().in(PermissionEntity::getCode, normalizedCodes))
                .stream()
                .collect(Collectors.toMap(PermissionEntity::getCode, Function.identity()));

        if (permissionByCode.size() != normalizedCodes.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "存在无效的权限编码");
        }

        rolePermissionMapper.delete(new LambdaUpdateWrapper<RolePermissionEntity>()
                .eq(RolePermissionEntity::getTenantId, tenantId)
                .eq(RolePermissionEntity::getRoleId, role.getId()));

        for (PermissionEntity permission : permissionByCode.values()) {
            RolePermissionEntity relation = new RolePermissionEntity();
            relation.setTenantId(tenantId);
            relation.setRoleId(role.getId());
            relation.setPermissionId(permission.getId());
            rolePermissionMapper.insert(relation);
        }
    }

    /**
     * 覆盖式分配用户角色。
     *
     * @param userId 用户 ID
     * @param request 角色分配请求
     */
    @Transactional
    public void assignRolesToUser(Long userId, UserAssignRolesRequest request) {
        Long tenantId = requireCurrentTenantId();
        UserEntity targetUser = requireTenantUser(tenantId, userId);
        Set<Long> roleIds = request.roleIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色 ID 列表不能为空");
        }

        List<RoleEntity> roles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getTenantId, tenantId)
                .in(RoleEntity::getId, roleIds));
        if (roles.size() != roleIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "存在不存在的角色");
        }

        userRoleMapper.delete(new LambdaUpdateWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getTenantId, tenantId)
                .eq(UserRoleEntity::getUserId, targetUser.getId()));

        for (Long roleId : roleIds) {
            UserRoleEntity relation = new UserRoleEntity();
            relation.setTenantId(tenantId);
            relation.setUserId(targetUser.getId());
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    /**
     * 检查当前登录用户是否拥有指定权限。
     *
     * @param code 权限编码
     * @return 权限检查结果
     */
    public PermissionCheckResponse checkCurrentUserPermission(String code) {
        UserEntity currentUser = requireCurrentUser();
        String normalizedCode = normalizePermissionCode(code);
        boolean granted = listPermissionCodesByUserId(currentUser.getId()).contains(normalizedCode);
        return new PermissionCheckResponse(normalizedCode, granted);
    }

    /**
     * 查询用户角色名列表（提供给 Sa-Token 动态鉴权）。
     *
     * @param userId 用户 ID
     * @return 角色名列表
     */
    public List<String> listRoleNamesByUserId(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getTenantId() == null || user.getTenantId() <= 0) {
            return Collections.emptyList();
        }
        Long tenantId = user.getTenantId();
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
                        .eq(UserRoleEntity::getTenantId, tenantId)
                        .eq(UserRoleEntity::getUserId, user.getId()))
                .stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                        .eq(RoleEntity::getTenantId, tenantId)
                        .in(RoleEntity::getId, roleIds))
                .stream()
                .map(RoleEntity::getName)
                .toList();
    }

    /**
     * 查询用户权限编码列表（提供给 Sa-Token 动态鉴权）。
     *
     * @param userId 用户 ID
     * @return 权限编码列表
     */
    public List<String> listPermissionCodesByUserId(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getTenantId() == null || user.getTenantId() <= 0) {
            return Collections.emptyList();
        }
        Long tenantId = user.getTenantId();
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
                        .eq(UserRoleEntity::getTenantId, tenantId)
                        .eq(UserRoleEntity::getUserId, user.getId()))
                .stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermissionEntity>()
                        .eq(RolePermissionEntity::getTenantId, tenantId)
                        .in(RolePermissionEntity::getRoleId, roleIds))
                .stream()
                .map(RolePermissionEntity::getPermissionId)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                        .in(PermissionEntity::getId, permissionIds))
                .stream()
                .map(PermissionEntity::getCode)
                .toList();
    }

    /**
     * 初始化租户 OWNER 角色及其内置权限，并绑定到拥有者用户。
     *
     * @param tenantId 租户 ID
     * @param ownerUserId 拥有者用户 ID
     */
    @Transactional
    public void bootstrapOwnerRole(Long tenantId, Long ownerUserId) {
        ensureBuiltInPermissions();

        RoleEntity ownerRole = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getTenantId, tenantId)
                .eq(RoleEntity::getName, RbacPermissionCatalog.OWNER_ROLE_NAME)
                .last("limit 1"));
        if (ownerRole == null) {
            ownerRole = new RoleEntity();
            ownerRole.setTenantId(tenantId);
            ownerRole.setName(RbacPermissionCatalog.OWNER_ROLE_NAME);
            roleMapper.insert(ownerRole);
        }

        List<PermissionEntity> builtInPermissions = permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                .in(PermissionEntity::getCode, builtInPermissionCodes()));
        overwriteRolePermissions(tenantId, ownerRole.getId(),
                builtInPermissions.stream().map(PermissionEntity::getId).toList());
        ensureUserRole(tenantId, ownerUserId, ownerRole.getId());
    }

    private void ensureBuiltInPermissions() {
        for (RbacPermissionCatalog.PermissionDefinition definition : RbacPermissionCatalog.builtInPermissions()) {
            PermissionEntity existing = permissionMapper.selectOne(new LambdaQueryWrapper<PermissionEntity>()
                    .eq(PermissionEntity::getCode, definition.code())
                    .last("limit 1"));
            if (existing != null) {
                continue;
            }
            PermissionEntity permission = new PermissionEntity();
            permission.setCode(definition.code());
            permission.setDescription(definition.description());
            try {
                permissionMapper.insert(permission);
            } catch (DuplicateKeyException ignored) {
                // Ignore duplicate inserts in concurrent bootstrap.
            }
        }
    }

    private List<String> builtInPermissionCodes() {
        List<String> codes = new ArrayList<>();
        for (RbacPermissionCatalog.PermissionDefinition definition : RbacPermissionCatalog.builtInPermissions()) {
            codes.add(definition.code());
        }
        return codes;
    }

    private void overwriteRolePermissions(Long tenantId, Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaUpdateWrapper<RolePermissionEntity>()
                .eq(RolePermissionEntity::getTenantId, tenantId)
                .eq(RolePermissionEntity::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            RolePermissionEntity relation = new RolePermissionEntity();
            relation.setTenantId(tenantId);
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
    }

    private void fillMissingRolePermissions(Long tenantId, Long roleId, Set<Long> expectedPermissionIds) {
        Set<Long> existingPermissionIds = new HashSet<>(rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermissionEntity>()
                                .eq(RolePermissionEntity::getTenantId, tenantId)
                                .eq(RolePermissionEntity::getRoleId, roleId))
                .stream()
                .map(RolePermissionEntity::getPermissionId)
                .toList());
        for (Long permissionId : expectedPermissionIds) {
            if (existingPermissionIds.contains(permissionId)) {
                continue;
            }
            RolePermissionEntity relation = new RolePermissionEntity();
            relation.setTenantId(tenantId);
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            try {
                rolePermissionMapper.insert(relation);
            } catch (DuplicateKeyException ignored) {
                // Ignore duplicate inserts in concurrent sync.
            }
        }
    }

    private void ensureUserRole(Long tenantId, Long userId, Long roleId) {
        UserRoleEntity existing = userRoleMapper.selectOne(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getTenantId, tenantId)
                .eq(UserRoleEntity::getUserId, userId)
                .eq(UserRoleEntity::getRoleId, roleId)
                .last("limit 1"));
        if (existing != null) {
            return;
        }
        UserRoleEntity relation = new UserRoleEntity();
        relation.setTenantId(tenantId);
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        userRoleMapper.insert(relation);
    }

    private UserEntity requireCurrentUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Long requireCurrentTenantId() {
        UserEntity user = requireCurrentUser();
        if (user.getTenantId() == null || user.getTenantId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前用户未绑定租户");
        }
        return user.getTenantId();
    }

    private RoleEntity requireTenantRole(Long tenantId, Long roleId) {
        RoleEntity role = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getTenantId, tenantId)
                .eq(RoleEntity::getId, roleId)
                .last("limit 1"));
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private UserEntity requireTenantUser(Long tenantId, Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || !tenantId.equals(user.getTenantId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim();
    }

    private String normalizePermissionCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toLowerCase(Locale.ROOT);
    }

    private RoleResponse toRoleResponse(RoleEntity role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}
