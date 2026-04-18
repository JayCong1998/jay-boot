package com.jaycong.boot.modules.mail.util;

import com.jaycong.boot.modules.mail.config.MailProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MailEncryptor {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final byte[] keyBytes;
    private final SecureRandom secureRandom = new SecureRandom();

    public MailEncryptor(MailProperties mailProperties) {
        this.keyBytes = sha256(mailProperties.getSecurity().getSecretKey());
    }

    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new IllegalStateException("Encrypt smtp password failed", ex);
        }
    }

    public String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText)) {
            return cipherText;
        }
        try {
            String[] parts = cipherText.split(":");
            if (parts.length != 2) {
                return cipherText;
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Decrypt smtp password failed", ex);
        }
    }

    public String mask(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        int len = value.length();
        if (len <= 2) {
            return "*".repeat(len);
        }
        return value.charAt(0) + "*".repeat(Math.max(1, len - 2)) + value.charAt(len - 1);
    }

    private byte[] sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest((raw == null ? "" : raw).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Build encrypt key failed", ex);
        }
    }
}

