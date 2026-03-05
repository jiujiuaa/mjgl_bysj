package com.zjb.mjgl.common.enums;

import lombok.Getter;

/**
 * 智能预警规则编码
 */
@Getter
public enum AlertRuleCodeEnum {

    /** 近30天维修（故障）次数 ≥ 3 */
    RECENT_30D_FAULT_GE_3("RECENT_30D_FAULT_GE_3", "近30天故障≥3次", 30, 3, "repair"),
    /** 近30天异常记录次数 ≥ 3 */
    RECENT_30D_ABNORMAL_GE_3("RECENT_30D_ABNORMAL_GE_3", "近30天异常≥3次", 30, 3, "abnormal");

    private final String code;
    private final String description;
    private final int days;
    private final int threshold;
    private final String source;

    AlertRuleCodeEnum(String code, String description, int days, int threshold, String source) {
        this.code = code;
        this.description = description;
        this.days = days;
        this.threshold = threshold;
        this.source = source;
    }
}
