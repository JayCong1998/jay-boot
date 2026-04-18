package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "发送验证码结果")
public record EmailCodeSendResultView(
        @Schema(description = "验证码有效秒数")
        int expireSeconds
) {
}

