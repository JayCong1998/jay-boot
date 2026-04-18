package com.jaycong.boot.modules.mail.job;

import com.jaycong.boot.modules.mail.config.MailProperties;
import com.jaycong.boot.modules.mail.entity.MailSendLogEntity;
import com.jaycong.boot.modules.mail.service.MailDeliveryService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MailRetryJob {

    private static final Logger log = LoggerFactory.getLogger(MailRetryJob.class);

    private final MailDeliveryService mailDeliveryService;
    private final MailProperties mailProperties;

    public MailRetryJob(MailDeliveryService mailDeliveryService, MailProperties mailProperties) {
        this.mailDeliveryService = mailDeliveryService;
        this.mailProperties = mailProperties;
    }

    @Scheduled(fixedDelay = 30000)
    public void retryFailedMails() {
        if (!mailProperties.isEnabled()) {
            return;
        }
        List<MailSendLogEntity> candidates = mailDeliveryService.listRetryCandidates(mailProperties.getRetry().getScanSize());
        for (MailSendLogEntity candidate : candidates) {
            try {
                mailDeliveryService.retryByLogId(candidate.getId());
            } catch (Exception ex) {
                log.warn("Retry mail failed in job, logId={}", candidate.getId(), ex);
            }
        }
    }
}

