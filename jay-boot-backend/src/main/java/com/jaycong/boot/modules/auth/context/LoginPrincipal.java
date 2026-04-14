package com.jaycong.boot.modules.auth.context;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record LoginPrincipal(
        Long userId,
        String username,
        String email,
        String role,
        LoginClientType clientType,
        String loginIp,
        String loginUa,
        LocalDateTime loginTime
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
