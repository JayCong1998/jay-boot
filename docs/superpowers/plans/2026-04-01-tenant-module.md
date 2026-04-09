# Tenant Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement pseudo multi-tenant MVP with auto tenant provisioning on registration and current-tenant query/update APIs.

**Architecture:** Keep auth and tenant loosely coupled by introducing a tenant provisioning service called from auth registration transaction. Store tenant ownership in `tenants` and `user_tenant`, and expose tenant self-service APIs based on current login user. Enforce tenant isolation for this module at service layer by resolving tenant from current user.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Sa-Token, MySQL/H2, Spring Boot Test (MockMvc)

## Current Status (2026-04-01)
- Tenant provisioning is implemented and integrated into `AuthService.register`.
- Tenant current-query and update APIs are implemented in `TenantController` + `TenantService`.
- Tenant tables are present in MySQL/H2 schema scripts.
- Integration tests for tenant provisioning and tenant APIs already exist in test sources.
- Local verification with `mvn` is pending in this environment because Maven CLI is unavailable.

---

### Task 1: Add Tenant Failing Integration Tests

**Files:**
- Modify: `src/test/java/com/jaycong/boot/modules/auth/AuthApiTest.java`
- Create: `src/test/java/com/jaycong/boot/modules/tenant/TenantApiTest.java`
- Test: `src/test/java/com/jaycong/boot/modules/auth/AuthApiTest.java`, `src/test/java/com/jaycong/boot/modules/tenant/TenantApiTest.java`

- [ ] **Step 1: Write failing tests for tenant provisioning after register**

```java
// in AuthApiTest
@Test
void register_shouldCreateTenantAndUserTenantRelation() throws Exception {
    register("tenant.owner@example.com", "Password123");
    UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getEmail, "tenant.owner@example.com").last("limit 1"));
    Assertions.assertNotNull(user.getTenantId());
    TenantEntity tenant = tenantMapper.selectById(user.getTenantId());
    Assertions.assertNotNull(tenant);
    UserTenantEntity relation = userTenantMapper.selectOne(new LambdaQueryWrapper<UserTenantEntity>()
            .eq(UserTenantEntity::getUserId, user.getId()).last("limit 1"));
    Assertions.assertEquals("OWNER", relation.getRoleInTenant());
}
```

- [ ] **Step 2: Run tests to verify RED**

Run: `mvn -Dtest=AuthApiTest,TenantApiTest test`
Expected: FAIL with missing tenant entities/services/tables or API not found.

- [ ] **Step 3: Write failing tests for tenant APIs**

```java
// in TenantApiTest
@Test
void getCurrentTenant_shouldReturnTenantForLoggedInUser() throws Exception {
    String token = registerAndLogin("tenant.api@example.com", "Password123");
    mockMvc.perform(get("/api/tenants/current").header("satoken", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value(startsWith("workspace-")));
}

@Test
void updateCurrentTenant_shouldUpdateName() throws Exception {
    String token = registerAndLogin("tenant.update@example.com", "Password123");
    mockMvc.perform(post("/api/tenants/current/update")
            .header("satoken", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Acme Workspace\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Acme Workspace"));
}
```

- [ ] **Step 4: Run tests to verify RED again**

Run: `mvn -Dtest=TenantApiTest test`
Expected: FAIL because `/api/tenants/current` and `/api/tenants/current/update` do not exist yet.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/jaycong/boot/modules/auth/AuthApiTest.java src/test/java/com/jaycong/boot/modules/tenant/TenantApiTest.java
git commit -m "test: add failing tests for tenant provisioning and tenant APIs"
```

### Task 2: Implement Tenant Domain and Provisioning

**Files:**
- Create: `src/main/java/com/jaycong/boot/modules/tenant/entity/TenantEntity.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/entity/UserTenantEntity.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/mapper/TenantMapper.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/mapper/UserTenantMapper.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/service/TenantProvisioningService.java`
- Modify: `src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java`
- Test: `src/test/java/com/jaycong/boot/modules/auth/AuthApiTest.java`

- [ ] **Step 1: Write minimal tenant entities and mappers**

```java
@TableName("tenants")
public class TenantEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Long ownerUserId;
    private String planCode;
}

