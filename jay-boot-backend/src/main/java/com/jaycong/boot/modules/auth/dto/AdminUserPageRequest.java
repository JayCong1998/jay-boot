package com.jaycong.boot.modules.auth.dto;

import com.jaycong.boot.common.constant.enums.AdminUserRole;
import com.jaycong.boot.common.constant.enums.AdminUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端用户分页查询请求")
public record AdminUserPageRequest(
        @Schema(description = "页码，从 1 开始", example = "1")
        Integer page,
        @Schema(description = "每页条数", example = "10")
        Integer pageSize,
        @Schema(description = "关键词（用户名/邮箱）", example = "alice")
        String keyword,
        @Schema(description = "角色")
        AdminUserRole role,
        @Schema(description = "状态")
        AdminUserStatus status
) {
}

