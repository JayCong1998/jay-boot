package com.jaycong.boot.modules.lock.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * SpEL 表达式解析器
 * 用于解析分布式锁 key 中的 SpEL 表达式
 */
public class SpelKeyParser {
    
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    
    /**
     * 解析 SpEL 表达式
     *
     * @param spel  SpEL 表达式
     * @param point 切点
     * @return 解析后的字符串
     */
    public static String parse(String spel, ProceedingJoinPoint point) {
        if (spel == null || spel.isEmpty()) {
            return "";
        }
        
        // 如果不包含 SpEL 表达式特征，直接返回
        if (!spel.contains("#") && !spel.contains("T(")) {
            return spel;
        }
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();
        Parameter[] parameters = method.getParameters();
        
        // 创建上下文
        EvaluationContext context = new StandardEvaluationContext();
        
        // 将方法参数绑定到上下文
        for (int i = 0; i < parameters.length && i < args.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
        
        // 解析表达式
        Expression expression = PARSER.parseExpression(spel);
        Object value = expression.getValue(context);
        
        return value != null ? value.toString() : "";
    }
}
