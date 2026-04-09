package com.jaycong.boot.common.web;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统健康检查控制器。
 */
@RestController
@Tag(name = "系统状态", description = "系统健康检查接口")
public class SystemHealthController {

    /**
     * 返回系统基础存活状态。
     */
    @Operation(summary = "系统存活检查")
    @GetMapping("/api/system/ping")
    public Map<String, Object> ping() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("app", "jay-boot");
        payload.put("status", "UP");
        payload.put("time", LocalDateTime.now());
        return payload;
    }
}
