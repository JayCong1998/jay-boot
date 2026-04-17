## 一、后端开发规范

- 严格遵循 **SOLID、DRY、KISS、YAGNI** 原则
- 遵循 **OWASP 安全最佳实践**（如输入验证、SQL 注入防护、密码加密存储）
- 采用 **分层架构设计**，确保职责分离

***

## 二、技术栈规范

### 2.1 核心技术栈

- **框架**：Spring Boot 3.3.x + Java 17
- **ORM 框架**：MyBatis-Plus 3.5.x
- **认证授权**：Sa-Token 1.39.x
- **数据库**：MySQL 8.0+
- **缓存**：Redis
- **消息队列**：RabbitMQ
- **接口文档**：Swagger
- **工具包**：HuTool



***

## 三、应用逻辑设计规范

### 3.1 分层架构原则

| 层级             | 职责                      | 约束条件                                                                    |
| -------------- | ----------------------- | ----------------------------------------------------------------------- |
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | - 禁止直接操作数据库- 必须通过 Service 层调用- 统一返回 `ApiResponse<T>`                    |
| **Service**    | 业务逻辑实现，事务管理，数据校验        | - 必须通过 Mapper 访问数据库- 返回 DTO 而非 Entity（除非必要）- 使用 `@Transactional` 管理事务   |
| **Mapper**     | 数据持久化接口，定义数据库查询逻辑       | - 必须继承 `BaseMapper<T>`- 复杂查询使用 `LambdaQueryWrapper`- 禁止在 Mapper 中编写业务逻辑 |
| **Entity**     | 数据库表结构映射对象              | - 仅用于数据库交互- 禁止直接返回给前端（需通过 DTO 转换）- 必须包含审计字段（creator、updater 等）          |

### 3.2 模块划分原则

- **按业务领域垂直拆分**：每个模块独立包路径（如 `modules.auth`, `modules.billing`）
- **公共组件横向抽取**：通用功能放入 `common` 包（如 `common.web`, `common.exception`）
- **基础设施独立分层**：第三方服务集成放入 `infrastructure` 包（如 AI、支付、MQ）

***

## 四、核心代码规范

### 4.1 实体类（Entity）规范

```
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;  // 多租户隔离字段

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式无效")
    private String email;

    private String passwordHash;

    private String status;  // ACTIVE/INACTIVE
}
```

**关键要求：**

- 必须继承 `BaseEntity`（包含 creator、updater、createdTime、updatedTime、isDeleted）
- 使用 `@TableId(type = IdType.ASSIGN_ID)` 生成雪花算法 ID
- 逻辑删除使用 `@TableLogic` 注解（在 BaseEntity 中已定义）

### 4.2 数据访问层（Mapper）规范

```
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // 优先使用 MyBatis-Plus 提供的 CRUD 方法
    // 复杂查询使用 LambdaQueryWrapper
}

// Service 中使用示例
public UserEntity findByEmail(String email) {
    return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
            .eq(UserEntity::getEmail, email)
            .last("limit 1"));
}
```

**关键要求：**

- 禁止手写 SQL，优先使用 MyBatis-Plus 的 `LambdaQueryWrapper`
- 关联查询使用 MP-Join 或手动 JOIN，避免 N+1 查询
- 批量操作使用 `insertBatch` 或 `updateBatchById`

### 4.3 服务层（Service）规范

```
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 推荐使用构造器注入
    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request, AuthRequestContext context) {
        // 1. 数据校验
        String normalizedEmail = normalizeEmail(request.email());
        
        // 2. 业务逻辑
        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus("ACTIVE");
        
        // 3. 持久化
        userMapper.insert(user);
        
        // 4. 返回 DTO
        return buildTokenResponse(user);
    }
}
```

**关键要求：**

- 使用**构造器注入**替代 `@Autowired`（推荐，避免循环依赖）
- 事务注解 `@Transactional` 只标注在 Service 层
- 返回 DTO 对象，禁止直接返回 Entity
- 异常抛出使用 `BusinessException`

### 4.4 控制器（Controller）规范

```
@RestController
@RequestMapping("/api/auth")
@Validated
@Api(tags = "认证管理")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    @ApiResponses({
        @ApiResponse(code = 0, message = "成功"),
        @ApiResponse(code = 401, message = "未授权"),
        @ApiResponse(code = 400, message = "参数错误")
    })
    public ApiResponse<AuthTokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, buildContext(httpRequest)));
    }
}
```

**关键要求：**

