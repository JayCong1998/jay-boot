package com.jaycong.boot.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailSendStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailSendLogItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailSendLogPageRequest;
import com.jaycong.boot.modules.mail.entity.MailSendLogEntity;
import com.jaycong.boot.modules.mail.mapper.MailSendLogMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MailSendLogService {

    private final MailSendLogMapper mailSendLogMapper;
    private final MailDeliveryService mailDeliveryService;

    public MailSendLogService(MailSendLogMapper mailSendLogMapper, MailDeliveryService mailDeliveryService) {
        this.mailSendLogMapper = mailSendLogMapper;
        this.mailDeliveryService = mailDeliveryService;
    }

    public PageResult<AdminMailSendLogItemView> page(AdminMailSendLogPageRequest request) {
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();
        LambdaQueryWrapper<MailSendLogEntity> wrapper = new LambdaQueryWrapper<>();
        if (request.bizType() != null) {
            wrapper.eq(MailSendLogEntity::getBizType, request.bizType().value());
        }
        if (StringUtils.hasText(request.sceneCode())) {
            wrapper.eq(MailSendLogEntity::getSceneCode, request.sceneCode().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(request.templateCode())) {
            wrapper.eq(MailSendLogEntity::getTemplateCode, request.templateCode().trim().toLowerCase(Locale.ROOT));
        }
        if (StringUtils.hasText(request.recipientEmail())) {
            wrapper.eq(MailSendLogEntity::getRecipientEmail, request.recipientEmail().trim().toLowerCase(Locale.ROOT));
        }
        if (request.status() != null) {
            wrapper.eq(MailSendLogEntity::getStatus, request.status().value());
        }
        wrapper.ge(StringUtils.hasText(request.startTime()), MailSendLogEntity::getCreatedTime, parseStartTime(request.startTime()))
                .le(StringUtils.hasText(request.endTime()), MailSendLogEntity::getCreatedTime, parseEndTime(request.endTime()))
                .orderByDesc(MailSendLogEntity::getCreatedTime);

        Page<MailSendLogEntity> page = mailSendLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminMailSendLogItemView> records = page.getRecords().stream().map(this::toView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AdminMailSendLogItemView getById(Long id) {
        MailSendLogEntity entity = mailSendLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件日志不存在");
        }
        return toView(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "重试邮件发送", detail = "重试邮件日志ID：#{#id}")
    public void retry(Long id) {
        mailDeliveryService.retryByLogId(id);
    }

    private AdminMailSendLogItemView toView(MailSendLogEntity entity) {
        return new AdminMailSendLogItemView(
                entity.getId(),
                MailBizType.fromValue(entity.getBizType()),
                entity.getSceneCode(),
                entity.getTemplateCode(),
                entity.getChannelCode(),
                entity.getRecipientEmail(),
                entity.getSubjectRendered(),
                entity.getBodyRendered(),
                entity.getBizKey(),
                entity.getTraceId(),
                MailSendStatus.fromValue(entity.getStatus()),
                entity.getErrorCode(),
                entity.getErrorMessage(),
                entity.getRetryCount(),
                entity.getMaxRetryCount(),
                entity.getNextRetryTime() == null ? null : entity.getNextRetryTime().toString(),
                entity.getSentTime() == null ? null : entity.getSentTime().toString(),
                entity.getCreatedTime() == null ? null : entity.getCreatedTime().toString()
        );
    }

    private LocalDateTime parseStartTime(String startTime) {
        if (!StringUtils.hasText(startTime)) {
            return null;
        }
        try {
            return LocalDate.parse(startTime.substring(0, 10)).atStartOfDay();
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime parseEndTime(String endTime) {
        if (!StringUtils.hasText(endTime)) {
            return null;
        }
        try {
            return LocalDate.parse(endTime.substring(0, 10)).atTime(LocalTime.MAX);
        } catch (Exception ex) {
            return null;
        }
    }
}

