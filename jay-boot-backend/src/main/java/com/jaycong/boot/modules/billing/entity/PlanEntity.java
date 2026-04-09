package com.jaycong.boot.modules.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 套餐实体，映射 plans 表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plans")
public class PlanEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private String billingCycle;

    private String quotaJson;

    private Long price;

    private String status;
}
