package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 报警状态枚举：与 alerts.status 一致（tinyint）
 * 1=活跃, 2=已解决, 3=已忽略
 */
@Getter
public enum AlertStatusEnum {

    ACTIVE(1, "活跃"),
    RESOLVED(2, "已解决"),
    IGNORED(3, "已忽略");

    private final Integer code;
    private final String description;

    AlertStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AlertStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知报警状态: " + code));
    }
}
