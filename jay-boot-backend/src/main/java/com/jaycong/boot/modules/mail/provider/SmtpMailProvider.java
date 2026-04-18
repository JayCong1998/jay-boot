package com.jaycong.boot.modules.mail.provider;

import com.jaycong.boot.modules.mail.constant.MailTlsMode;
import com.jaycong.boot.modules.mail.entity.MailChannelEntity;
import com.jaycong.boot.modules.mail.util.MailEncryptor;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailProvider implements MailProvider {

    private final MailEncryptor mailEncryptor;

    public SmtpMailProvider(MailEncryptor mailEncryptor) {
        this.mailEncryptor = mailEncryptor;
    }

    @Override
    public void send(MailChannelEntity channel, String recipientEmail, String subject, String body, boolean html) {
        JavaMailSenderImpl sender = buildSender(channel);
        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(body, html);
            helper.setFrom(new InternetAddress(channel.getFromEmail(), channel.getFromName(), StandardCharsets.UTF_8.name()));
            sender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Send mail failed", ex);
        }
    }

    private JavaMailSenderImpl buildSender(MailChannelEntity channel) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(channel.getSmtpHost());
        sender.setPort(channel.getSmtpPort() == null ? 25 : channel.getSmtpPort());
        sender.setUsername(channel.getSmtpUsername());
        sender.setPassword(mailEncryptor.decrypt(channel.getSmtpPasswordCipher()));
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        sender.setJavaMailProperties(buildProperties(channel));
        return sender;
    }

    private Properties buildProperties(MailChannelEntity channel) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");
        MailTlsMode tlsMode = MailTlsMode.fromValue(channel.getTlsMode());
        if (tlsMode == MailTlsMode.STARTTLS) {
            properties.put("mail.smtp.starttls.enable", "true");
        } else if (tlsMode == MailTlsMode.SSL) {
            properties.put("mail.smtp.ssl.enable", "true");
        }
        return properties;
    }
}

