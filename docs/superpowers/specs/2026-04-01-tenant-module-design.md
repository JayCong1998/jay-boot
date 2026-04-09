# Tenant Module (Pseudo Multi-Tenant) Design

## 1. Context
- Project: Java Micro-SaaS Starter
- Date: 2026-04-01
- Input docs: `design/Java Micro-SaaS Starter 技术开发文档.md`
- Existing state: Auth module and Tenant module MVP are implemented (tenant provisioning + current tenant APIs).

## 2. Goal and Scope
### 2.1 Goal
Implement MVP Tenant module for pseudo multi-tenancy so that:
- New user registration automatically creates a default workspace (tenant)
- Core data can use `tenant_id` for isolation
- Tenant self-service APIs are available for current tenant query and update

### 2.2 Confirmed Product Decisions
- Single-user single-tenant for v1
- Default workspace naming rule: `workspace-<4 char random suffix>`
- Tenant API scope:
  - `GET /api/tenants/current`
  - `POST /api/tenants/current/update`

### 2.3 Out of Scope (v1)
- User joining multiple tenants
- Tenant switch API
- Admin cross-tenant operation APIs
- Full DB-level tenant interceptor for all modules (can be added later)

## 3. Approaches Compared
### Option A: Inline Tenant logic inside `AuthService.register`
- Pros: fewest files, fastest initial implementation
- Cons: Auth/Tenant coupling, hard to evolve

### Option B (Recommended): `TenantProvisioningService` invoked by Auth
- Pros: clean boundaries, easier to evolve to invitations/RBAC/billing bootstrap
- Cons: slightly more code

### Option C: Async create tenant after registration event
- Pros: best decoupling
- Cons: eventual consistency complexity for MVP

Decision: Option B.

## 4. Architecture and Module Boundaries
### 4.1 New module structure
- `src/main/java/com/jaycong/boot/modules/tenant/controller`
- `src/main/java/com/jaycong/boot/modules/tenant/service`
- `src/main/java/com/jaycong/boot/modules/tenant/entity`
- `src/main/java/com/jaycong/boot/modules/tenant/mapper`
- `src/main/java/com/jaycong/boot/modules/tenant/dto`

### 4.2 Responsibilities
- `TenantProvisioningService`
  - Creates default tenant for a newly registered user
  - Creates user-tenant relation record
  - Returns created tenant id
- `TenantService`
  - Fetches current tenant by current login user
  - Updates current tenant basic profile (name)
- `TenantController`
  - Exposes APIs and request validation

### 4.3 Auth integration point
- Replace current temporary logic in `AuthService.register`:
  - Remove "tenant_id=user_id" fallback
  - Call `TenantProvisioningService.provisionForUser(userId)`
  - Update `users.tenant_id` using returned tenant id

## 5. Data Model
### 5.1 `tenants`
Fields:
- `id` BIGINT PK
- `name` VARCHAR(64) NOT NULL
- `owner_user_id` BIGINT NOT NULL
- `plan_code` VARCHAR(32) NOT NULL DEFAULT 'FREE'
- Common audit fields from `BaseEntity`:
  - `creator_id`, `creator_name`, `created_time`
  - `updater_id`, `updater_name`, `updated_time`
  - `is_deleted`

Indexes:
- `idx_tenants_owner_user_id(owner_user_id)`

### 5.2 `user_tenant`
Fields:
- `id` BIGINT PK
- `tenant_id` BIGINT NOT NULL
- `user_id` BIGINT NOT NULL
- `role_in_tenant` VARCHAR(32) NOT NULL (default `OWNER` for creator)
- Common audit fields from `BaseEntity`

Indexes:
- Unique: `uk_user_tenant_user_id(user_id)` (single-user single-tenant)
- Index: `idx_user_tenant_tenant_id(tenant_id)`

### 5.3 Existing `users` change
- Keep using existing `users.tenant_id`
- Registration flow writes actual tenant id after tenant creation

## 6. API Contract
### 6.1 `GET /api/tenants/current`
- Auth required (Sa-Token login)
- Resolve current user by `StpUtil.getLoginIdAsLong()`
- Use `users.tenant_id` to fetch tenant
- Response: current tenant summary

Response example:
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "tenantId": 123,
    "name": "workspace-a1b2",
    "ownerUserId": 1001,
    "planCode": "FREE"
  }
}
```

### 6.2 `POST /api/tenants/current/update`
- Auth required
- Body:
  - `name` (required, 2-64 chars)
- Update only current tenant's name
- Response: updated tenant summary

## 7. Transaction and Error Handling
### 7.1 Registration transaction
Single transaction in register flow:
1. Insert user
2. Create tenant
3. Update `users.tenant_id`
4. Insert `user_tenant`

Any step failure rolls back all previous writes.

### 7.2 Error mapping
- Not logged in -> `401`
- Current user not found -> `404`
- User has no valid tenant -> `404`
- Tenant name invalid -> `400`
- Tenant create conflict/retry exhausted -> `500`

### 7.3 Workspace name generation
- Rule: `workspace-` + 4 chars lowercase alnum
- Retry up to 3 times on unique conflict
- If all retries fail, return internal error

## 8. Tenant Isolation Strategy (v1)
- Service-level enforced current-tenant reads/updates for Tenant APIs
- Registration ensures `users.tenant_id` is always assigned
- Future evolution: add MyBatis tenant condition injector/interceptor for all tenant-bound tables

## 9. Testing Strategy (TDD)
### 9.1 Integration tests
- Registration creates tenant and user_tenant relation
- User record tenant_id points to created tenant
- `GET /api/tenants/current` returns tenant for logged-in user
- `POST /api/tenants/current/update` updates current tenant name
- Unauthorized access to Tenant APIs returns 401

### 9.2 Negative tests
- Invalid update name returns 400
- Missing tenant for user returns 404
- Provisioning failure rolls back registration writes

## 10. Risks and Mitigation
- Risk: registration partially writes user without tenant
  - Mitigation: single transaction + rollback tests
- Risk: workspace name collision
  - Mitigation: unique key + retry
- Risk: later need multi-tenant membership
  - Mitigation: keep `user_tenant` table now, even in single-user single-tenant mode

## 11. Implementation Notes
- Keep existing API style: `/api/*`, GET for query, POST for write/action
- Reuse existing global exception + api response conventions
- Keep table and entity naming aligned with MyBatis-Plus underscore-to-camel-case mapping
