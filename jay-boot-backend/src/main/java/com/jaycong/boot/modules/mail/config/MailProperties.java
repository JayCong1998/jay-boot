package com.jaycong.boot.modules.mail.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private boolean enabled = true;
    private VerifyCode verifyCode = new VerifyCode();
    private Retry retry = new Retry();
    private Security security = new Security();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public VerifyCode getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(VerifyCode verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class VerifyCode {
        private int ttlSeconds = 60;
        private int cooldownSeconds = 60;
        private int hourlyLimit = 10;
        private int length = 6;
        private int maxFailuresPerHour = 5;

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(int cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }

        public int getHourlyLimit() {
            return hourlyLimit;
        }

        public void setHourlyLimit(int hourlyLimit) {
            this.hourlyLimit = hourlyLimit;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getMaxFailuresPerHour() {
            return maxFailuresPerHour;
        }

        public void setMaxFailuresPerHour(int maxFailuresPerHour) {
            this.maxFailuresPerHour = maxFailuresPerHour;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private List<Integer> intervalsSeconds = new ArrayList<>(List.of(60, 300, 1800));
        private int scanSize = 100;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public List<Integer> getIntervalsSeconds() {
            return intervalsSeconds;
        }

        public void setIntervalsSeconds(List<Integer> intervalsSeconds) {
            this.intervalsSeconds = intervalsSeconds;
        }

        public int getScanSize() {
            return scanSize;
        }

        public void setScanSize(int scanSize) {
            this.scanSize = scanSize;
        }
    }

    public static class Security {
        private String secretKey = "change-me";

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}

