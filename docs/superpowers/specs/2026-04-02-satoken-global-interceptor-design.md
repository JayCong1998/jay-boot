# Sa-Token 全局登录拦截器设计（`/api/**`）

## 1. 背景与目标

当前项目鉴权方式分散在两处：

- Controller 级 `@SaCheckPermission`
- Service 内 `StpUtil.isLogin()` 显式判断

这会导致“是否登录”的规则不够集中。  
本次目标是新增 Sa-Token 全局拦截器，实现统一登录兜底：

- 全局拦截范围：`/api/**`
- 白名单：`/api/auth/**`、`/api/system/ping`
- 其余 `/api/**` 必须登录

## 2. 设计方案

采用 `WebMvcConfigurer + SaInterceptor`。

### 2.1 拦截规则

- `addPathPatterns("/api/**")`
- 在 Sa 拦截器中通过 `SaRouter` 放行白名单：
  - `/api/auth/**`
  - `/api/system/ping`
- 对非白名单路径执行 `StpUtil.checkLogin()`

### 2.2 与权限控制协同

- 全局拦截器只负责“是否登录”
- 保留现有 `@SaCheckPermission`，继续负责“是否有权限”
- 请求链路变为：登录校验（401） -> 权限校验（403） -> 业务处理

### 2.3 与现有异常处理协同

`StpUtil.checkLogin()` 未通过时会触发 Sa-Token 未登录异常，继续由现有 `GlobalExceptionHandler` 统一映射为 401 响应，不修改现有异常体系。

## 3. 代码变更清单

- 新增 `src/main/java/com/jaycong/boot/common/config/SaTokenGlobalInterceptorConfig.java`
  - 注册全局 `SaInterceptor`
  - 配置 `/api/**` + 白名单放行

不改动现有 Controller / Service 业务逻辑。

## 4. 验收标准

1. 未登录访问 `POST /api/auth/login`：可正常访问（公开接口）
2. 未登录访问 `GET /api/system/ping`：可正常访问（健康检查）
3. 未登录访问 `GET /api/tenants/current`：返回 401
4. 已登录但无权限访问受 `@SaCheckPermission` 保护接口：返回 403

## 5. 风险与回滚

- 风险：若白名单漏配，会导致公开接口被错误拦截
- 缓解：仅开放最小白名单并通过接口验收验证
- 回滚：删除新增配置类即可恢复到当前分散鉴权模式

