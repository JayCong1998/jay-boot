package com.jaycong.boot.modules.log.aspect;

import com.jaycong.boot.modules.auth.context.LoginContext;
import com.jaycong.boot.modules.auth.context.LoginPrincipal;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.log.context.RequestContext;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import com.jaycong.boot.modules.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 操作日志切面类。
 * 通过AOP自动拦截带有@OperationLog注解的方法，记录操作日志。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    /**
     * 定义切点：所有带有@OperationLog注解的方法。
     */
    @Pointcut("@annotation(com.jaycong.boot.modules.log.annotation.OperationLog)")
    public void operationLogPointcut() {}

    /**
     * 环绕通知：拦截方法执行并记录操作日志。
     *
     * @param point 连接点
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 先执行业务方法
        Object result = point.proceed();

        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);

            // 解析 SpEL 表达式
            String detail = parseSpel(annotation.detail(), point);

            // 构建日志实体
            OperationLogEntity entity = buildLogEntity(annotation, detail);

            // 保存日志
            operationLogService.record(entity);
        } catch (Exception e) {
            // 日志记录失败不影响业务
            log.warn("操作日志记录失败", e);
        }

        return result;
    }

    /**
     * 解析SpEL表达式。
     *
     * @param template 表达式模板
     * @param point    连接点
     * @return 解析后的字符串
     */
    private String parseSpel(String template, ProceedingJoinPoint point) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        if (!template.contains("#{")) {
            return template;
        }

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = point.getArgs();

            // 处理混合模板，如 "删除用户：#{#username}"
            return parseMixedTemplate(template, paramNames, args);
        } catch (Exception e) {
            log.debug("SpEL 表达式解析失败: {}", template, e);
            return template;
        }
    }

    /**
     * 解析混合模板中的SpEL表达式。
     *
     * @param template   模板字符串
     * @param paramNames 参数名数组
     * @param args       参数值数组
     * @return 解析后的字符串
     */
    private String parseMixedTemplate(String template, String[] paramNames, Object[] args) {
        String result = template;
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        int start;
        while ((start = result.indexOf("#{")) != -1) {
            int end = result.indexOf("}", start);
            if (end == -1) break;

            String expr = result.substring(start + 2, end);
            try {
                Expression expression = spelParser.parseExpression(expr);
                Object value = expression.getValue(context);
                String replacement = value != null ? String.valueOf(value) : "";
                result = result.substring(0, start) + replacement + result.substring(end + 1);
            } catch (Exception e) {
                break;
            }
        }
        return result;
    }

    /**
     * 构建操作日志实体。
     *
     * @param annotation 操作日志注解
     * @param detail     操作详情
     * @return 操作日志实体
     */
    private OperationLogEntity buildLogEntity(OperationLog annotation, String detail) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setModule(annotation.module());
        entity.setAction(annotation.action());
        entity.setDetail(detail);

        // 从登录上下文获取用户信息
        Optional<LoginPrincipal> principal = LoginContext.currentPrincipal();
        if (principal.isPresent()) {
            LoginPrincipal p = principal.get();
            entity.setUserId(p.userId());
            entity.setUsername(p.username());
        }

        // 从请求上下文获取 IP 和 RequestId
        RequestContext requestContext = RequestContext.current();
        if (requestContext != null) {
            entity.setRequestId(requestContext.getRequestId());
        }

        return entity;
    }
}
