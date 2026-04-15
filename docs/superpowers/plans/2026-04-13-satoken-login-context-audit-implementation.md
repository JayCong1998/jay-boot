# Sa-Token 登录上下文与审计联动 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 同时覆盖管理端与用户端，统一登录上下文、优化 Sa-Token 登录日志记录链路，并让 MyBatis-Plus 审计字段自动读取当前登录用户。

**Architecture:** 在认证模块新增 `LoginPrincipal + LoginContext` 作为统一登录态抽象，登录成功后写入 `SaSession` 与扩展参数。登录日志拆分为“认证失败在服务内写入、成功态在 Sa-Token 监听器写入”，并在 `MybatisPlusMetaObjectHandler` 中通过 `LoginContext` 回填 `creator/updater` 字段，未登录回退 `0/system`。

**Tech Stack:** Spring Boot 3.3.x、Sa-Token 1.39.x、MyBatis-Plus 3.5.x、MySQL

---

### Task 1: 建立登录上下文模型与会话键常量

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/context/LoginPrincipal.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/context/LoginSessionKeys.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/context/LoginClientType.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/context/LoginContext.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java`

- [ ] **Step 1: 新增会话键常量**

```java
package com.jaycong.boot.modules.auth.context;

public final class LoginSessionKeys {
    public static final String LOGIN_PRINCIPAL = "LOGIN_PRINCIPAL";
    public static final String LOGIN_IP = "LOGIN_IP";
    public static final String LOGIN_UA = "LOGIN_UA";
    public static final String LOGIN_TYPE = "LOGIN_TYPE";
    public static final String CLIENT_TYPE = "CLIENT_TYPE";

    private LoginSessionKeys() {
    }
}
```

- [ ] **Step 2: 新增端类型枚举**

```java
package com.jaycong.boot.modules.auth.context;

public enum LoginClientType {
    ADMIN,
    SITE
}
```

- [ ] **Step 3: 新增登录实体**

```java
package com.jaycong.boot.modules.auth.context;

import java.io.Serializable;
import java.time.LocalDateTime;

public record LoginPrincipal(
        Long userId,
        String username,
        String email,
        String role,
        LoginClientType clientType,
        String loginIp,
        String loginUa,
        LocalDateTime loginTime
) implements Serializable {
}
```

- [ ] **Step 4: 新增登录上下文组件**

```java
package com.jaycong.boot.modules.auth.context;

