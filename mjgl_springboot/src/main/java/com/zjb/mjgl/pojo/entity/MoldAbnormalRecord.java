package com.zjb.mjgl.pojo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class MoldAbnormalRecord {

    private String id;

    /**
     * 关联模具 ID（molds.id）
     */
    private String moldId;

    /**
     * 异常类型：1=温度异常，2=润滑异常，3=其它
     */
    private Integer abnormalType;

    /**
     * 实际测量值，如 "120℃" 或 "30%, 3.5kPa"
     */
    private String measuredValue;

    /**
     * 阈值或期望范围说明，如 ">110℃"、"30%~70%"
     */
    private String thresholdValue;

    /**
     * 异常描述
     */
    private String description;

    /**
     * 异常发生时间（人工填写）
     */
    private Date occurredAt;

    /**
     * 来源：1=自动监控，2=温度巡检，3=润滑巡检，4=人工录入
     */
    private Integer sourceType;

    /**
     * 操作人 ID（users.id）
     */
    private String operatorId;

    /**
     * 终端/设备 ID（可选）
     */
    private String deviceId;

    /**
     * 关联的维修记录 ID（可选）
     */
    private String linkedRepairId;

    /**
     * 关联的温度/润滑巡检记录 ID
     */
    private String linkedLogId;

    private Date createdAt;

    private Date updatedAt;
}

