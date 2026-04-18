package com.jaycong.boot.modules.mail.provider;

import com.jaycong.boot.modules.mail.entity.MailChannelEntity;

public interface MailProvider {

    void send(MailChannelEntity channel, String recipientEmail, String subject, String body, boolean html);
}

