package com.jaycong.boot.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置，统一生成 /api/** 接口文档。
 */
@Configuration
public class SwaggerConfig {

    /**
     * 定义基础 OpenAPI 信息与 sa-token 鉴权头。
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jay Boot API")
                        .version("1.0")
                        .description("Jay Boot 项目接口文档"))
                .components(new Components()
                        .addSecuritySchemes("satoken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("satoken")));
    }

    /**
     * 仅暴露业务 API 路径到文档分组。
     *
     * @return OpenAPI 分组
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(authPathSecurityCustomizer())
                .build();
    }

    /**
     * 根据路径自动标注需要 satoken 的接口。
     * 与 Sa-Token 全局拦截白名单保持一致：
     * - 放行：/api/auth/**、/api/admin/auth/**、/api/user/auth/**、/api/system/ping
     * - 其余 /api/** 需要登录
     */
    @Bean
    public OpenApiCustomizer authPathSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) -> {
                if (!requiresAuth(path)) {
                    return;
                }
                pathItem.readOperations().forEach(this::appendSaTokenRequirement);
            });
        };
    }

    private boolean requiresAuth(String path) {
        if (path == null || !path.startsWith("/api/")) {
            return false;
        }
        if ("/api/system/ping".equals(path)) {
            return false;
        }
        return !path.startsWith("/api/auth/")
                && !path.startsWith("/api/admin/auth/")
                && !path.startsWith("/api/user/auth/");
    }

    private void appendSaTokenRequirement(Operation operation) {
        if (operation == null) {
            return;
        }
        List<SecurityRequirement> securityRequirements = operation.getSecurity();
        if (securityRequirements == null) {
            securityRequirements = new ArrayList<>();
            operation.setSecurity(securityRequirements);
        }
        boolean exists = securityRequirements.stream().anyMatch(req -> req.containsKey("satoken"));
        if (!exists) {
            securityRequirements.add(new SecurityRequirement().addList("satoken"));
        }
    }
}
