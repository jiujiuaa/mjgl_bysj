package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 模具当前状态枚举.
 * <p>
 * 与 {@link com.zjb.mjgl.pojo.entity.Molds#currentStatus} 字段对应：
 * 1=在库, 2=使用中, 3=维修中, 4=外借, 5=待报废
 */
@Getter
public enum MoldStatusEnum {

    /** 1 = 在库 */
    IN_STOCK(1, "在库"),

    /** 2 = 使用中 */
    IN_USE(2, "使用中"),

    /** 3 = 维修中 */
    UNDER_MAINTENANCE(3, "维修中"),

    /** 4 = 外借 */
    LENT_OUT(4, "外借"),

    /** 5 = 待报废 */
    TO_BE_SCRAPPED(5, "待报废");

    private final Integer code;
    private final String description;

    MoldStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据数值 code 反查枚举.
     */
    public static MoldStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知模具状态 code: " + code));
    }
}

