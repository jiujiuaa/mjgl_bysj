package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MoldScrapStatusEnum {
    /**
     * 1 = 待审批
     * 2 = 已批准
     * 3 = 已拒绝
     * 4 = 已执行（报废完成）
     */
    PENDING_APPROVAL(1, "待审批"),
    APPROVED(2, "已批准"),
    REJECTED(3, "已拒绝"),
    EXECUTED(4, "已执行");

    private final Integer code;
    private final String description;

    MoldScrapStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MoldScrapStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知报废状态 code: " + code));
    }
}

