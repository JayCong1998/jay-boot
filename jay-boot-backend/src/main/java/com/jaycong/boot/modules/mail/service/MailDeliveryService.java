package com.jaycong.boot.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.util.ValidateUtil;
import com.jaycong.boot.modules.mail.config.MailProperties;
import com.jaycong.boot.modules.mail.constant.MailBodyType;
import com.jaycong.boot.modules.mail.constant.MailSendStatus;
import com.jaycong.boot.modules.mail.dto.MailDeliveryCommand;
import com.jaycong.boot.modules.mail.entity.MailChannelEntity;
import com.jaycong.boot.modules.mail.entity.MailSendLogEntity;
import com.jaycong.boot.modules.mail.entity.MailTemplateEntity;
import com.jaycong.boot.modules.mail.mapper.MailSendLogMapper;
import com.jaycong.boot.modules.mail.provider.MailProvider;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MailDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(MailDeliveryService.class);

    private final MailTemplateService mailTemplateService;
    private final MailChannelService mailChannelService;
    private final TemplateRenderService templateRenderService;
    private final MailProvider mailProvider;
    private final MailSendLogMapper mailSendLogMapper;
    private final MailProperties mailProperties;

    public MailDeliveryService(MailTemplateService mailTemplateService,
                               MailChannelService mailChannelService,
                               TemplateRenderService templateRenderService,
                               MailProvider mailProvider,
                               MailSendLogMapper mailSendLogMapper,
                               MailProperties mailProperties) {
        this.mailTemplateService = mailTemplateService;
        this.mailChannelService = mailChannelService;
        this.templateRenderService = templateRenderService;
        this.mailProvider = mailProvider;
        this.mailSendLogMapper = mailSendLogMapper;
        this.mailProperties = mailProperties;
    }

    @Transactional
    public MailSendLogEntity deliver(MailDeliveryCommand command) {
        ValidateUtil.notNull(command, "邮件投递参数不能为空");
        String recipientEmail = normalizeEmail(command.recipientEmail());
        ValidateUtil.email(recipientEmail, "收件邮箱格式不正确");
        checkBizKeyDuplicate(command.bizKey(), command.strictMode());

        MailTemplateEntity template = resolveTemplate(command);
        MailChannelEntity channel = mailChannelService.pickEnabledChannel();
        Map<String, Object> variables = command.variables() == null ? Collections.emptyMap() : command.variables();
        String subject = templateRenderService.render(template.getSubjectTemplate(), variables);
        String body = templateRenderService.render(template.getBodyTemplate(), variables);

        MailSendLogEntity logEntity = new MailSendLogEntity();
        logEntity.setBizType(command.bizType().value());
        logEntity.setSceneCode(template.getSceneCode());
        logEntity.setTemplateCode(template.getTemplateCode());
        logEntity.setChannelCode(channel.getChannelCode());
        logEntity.setRecipientEmail(recipientEmail);
        logEntity.setSubjectRendered(subject);
        logEntity.setBodyRendered(body);
        logEntity.setBizKey(normalizeOptionalText(command.bizKey(), 128));
        logEntity.setTraceId(normalizeOptionalText(command.traceId(), 64));
        logEntity.setStatus(MailSendStatus.PENDING.value());
        logEntity.setRetryCount(0);
        logEntity.setMaxRetryCount(mailProperties.getRetry().getMaxAttempts());
        mailSendLogMapper.insert(logEntity);

        boolean html = MailBodyType.fromValue(template.getBodyType()) == MailBodyType.HTML;
        try {
            mailProvider.send(channel, recipientEmail, subject, body, html);
            markSuccess(logEntity.getId());
        } catch (Exception ex) {
            markInitialFailed(logEntity.getId(), ex);
            if (command.strictMode()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "邮件发送失败，请稍后重试");
            }
        }
        return mailSendLogMapper.selectById(logEntity.getId());
    }

    @Transactional
    public void retryByLogId(Long logId) {
        MailSendLogEntity logEntity = mailSendLogMapper.selectById(logId);
        if (logEntity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件日志不存在");
        }
        if (MailSendStatus.SUCCESS.value().equalsIgnoreCase(logEntity.getStatus())) {
            return;
        }
        if (logEntity.getRetryCount() != null && logEntity.getRetryCount() >= logEntity.getMaxRetryCount()) {
            return;
        }

        MailChannelEntity channel = mailChannelService.findByCode(logEntity.getChannelCode());
        if (channel == null) {
            markRetryFailed(logEntity, new IllegalStateException("邮件通道不存在"), false);
            return;
        }

        try {
            mailProvider.send(
                    channel,
                    logEntity.getRecipientEmail(),
                    logEntity.getSubjectRendered(),
                    logEntity.getBodyRendered(),
                    true
            );
            markSuccess(logEntity.getId());
        } catch (Exception ex) {
            markRetryFailed(logEntity, ex, true);
        }
    }

    public List<MailSendLogEntity> listRetryCandidates(int limit) {
        int scanSize = limit > 0 ? limit : mailProperties.getRetry().getScanSize();
        LambdaQueryWrapper<MailSendLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailSendLogEntity::getStatus, MailSendStatus.FAILED.value())
                .isNotNull(MailSendLogEntity::getNextRetryTime)
                .le(MailSendLogEntity::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(MailSendLogEntity::getNextRetryTime)
                .last("limit " + scanSize);
        return mailSendLogMapper.selectList(wrapper);
    }

    private MailTemplateEntity resolveTemplate(MailDeliveryCommand command) {
        if (StringUtils.hasText(command.templateCode())) {
            return mailTemplateService.findEnabledByCode(command.templateCode());
        }
        return mailTemplateService.findEnabledByBizScene(command.bizType(), command.sceneCode());
    }

    private void checkBizKeyDuplicate(String bizKey, boolean strictMode) {
        if (!StringUtils.hasText(bizKey)) {
            return;
        }
        String normalizedBizKey = bizKey.trim();
        LambdaQueryWrapper<MailSendLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailSendLogEntity::getBizKey, normalizedBizKey).last("limit 1");
        MailSendLogEntity existing = mailSendLogMapper.selectOne(wrapper);
        if (existing != null && strictMode) {
            throw new BusinessException(ErrorCode.CONFLICT, "业务幂等键重复");
        }
    }

    private void markSuccess(Long logId) {
        MailSendLogEntity update = new MailSendLogEntity();
        update.setId(logId);
        update.setStatus(MailSendStatus.SUCCESS.value());
        update.setErrorCode(null);
        update.setErrorMessage(null);
        update.setNextRetryTime(null);
        update.setSentTime(LocalDateTime.now());
        mailSendLogMapper.updateById(update);
    }

    private void markInitialFailed(Long logId, Exception exception) {
        MailSendLogEntity update = new MailSendLogEntity();
        update.setId(logId);
        update.setStatus(MailSendStatus.FAILED.value());
        update.setErrorCode("SEND_ERROR");
        update.setErrorMessage(extractErrorMessage(exception));
        update.setNextRetryTime(LocalDateTime.now().plusSeconds(resolveRetryInterval(0)));
        mailSendLogMapper.updateById(update);
        log.warn("Send mail failed, logId={}", logId, exception);
    }

    private void markRetryFailed(MailSendLogEntity logEntity, Exception exception, boolean increaseRetryCount) {
        int currentRetry = logEntity.getRetryCount() == null ? 0 : logEntity.getRetryCount();
        int nextRetry = increaseRetryCount ? currentRetry + 1 : currentRetry;
        Integer maxRetry = logEntity.getMaxRetryCount() == null ? mailProperties.getRetry().getMaxAttempts() : logEntity.getMaxRetryCount();
        boolean canRetry = nextRetry < maxRetry;

        MailSendLogEntity update = new MailSendLogEntity();
        update.setId(logEntity.getId());
        update.setStatus(MailSendStatus.FAILED.value());
        update.setRetryCount(nextRetry);
        update.setErrorCode("RETRY_ERROR");
        update.setErrorMessage(extractErrorMessage(exception));
        update.setNextRetryTime(canRetry ? LocalDateTime.now().plusSeconds(resolveRetryInterval(nextRetry)) : null);
        mailSendLogMapper.updateById(update);
        log.warn("Retry mail failed, logId={}, retryCount={}", logEntity.getId(), nextRetry, exception);
    }

    private long resolveRetryInterval(int attemptIndex) {
        List<Integer> intervals = mailProperties.getRetry().getIntervalsSeconds();
        if (intervals == null || intervals.isEmpty()) {
            return 60L;
        }
        int normalizedIndex = Math.max(0, Math.min(attemptIndex, intervals.size() - 1));
        Integer value = intervals.get(normalizedIndex);
        return value == null || value <= 0 ? 60L : value;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        ValidateUtil.maxLength(normalized, maxLength, "文本长度不能超过" + maxLength);
        return normalized;
    }

    private String extractErrorMessage(Exception exception) {
        String message = exception == null ? "unknown error" : exception.getMessage();
        if (!StringUtils.hasText(message)) {
            message = "unknown error";
        }
        if (message.length() > 512) {
            return message.substring(0, 512);
        }
        return message;
    }
}