- 使用 `@Validated` 开启参数校验
- 请求体参数使用 `@Valid @RequestBody`
- 统一返回 `ApiResponse.success(data)`
- 禁止在 Controller 中捕获异常（交给全局异常处理器）
- 使用 Swagger 注解生成 API 文档
  - `@Api(tags = "模块名称")`：标注控制器模块
  - `@ApiOperation("接口描述")`：标注接口功能
  - `@ApiResponses`：标注响应状态码
  - `@ApiParam`：标注请求参数（可选）

***

## 五、数据传输对象（DTO）规范

### 5.1 使用 record 定义 DTO（Java 17 特性）

```
// 请求 DTO
public record RegisterRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式无效")
        String email,
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 64, message = "密码长度必须在8-64之间")
        String password
) {}

// 响应 DTO
public record AuthTokenResponse(
        String token,
        long tokenTimeout,
        AuthUserView user
) {}

// 视图 DTO
public record AuthUserView(
        Long id,
        Long tenantId,
        String email,
        String status
) {}
```

### 5.2 DTO 分类使用

- **Request DTO**：接收前端参数，带校验注解
- **Response DTO**：返回前端数据，使用 record 或普通类
- **View DTO**：嵌套在 Response 中的子对象

**关键要求：**

- 优先使用 `record`（不可变、线程安全、简洁）
- 校验注解放在 Request DTO 上，校验信息使用中文
- DTO 之间可以嵌套组合

***

## 六、全局异常处理规范

### 6.1 统一响应格式（ApiResponse）

```
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

**响应码规范：**

- `200`：SUCCESS(成功)
- `400`：BAD_REQUEST（参数错误）
- `401`：UNAUTHORIZED（未登录/ token 失效）
- `403`：FORBIDDEN（无权限）
- `404`：NOT_FOUND（资源不存在）
- `409`：CONFLICT（资源冲突）
- `500`：INTERNAL_ERROR（服务器内部错误）

### 6.2 业务异常（BusinessException）

```
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

// 使用示例
throw new BusinessException(ErrorCode.UNAUTHORIZED, "Email or password is invalid");
```

### 6.3 全局异常处理器（GlobalExceptionHandler）

```
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        // 记录日志
        log.error("Unknown error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), "Internal server error"));
    }
}
```

**关键要求：**

- Controller 中禁止 try-catch（交给全局异常处理器）
- 业务异常统一使用 `BusinessException`
- 未知异常记录 ERROR 级别日志

***

## 七、安全与性能规范

### 7.1 输入校验

- 使用 `@Valid` + JSR-303 校验注解（`@NotBlank`, `@Size`, `@Email` 等）
- 禁止直接拼接 SQL（使用 `LambdaQueryWrapper` 防止注入）
- 文件上传需校验类型和大小

### 7.2 事务管理

- `@Transactional` 仅标注在 Service 方法上
- 避免在循环中频繁提交事务
- 大事务拆分为小事务（避免长事务锁表）

***

## 八、代码风格规范

### 8.1 命名规范

- **类名**：`UpperCamelCase`（如 `UserServiceImpl`, `AuthController`）
- **方法/变量名**：`lowerCamelCase`（如 `saveUser`, `userService`）
- **常量**：`UPPER_SNAKE_CASE`（如 `USER_STATUS_ACTIVE`）
- **包名**：全小写，单数形式（如 `com.jaycong.boot.modules.auth.service`）

### 8.2 注释规范

- **类注释**：说明类的职责和用途
- **方法注释**：使用 Javadoc 格式，说明参数、返回值、异常
- **TODO**：计划待完成的任务
- **FIXME**：存在缺陷需要修复的逻辑

```
/**
 * 用户服务实现类。
 * 处理用户注册、登录和个人信息管理。
 */
@Service
public class AuthService {

    /**
     * 注册新用户。
     *
     * @param request 注册请求参数
     * @param context 请求上下文（IP 地址、User-Agent）
     * @return 认证令牌响应
     * @throws BusinessException 当邮箱已被注册时抛出
     */
    @Transactional
    public AuthTokenResponse register(RegisterRequest request, AuthRequestContext context) {
        // TODO: 添加邮箱验证
        ...
    }
}
```

### 8.3 代码格式化

- 使用 IntelliJ IDEA 默认 Spring Boot 风格
- 禁止手动修改代码缩进（依赖 IDE 自动格式化）
- 导入语句按 IDE 设置自动排序

### 8.4 日志规范

- 使用 SLF4J（禁止使用 `System.out.println`）
- 核心操作记录 INFO 级别日志
- 异常记录 ERROR 级别日志（带堆栈）

```
private static final Logger log = LoggerFactory.getLogger(AuthService.class);

// 正确示例
log.info("User registered: userId={}, email={}", user.getId(), user.getEmail());
log.error("Login failed: userId={}, reason={}", userId, reason, ex);

// 错误示例（禁止使用）
System.out.println("Debug info")
```

