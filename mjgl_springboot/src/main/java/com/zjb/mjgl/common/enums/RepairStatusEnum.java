package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 维修状态枚举，对应 repair_records.status 字段：
 * 1=待处理, 2=维修中, 3=已修复, 4=已验收
 */
@Getter
public enum RepairStatusEnum {

    PENDING(1, "待处理"),
    IN_PROGRESS(2, "维修中"),
    FIXED(3, "已修复"),
    ACCEPTED(4, "已验收");

    private final Integer code;
    private final String description;

    RepairStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static RepairStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知维修状态 code: " + code));
    }
}

