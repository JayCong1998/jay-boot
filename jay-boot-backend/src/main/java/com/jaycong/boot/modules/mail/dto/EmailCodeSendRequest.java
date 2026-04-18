package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailSceneCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "发送邮箱验证码请求")
public record EmailCodeSendRequest(
        @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,
        @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "场景编码不能为空")
        MailSceneCode sceneCode
) {
}

