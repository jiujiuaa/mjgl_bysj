package com.zjb.mjgl.common.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 保养提醒状态枚举（一模具+一计划对应一条记录）
 * <p>
 * 流转说明：
 * <ul>
 *   <li>待处理(1) → 已提醒(2)：定时任务或用户点击「发送提醒」</li>
 *   <li>已提醒(2) → 待处理(1)：用户录入保养记录，系统在同一条记录上滚动下一周期并重置状态</li>
 *   <li>待处理(1)/已提醒(2) → 已忽略(4)：用户点击「忽略」，定时任务不再处理</li>
 * </ul>
 * 状态 3（已完成）在当前单条记录设计下不写入，保留用于兼容历史数据或扩展。
 */
@Getter
public enum MaintenanceReminderStatusEnum {

    PENDING(1, "待处理"),
    REMINDED(2, "已提醒"),
    COMPLETED(3, "已完成"),
    IGNORED(4, "已忽略");

    private final Integer code;
    private final String description;

    MaintenanceReminderStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MaintenanceReminderStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
