package com.jaycong.boot.modules.dict.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class DictTypeEntity extends BaseEntity {

    private String typeCode;

    private String typeName;

    private String status;

    private String description;
}

