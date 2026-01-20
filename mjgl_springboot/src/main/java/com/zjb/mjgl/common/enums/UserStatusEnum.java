package com.zjb.mjgl.common.enums;


import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String description;

    UserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    // 根据 code 获取枚举
    public static UserStatusEnum fromCode(Integer code) {
        if (code == null) return DISABLED;
        for (UserStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}