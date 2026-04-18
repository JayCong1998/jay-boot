package com.jaycong.boot.modules.mail.service;

import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.util.ValidateUtil;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.dto.AdminMailNotificationSendRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailNotificationSendResultView;
import com.jaycong.boot.modules.mail.dto.MailDeliveryCommand;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MailNotificationService {

    private final MailDeliveryService mailDeliveryService;

    public MailNotificationService(MailDeliveryService mailDeliveryService) {
        this.mailDeliveryService = mailDeliveryService;
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "发送系统通知", detail = "发送系统通知模板：#{#request.templateCode}")
    public AdminMailNotificationSendResultView send(AdminMailNotificationSendRequest request) {
        ValidateUtil.notBlank(request.templateCode(), "模板编码不能为空");
        Set<String> recipients = normalizeRecipients(request.recipientEmails());
        Map<String, Object> variables = request.variables();

        int success = 0;
        List<String> failedEmails = new ArrayList<>();
        int index = 0;
        for (String recipient : recipients) {
            String bizKey = buildBizKey(request.bizKey(), recipient, index);
            try {
                mailDeliveryService.deliver(new MailDeliveryCommand(
                        MailBizType.SYSTEM_NOTICE,
                        null,
                        request.templateCode(),
                        recipient,
                        variables,
                        bizKey,
                        null,
                        false
                ));
                success++;
            } catch (Exception ex) {
                failedEmails.add(recipient);
            }
            index++;
        }
        return new AdminMailNotificationSendResultView(success, failedEmails.size(), failedEmails);
    }

    private Set<String> normalizeRecipients(List<String> rawRecipients) {
        ValidateUtil.notEmpty(rawRecipients, "收件人不能为空");
        Set<String> recipients = new LinkedHashSet<>();
        for (String recipient : rawRecipients) {
            if (!StringUtils.hasText(recipient)) {
                continue;
            }
            String email = recipient.trim().toLowerCase(Locale.ROOT);
            ValidateUtil.email(email, "收件邮箱格式不正确");
            recipients.add(email);
        }
        if (recipients.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "收件人不能为空");
        }
        if (recipients.size() > 200) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单次最多发送200个收件人");
        }
        return recipients;
    }

    private String buildBizKey(String rawBizKey, String recipientEmail, int index) {
        if (!StringUtils.hasText(rawBizKey)) {
            return null;
        }
        String normalized = rawBizKey.trim();
        ValidateUtil.maxLength(normalized, 128, "业务幂等键长度不能超过128");
        return normalized + ":" + recipientEmail + ":" + index;
    }
}

