package com.jaycong.boot.modules.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 标注在方法上，AOP 切面会自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /**
     * 模块名称
     * 示例: "用户管理"、"套餐管理"
     */
    String module();

    /**
     * 操作类型
     * 示例: "创建"、"删除"、"修改"
     */
    String action();

    /**
     * 操作详情模板（支持 SpEL 表达式）
     * 示例: "删除用户：#{#username}"
     */
    String detail() default "";
}
