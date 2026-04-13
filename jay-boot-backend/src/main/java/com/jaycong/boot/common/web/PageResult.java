package com.jaycong.boot.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "分页结果")
public record PageResult<T>(
        @Schema(description = "当前页记录")
        List<T> records,
        @Schema(description = "总记录数", example = "58")
        long total,
        @Schema(description = "当前页码", example = "1")
        long page,
        @Schema(description = "每页条数", example = "10")
        long pageSize
) {

    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }
}
