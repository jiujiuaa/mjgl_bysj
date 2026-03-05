package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 报警类型枚举：与 alerts.alert_type 一致（tinyint）
 * 1-故障频发, 2-保养超期, 3-温度异常, 4-润滑异常
 */
@Getter
public enum AlertTypeEnum {

    FREQUENT_FAULT(1, "故障频发"),
    MAINTENANCE_OVERDUE(2, "保养超期"),
    TEMPERATURE_ANOMALY(3, "温度异常"),
    LUBRICATION_ANOMALY(4, "润滑异常");

    private final Integer code;
    private final String description;

    AlertTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AlertTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