@TableName("user_tenant")
public class UserTenantEntity extends BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long userId;
    private String roleInTenant;
}
```

- [ ] **Step 2: Implement provisioning service**

```java
@Transactional
public Long provisionForUser(Long userId) {
    TenantEntity tenant = new TenantEntity();
    tenant.setName(generateWorkspaceName());
    tenant.setOwnerUserId(userId);
    tenant.setPlanCode("FREE");
    tenantMapper.insert(tenant);

    UserTenantEntity userTenant = new UserTenantEntity();
    userTenant.setTenantId(tenant.getId());
    userTenant.setUserId(userId);
    userTenant.setRoleInTenant("OWNER");
    userTenantMapper.insert(userTenant);

    return tenant.getId();
}
```

- [ ] **Step 3: Integrate provisioning into auth register**

```java
Long tenantId = tenantProvisioningService.provisionForUser(user.getId());
user.setTenantId(tenantId);
userMapper.updateById(user);
```

- [ ] **Step 4: Run tests to verify GREEN for provisioning tests**

Run: `mvn -Dtest=AuthApiTest#register_shouldCreateTenantAndUserTenantRelation test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jaycong/boot/modules/tenant src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java
git commit -m "feat: add tenant provisioning in registration"
```

### Task 3: Implement Tenant APIs

**Files:**
- Create: `src/main/java/com/jaycong/boot/modules/tenant/dto/TenantCurrentResponse.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/dto/TenantUpdateCurrentRequest.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/service/TenantService.java`
- Create: `src/main/java/com/jaycong/boot/modules/tenant/controller/TenantController.java`
- Modify: `src/main/java/com/jaycong/boot/common/exception/ErrorCode.java` (if needed)
- Test: `src/test/java/com/jaycong/boot/modules/tenant/TenantApiTest.java`

- [ ] **Step 1: Implement service logic for current tenant read/update**

```java
public TenantCurrentResponse getCurrentTenant() {
    Long userId = StpUtil.getLoginIdAsLong();
    UserEntity user = userMapper.selectById(userId);
    TenantEntity tenant = tenantMapper.selectById(user.getTenantId());
    return toResponse(tenant);
}

@Transactional
public TenantCurrentResponse updateCurrentTenant(TenantUpdateCurrentRequest request) {
    Long userId = StpUtil.getLoginIdAsLong();
    UserEntity user = userMapper.selectById(userId);
    TenantEntity tenant = tenantMapper.selectById(user.getTenantId());
    tenant.setName(request.name().trim());
    tenantMapper.updateById(tenant);
    return toResponse(tenant);
}
```

- [ ] **Step 2: Expose controller endpoints**

```java
@GetMapping("/current")
public ApiResponse<TenantCurrentResponse> current() {
    return ApiResponse.success(tenantService.getCurrentTenant());
}

@PostMapping("/current/update")
public ApiResponse<TenantCurrentResponse> updateCurrent(@Valid @RequestBody TenantUpdateCurrentRequest request) {
    return ApiResponse.success(tenantService.updateCurrentTenant(request));
}
```

- [ ] **Step 3: Run tenant API tests to verify GREEN**

Run: `mvn -Dtest=TenantApiTest test`
Expected: PASS

- [ ] **Step 4: Run full related tests**

Run: `mvn -Dtest=AuthApiTest,TenantApiTest,MicroSaasStarterApplicationTests test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jaycong/boot/modules/tenant src/test/java/com/jaycong/boot/modules/tenant/TenantApiTest.java
git commit -m "feat: add tenant current query and update APIs"
```

### Task 4: Add Schema and Test Schema for Tenant Tables

**Files:**
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/test/resources/schema.sql`

- [ ] **Step 1: Add tenants and user_tenant tables in MySQL schema**

```sql
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    plan_code VARCHAR(32) NOT NULL DEFAULT 'FREE',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_tenants_owner_user_id (owner_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_tenant (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_in_tenant VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_tenant_user_id (user_id),
    KEY idx_user_tenant_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Mirror tables in H2 test schema**

```sql
CREATE TABLE IF NOT EXISTS tenants (...);
CREATE TABLE IF NOT EXISTS user_tenant (...);
```

- [ ] **Step 3: Run full tenant/auth tests**

Run: `mvn -Dtest=AuthApiTest,TenantApiTest test`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/schema.sql src/test/resources/schema.sql
git commit -m "chore: add tenant schema for mysql and h2"
```

### Task 5: Final Verification and Docs Alignment

**Files:**
- Modify: `docs/superpowers/specs/2026-04-01-tenant-module-design.md` (if implementation changed details)

- [ ] **Step 1: Run compile and all tests**

Run: `mvn -DskipTests compile && mvn test`
Expected: BUILD SUCCESS and all tests pass

- [ ] **Step 2: Verify acceptance checklist**

```text
- Register auto-creates tenant
- users.tenant_id references created tenant
- user_tenant OWNER relation exists
- GET /api/tenants/current works
- POST /api/tenants/current/update works
- unauthorized tenant API is 401
```

- [ ] **Step 3: Commit final polish**

```bash
git add docs/superpowers/specs/2026-04-01-tenant-module-design.md
git commit -m "docs: align tenant spec with implementation"
```
