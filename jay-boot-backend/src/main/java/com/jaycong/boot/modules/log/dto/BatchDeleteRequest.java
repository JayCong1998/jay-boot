package com.jaycong.boot.modules.log.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class BatchDeleteRequest {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