import cn.dev33.satoken.stp.StpUtil;
import com.jaycong.boot.modules.user.entity.UserEntity;
import com.jaycong.boot.modules.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LoginContext {
    private final UserMapper userMapper;

    public LoginContext(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Optional<LoginPrincipal> currentPrincipal() {
        if (!StpUtil.isLogin()) {
            return Optional.empty();
        }
        Object raw = StpUtil.getSession().get(LoginSessionKeys.LOGIN_PRINCIPAL);
        if (raw instanceof LoginPrincipal principal) {
            return Optional.of(principal);
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        UserEntity user = userMapper.selectById(loginId);
        if (user == null) {
            return Optional.empty();
        }
        LoginPrincipal principal = new LoginPrincipal(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole(),
                LoginClientType.ADMIN, null, null, LocalDateTime.now()
        );
        StpUtil.getSession().set(LoginSessionKeys.LOGIN_PRINCIPAL, principal);
        return Optional.of(principal);
    }
}
```

- [ ] **Step 5: 在 AuthService 引入上下文模型并统一写入入口**

```java
private void bindLoginSession(UserEntity user, AuthRequestContext context, LoginClientType clientType, String loginType) {
    StpUtil.login(user.getId());
    LoginPrincipal principal = new LoginPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            clientType,
            context == null ? null : context.ip(),
            context == null ? null : context.userAgent(),
            LocalDateTime.now()
    );
    StpUtil.getSession().set(LoginSessionKeys.LOGIN_PRINCIPAL, principal);
    StpUtil.getSession().set(LoginSessionKeys.LOGIN_TYPE, loginType);
    StpUtil.getSession().set(LoginSessionKeys.CLIENT_TYPE, clientType.name());
    StpUtil.getSession().set(LoginSessionKeys.LOGIN_IP, context == null ? null : context.ip());
    StpUtil.getSession().set(LoginSessionKeys.LOGIN_UA, context == null ? null : context.userAgent());
}
```

- [ ] **Step 6: 编译校验**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

- [ ] **Step 7: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/context \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java
git commit -m "feat(auth): 增加统一登录上下文模型与会话绑定"
```

### Task 2: 登录日志事件化（成功态监听器 + 失败态统一写入）

**Files:**
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/LoginLogService.java`
- Create: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/listener/AuthSaTokenListener.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/entity/LoginLogEntity.java`

- [ ] **Step 1: 抽取登录日志服务**

```java
@Service
public class LoginLogService {
    private final LoginLogMapper loginLogMapper;

    public void record(Long userId, String ip, String ua, boolean success, String reason) {
        LoginLogEntity log = new LoginLogEntity();
        log.setUserId(userId);
        log.setIp(ip);
        log.setUa(ua);
        log.setSuccess(success);
        log.setReason(reason);
        loginLogMapper.insert(log);
    }
}
```

- [ ] **Step 2: 失败态改为统一调用日志服务**

```java
private void recordLoginFailure(Long userId, AuthRequestContext context, String reason) {
    loginLogService.record(
            userId,
            context == null ? null : context.ip(),
            context == null ? null : context.userAgent(),
            false,
            reason
    );
}
```

- [ ] **Step 3: 新增 Sa-Token 监听器记录成功态事件**

```java
@Component
public class AuthSaTokenListener implements SaTokenListener {
    private final LoginLogService loginLogService;

    public AuthSaTokenListener(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        SaSession session = StpUtil.getSessionByLoginId(userId, false);
        String ip = session == null ? null : (String) session.get(LoginSessionKeys.LOGIN_IP);
        String ua = session == null ? null : (String) session.get(LoginSessionKeys.LOGIN_UA);
        String reason = session == null ? "LOGIN" : String.valueOf(session.get(LoginSessionKeys.LOGIN_TYPE));
        loginLogService.record(userId, ip, ua, true, reason);
    }

    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        loginLogService.record(userId, null, null, true, "LOGOUT");
    }

    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        loginLogService.record(userId, null, null, true, "KICKOUT");
    }

    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        loginLogService.record(userId, null, null, true, "REPLACED");
    }
}
```

- [ ] **Step 4: AuthService 去除成功态手工写日志**

```java
// 以 login(...) 为例
bindLoginSession(user, context, LoginClientType.ADMIN, "LOGIN");
return buildTokenResponse(user);

// 以 registerForSite(...) 为例
bindLoginSession(user, context, LoginClientType.SITE, "REGISTER");
return buildSiteSessionResponse(user);
```

- [ ] **Step 5: 编译校验**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

- [ ] **Step 6: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/LoginLogService.java \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/listener/AuthSaTokenListener.java \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/service/AuthService.java \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/auth/entity/LoginLogEntity.java
git commit -m "refactor(auth): 基于 satoken 监听器统一成功态登录日志"
```

### Task 3: 审计字段自动填充改造（读取登录上下文）

**Files:**
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/common/config/MybatisPlusMetaObjectHandler.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java`
- Modify: `jay-boot-backend/src/main/java/com/jaycong/boot/modules/user/service/AdminUserService.java`

- [ ] **Step 1: MetaObjectHandler 注入 LoginContext**

```java
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {
    private static final Long SYSTEM_ID = 0L;
    private static final String SYSTEM_NAME = "system";
    private final LoginContext loginContext;
}
```

- [ ] **Step 2: insertFill/updateFill 统一读取当前登录人**

```java
private Long resolveOperatorId() {
    return loginContext.currentPrincipal().map(LoginPrincipal::userId).orElse(SYSTEM_ID);
}

private String resolveOperatorName() {
    return loginContext.currentPrincipal().map(LoginPrincipal::username).orElse(SYSTEM_NAME);
}
```

- [ ] **Step 3: 业务服务将“是否登录/操作者校验”迁移为 LoginContext 优先**

```java
private Long ensureAdminOperator() {
    LoginPrincipal principal = loginContext.currentPrincipal()
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录"));
    UserEntity operator = requireUser(principal.userId());
    if (!isManagementRole(operator.getRole())) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "Only super admin or admin can perform this action");
    }
    return principal.userId();
}
```

- [ ] **Step 4: 编译校验**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

- [ ] **Step 5: 提交**

```bash
git add jay-boot-backend/src/main/java/com/jaycong/boot/common/config/MybatisPlusMetaObjectHandler.java \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/plan/service/AdminPlanService.java \
        jay-boot-backend/src/main/java/com/jaycong/boot/modules/user/service/AdminUserService.java
git commit -m "feat(audit): 审计字段自动读取登录上下文"
```

### Task 4: 链路回归验证与文档同步

**Files:**
- Modify: `docs/superpowers/specs/2026-04-13-satoken-login-context-audit-design.md`

- [ ] **Step 1: 后端编译验证**

Run: `mvn -DskipTests compile`  
Expected: `BUILD SUCCESS`

- [ ] **Step 2: 手工链路验证（不新增测试用例）**

Run:

```bash
curl -X POST http://127.0.0.1:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@example.com\",\"password\":\"Password123\"}"

curl -X POST http://127.0.0.1:8080/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"password\":\"Password123\"}"

curl -X POST http://127.0.0.1:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@example.com\",\"password\":\"bad-pass\"}"

curl -X POST http://127.0.0.1:8080/api/admin/plan/1/status \
  -H "Content-Type: application/json" \
  -H "satoken: <ADMIN_TOKEN>" \
  -d "{\"status\":\"ACTIVE\"}"
```

Expected:

```text
1) login_logs 包含成功态与失败态记录
2) 成功态 reason 为 LOGIN/REGISTER/LOGOUT/KICKOUT/REPLACED 之一
3) 业务表 creator/updater 在登录态下为当前用户ID与username
4) 未登录写入场景仍回退 0/system
```

- [ ] **Step 3: 同步设计文档“实现状态”小节**

```markdown
## 10. 实现状态
- [x] 登录上下文模型
- [x] 成功态日志事件化
- [x] 审计字段自动填充
- [x] 编译与链路验证
```

- [ ] **Step 4: 最终提交**

```bash
git add docs/superpowers/specs/2026-04-13-satoken-login-context-audit-design.md
git commit -m "docs(auth): 更新 satoken 上下文与审计联动实现状态"
```

## 约束说明（遵循 AGENTS.md）

1. 全程仅修改后端认证与审计相关代码，不改部署脚本，不改项目整体结构。
2. 不新增测试用例；验证方式使用 `mvn -DskipTests compile` + 手工链路校验。
3. 所有新增与修改文件使用 UTF-8（无 BOM）。
