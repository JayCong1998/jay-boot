package com.jaycong.boot.common.util;

import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 统一参数校验工具类，提供静态方法和链式调用两种使用方式。
 *
 * <p>静态方法示例：
 * <pre>
 * ValidateUtil.notNull(user, "用户不存在");
 * ValidateUtil.notBlank(email, "邮箱不能为空");
 * </pre>
 *
 * <p>链式调用示例：
 * <pre>
 * ValidateUtil.chain()
 *     .notNull(user, "用户不存在")
 *     .isTrue(user.isActive(), "用户已被禁用")
 *     .notBlank(user.getEmail(), "用户邮箱不能为空");
 * </pre>
 */
public final class ValidateUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private ValidateUtil() {
    }

    // ==================== 静态方法 ====================

    /**
     * 校验对象不能为 null。
     *
     * @param obj     待校验对象
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果对象为 null
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验字符串不能为 null、空字符串或纯空白字符。
     *
     * @param str     待校验字符串
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果字符串为 null、空或纯空白
     */
    public static void notBlank(String str, String message) {
        if (!StringUtils.hasText(str)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验字符串不能为 null 或空字符串。
     *
     * @param str     待校验字符串
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果字符串为 null 或空
     */
    public static void notEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验集合不能为 null 或空。
     *
     * @param collection 待校验集合
     * @param message    校验失败时的错误信息
     * @throws BusinessException 如果集合为 null 或空
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验条件必须为 true。
     *
     * @param condition 待校验条件
     * @param message   校验失败时的错误信息
     * @throws BusinessException 如果条件为 false
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验条件必须为 false。
     *
     * @param condition 待校验条件
     * @param message   校验失败时的错误信息
     * @throws BusinessException 如果条件为 true
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验字符串最小长度。
     *
     * @param str     待校验字符串
     * @param min     最小长度（包含）
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果字符串长度小于最小值
     */
    public static void minLength(String str, int min, String message) {
        if (str == null || str.length() < min) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验字符串最大长度。
     *
     * @param str     待校验字符串
     * @param max     最大长度（包含）
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果字符串长度超过最大值
     */
    public static void maxLength(String str, int max, String message) {
        if (str != null && str.length() > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验整数数值范围。
     *
     * @param value   待校验数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果数值不在范围内
     */
    public static void range(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验长整数数值范围。
     *
     * @param value   待校验数值
     * @param min     最小值（包含）
     * @param max     最大值（包含）
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果数值不在范围内
     */
    public static void range(long value, long min, long max, String message) {
        if (value < min || value > max) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验邮箱格式。
     *
     * @param email   待校验邮箱
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果邮箱格式不正确
     */
    public static void email(String email, String message) {
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    /**
     * 校验字符串是否匹配正则表达式。
     *
     * @param str     待校验字符串
     * @param regex   正则表达式
     * @param message 校验失败时的错误信息
     * @throws BusinessException 如果字符串不匹配正则
     */
    public static void matches(String str, String regex, String message) {
        if (!StringUtils.hasText(str) || !Pattern.matches(regex, str)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    // ==================== 链式调用 ====================

    /**
     * 创建链式校验器。
     *
     * @return 链式校验器实例
     */
    public static Chain chain() {
        return new Chain();
    }

    /**
     * 链式校验器内部类。
     */
    public static final class Chain {

        private Chain() {
        }

        /**
         * 校验对象不能为 null。
         *
         * @param obj     待校验对象
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果对象为 null
         */
        public Chain notNull(Object obj, String message) {
            ValidateUtil.notNull(obj, message);
            return this;
        }

        /**
         * 校验字符串不能为 null、空字符串或纯空白字符。
         *
         * @param str     待校验字符串
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果字符串为 null、空或纯空白
         */
        public Chain notBlank(String str, String message) {
            ValidateUtil.notBlank(str, message);
            return this;
        }

        /**
         * 校验字符串不能为 null 或空字符串。
         *
         * @param str     待校验字符串
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果字符串为 null 或空
         */
        public Chain notEmpty(String str, String message) {
            ValidateUtil.notEmpty(str, message);
            return this;
        }

        /**
         * 校验集合不能为 null 或空。
         *
         * @param collection 待校验集合
         * @param message    校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果集合为 null 或空
         */
        public Chain notEmpty(Collection<?> collection, String message) {
            ValidateUtil.notEmpty(collection, message);
            return this;
        }

        /**
         * 校验条件必须为 true。
         *
         * @param condition 待校验条件
         * @param message   校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果条件为 false
         */
        public Chain isTrue(boolean condition, String message) {
            ValidateUtil.isTrue(condition, message);
            return this;
        }

        /**
         * 校验条件必须为 false。
         *
         * @param condition 待校验条件
         * @param message   校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果条件为 true
         */
        public Chain isFalse(boolean condition, String message) {
            ValidateUtil.isFalse(condition, message);
            return this;
        }

        /**
         * 校验字符串最小长度。
         *
         * @param str     待校验字符串
         * @param min     最小长度（包含）
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果字符串长度小于最小值
         */
        public Chain minLength(String str, int min, String message) {
            ValidateUtil.minLength(str, min, message);
            return this;
        }

        /**
         * 校验字符串最大长度。
         *
         * @param str     待校验字符串
         * @param max     最大长度（包含）
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果字符串长度超过最大值
         */
        public Chain maxLength(String str, int max, String message) {
            ValidateUtil.maxLength(str, max, message);
            return this;
        }

        /**
         * 校验整数数值范围。
         *
         * @param value   待校验数值
         * @param min     最小值（包含）
         * @param max     最大值（包含）
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果数值不在范围内
         */
        public Chain range(int value, int min, int max, String message) {
            ValidateUtil.range(value, min, max, message);
            return this;
        }

        /**
         * 校验长整数数值范围。
         *
         * @param value   待校验数值
         * @param min     最小值（包含）
         * @param max     最大值（包含）
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果数值不在范围内
         */
        public Chain range(long value, long min, long max, String message) {
            ValidateUtil.range(value, min, max, message);
            return this;
        }

        /**
         * 校验邮箱格式。
         *
         * @param email   待校验邮箱
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果邮箱格式不正确
         */
        public Chain email(String email, String message) {
            ValidateUtil.email(email, message);
            return this;
        }

        /**
         * 校验字符串是否匹配正则表达式。
         *
         * @param str     待校验字符串
         * @param regex   正则表达式
         * @param message 校验失败时的错误信息
         * @return 当前链式校验器，用于继续链式调用
         * @throws BusinessException 如果字符串不匹配正则
         */
        public Chain matches(String str, String regex, String message) {
            ValidateUtil.matches(str, regex, message);
            return this;
        }
    }
}
