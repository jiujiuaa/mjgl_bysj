package com.zjb.mjgl.common.enums;

import lombok.Getter;

/**
 * 业务类型枚举（与 files 表 biz_type 等通用关联字段对应）。
 * 依据 dump 中 files 当前关联 molds，以及后续扩展（订单、项目等）定义。
 */
@Getter
public enum BizTypeEnum {

    /** 模具（对应 molds 表，files 表当前 mold_id 所关联） */
    MOLD("mold", "模具"),

    /** 订单（预留） */
    ORDER("order", "订单"),

    /** 项目（预留） */
    PROJECT("project", "项目");

    private final String code;
    private final String description;

    BizTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据数据库存储的 code 反查枚举
     */
    public static BizTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BizTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知业务类型: " + code);
    }
}
