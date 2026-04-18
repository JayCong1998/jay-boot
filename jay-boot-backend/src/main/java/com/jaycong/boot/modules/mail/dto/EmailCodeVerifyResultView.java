package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "验证码校验结果")
public record EmailCodeVerifyResultView(
        @Schema(description = "是否通过")
        boolean passed
) {
}

