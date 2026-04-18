package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailSceneCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "校验邮箱验证码请求")
public record EmailCodeVerifyRequest(
        @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128")
        String email,
        @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "场景编码不能为空")
        MailSceneCode sceneCode,
        @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^[0-9]{4,8}$", message = "验证码格式不正确")
        String code
) {
}

