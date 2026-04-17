package com.jaycong.boot.modules.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class DictItemEntity extends BaseEntity {

    private String typeCode;

    private String itemCode;

    private String itemLabel;

    private String itemValue;

    private Integer sort;

    private String color;

    private String extJson;

    private String status;
}

