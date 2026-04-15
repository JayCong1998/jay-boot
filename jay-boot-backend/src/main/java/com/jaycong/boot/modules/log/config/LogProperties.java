package com.jaycong.boot.modules.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.log")
public class LogProperties {

    private RequestLogConfig request = new RequestLogConfig();
    private ErrorLogConfig error = new ErrorLogConfig();

    public RequestLogConfig getRequest() {
        return request;
    }

    public void setRequest(RequestLogConfig request) {
        this.request = request;
    }

    public ErrorLogConfig getError() {
        return error;
    }

    public void setError(ErrorLogConfig error) {
        this.error = error;
    }

    public static class RequestLogConfig {
        private boolean enabled = true;
        private int maxCount = 100000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }

    public static class ErrorLogConfig {
        private boolean enabled = true;
        private int maxCount = 50000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }
}
