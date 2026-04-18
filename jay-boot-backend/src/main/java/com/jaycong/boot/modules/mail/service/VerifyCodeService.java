package com.jaycong.boot.modules.mail.service;

import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.mail.config.MailProperties;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.dto.EmailCodeSendRequest;
import com.jaycong.boot.modules.mail.dto.EmailCodeSendResultView;
import com.jaycong.boot.modules.mail.dto.EmailCodeVerifyRequest;
import com.jaycong.boot.modules.mail.dto.EmailCodeVerifyResultView;
import com.jaycong.boot.modules.mail.dto.MailDeliveryCommand;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VerifyCodeService {

    private static final String CODE_KEY_PREFIX = "mail:vc:code:";
    private static final String COOLDOWN_KEY_PREFIX = "mail:vc:cooldown:";
    private static final String HOURLY_COUNT_KEY_PREFIX = "mail:vc:h1:";
    private static final String FAIL_COUNT_KEY_PREFIX = "mail:vc:fail:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MailProperties mailProperties;
    private final MailDeliveryService mailDeliveryService;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerifyCodeService(StringRedisTemplate stringRedisTemplate,
                             MailProperties mailProperties,
                             MailDeliveryService mailDeliveryService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailProperties = mailProperties;
        this.mailDeliveryService = mailDeliveryService;
    }

    public EmailCodeSendResultView sendCode(EmailCodeSendRequest request) {
        ensureEnabled();
        String email = normalizeEmail(request.email());
        String scene = request.sceneCode().value();

        checkCooldown(email, scene);
        checkHourlyLimit(email, scene);

        String code = generateCode(mailProperties.getVerifyCode().getLength());
        String codeHash = hashCode(scene, email, code);
        String codeKey = buildCodeKey(email, scene);
        String cooldownKey = buildCooldownKey(email, scene);

        stringRedisTemplate.opsForValue().set(
                codeKey,
                codeHash,
                Duration.ofSeconds(mailProperties.getVerifyCode().getTtlSeconds())
        );
        stringRedisTemplate.opsForValue().set(
                cooldownKey,
                "1",
                Duration.ofSeconds(mailProperties.getVerifyCode().getCooldownSeconds())
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("expireSeconds", mailProperties.getVerifyCode().getTtlSeconds());
        variables.put("expireMinutes", Math.max(1, mailProperties.getVerifyCode().getTtlSeconds() / 60));
        variables.put("sceneCode", scene);

        mailDeliveryService.deliver(new MailDeliveryCommand(
                MailBizType.VERIFY_CODE,
                scene,
                null,
                email,
                variables,
                null,
                null,
                true
        ));
        return new EmailCodeSendResultView(mailProperties.getVerifyCode().getTtlSeconds());
    }

    public EmailCodeVerifyResultView verifyCode(EmailCodeVerifyRequest request) {
        ensureEnabled();
        String email = normalizeEmail(request.email());
        String scene = request.sceneCode().value();
        String code = request.code().trim();
        String codeKey = buildCodeKey(email, scene);
        String storedHash = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(storedHash)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不存在或已过期");
        }
        String actualHash = hashCode(scene, email, code);
        if (!storedHash.equals(actualHash)) {
            handleVerifyFailed(email, scene);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
        clearCodeCache(email, scene);
        return new EmailCodeVerifyResultView(true);
    }

    private void checkCooldown(String email, String scene) {
        String cooldownKey = buildCooldownKey(email, scene);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后重试");
        }
    }

    private void checkHourlyLimit(String email, String scene) {
        String key = buildHourlyCountKey(email, scene);
        String current = stringRedisTemplate.opsForValue().get(key);
        int currentCount = parseInt(current);
        if (currentCount >= mailProperties.getVerifyCode().getHourlyLimit()) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "验证码发送次数已达上限，请稍后重试");
        }

        Long next = stringRedisTemplate.opsForValue().increment(key);
        if (next != null && next == 1L) {
            stringRedisTemplate.expire(key, Duration.ofHours(1));
        }
    }

    private void handleVerifyFailed(String email, String scene) {
        String failKey = buildFailCountKey(email, scene);
        Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
        if (failCount != null && failCount == 1L) {
            stringRedisTemplate.expire(failKey, Duration.ofHours(1));
        }
        int maxFailures = Math.max(1, mailProperties.getVerifyCode().getMaxFailuresPerHour());
        if (failCount != null && failCount >= maxFailures) {
            stringRedisTemplate.delete(buildCodeKey(email, scene));
        }
    }

    private void clearCodeCache(String email, String scene) {
        stringRedisTemplate.delete(buildCodeKey(email, scene));
        stringRedisTemplate.delete(buildFailCountKey(email, scene));
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱不能为空");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱格式不正确");
        }
        return normalized;
    }

    private String generateCode(int length) {
        int validLength = Math.max(4, Math.min(length, 8));
        int bound = (int) Math.pow(10, validLength);
        int floor = (int) Math.pow(10, validLength - 1);
        int value = floor + secureRandom.nextInt(bound - floor);
        return String.valueOf(value);
    }

    private String hashCode(String scene, String email, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String payload = scene + "|" + email + "|" + code + "|" + mailProperties.getSecurity().getSecretKey();
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hashed) {
                String hex = Integer.toHexString(item & 0xff);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Hash verify code failed", ex);
        }
    }

    private int parseInt(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void ensureEnabled() {
        if (!mailProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮件能力未启用");
        }
    }

    private String buildCodeKey(String email, String scene) {
        return CODE_KEY_PREFIX + scene + ":" + email;
    }

    private String buildCooldownKey(String email, String scene) {
        return COOLDOWN_KEY_PREFIX + scene + ":" + email;
    }

    private String buildHourlyCountKey(String email, String scene) {
        return HOURLY_COUNT_KEY_PREFIX + scene + ":" + email;
    }

    private String buildFailCountKey(String email, String scene) {
        return FAIL_COUNT_KEY_PREFIX + scene + ":" + email;
    }
}

