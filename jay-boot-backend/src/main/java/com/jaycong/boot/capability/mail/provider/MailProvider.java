package com.jaycong.boot.capability.mail.provider;

import com.jaycong.boot.capability.mail.entity.MailChannelEntity;

public interface MailProvider {

    void send(MailChannelEntity channel, String recipientEmail, String subject, String body, boolean html);
}

