package com.zjb.mjgl.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ADMIN("ADMIN", "管理员"),
    INSPECTOR("INSPECTOR", "巡查员"),
    PRODUCTION("PRODUCTION", "生产人员"),
    MAINTENANCE("MAINTENANCE", "保养人员"),
    OPERATOR("OPERATOR", "操作员"),
    USER("USER", "普通用户");

    private final String value;      // 存入数据库的英文值
    private final String description; // 前端展示用的中文

    RoleEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    // 根据数据库存储的英文值反查枚举（用于反序列化）
    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}