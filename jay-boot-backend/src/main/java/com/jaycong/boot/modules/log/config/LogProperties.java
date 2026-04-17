package com.jaycong.boot.modules.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志模块配置属性。
 * 用于配置请求日志和错误日志的开关及最大保留数量。
 */
@ConfigurationProperties(prefix = "app.log")
public class LogProperties {

    /** 请求日志配置 */
    private RequestLogConfig request = new RequestLogConfig();

    /** 错误日志配置 */
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

    /**
     * 请求日志配置项。
     */
    public static class RequestLogConfig {

        /** 是否启用请求日志记录 */
        private boolean enabled = true;

        /** 最大保留日志数量 */
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

    /**
     * 错误日志配置项。
     */
    public static class ErrorLogConfig {

        /** 是否启用错误日志记录 */
        private boolean enabled = true;

        /** 最大保留日志数量 */
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
