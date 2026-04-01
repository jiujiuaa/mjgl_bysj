package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 移动端二维码交互统一请求：
 * - 移动端先扫码拿到 codeId（二维码内容）
 * - 再由“动作选择 UI”选择 scanType（1~9）
 * - 前端将 codeId + scanType + 最小业务字段提交给后端 resolve 接口
 */
@Data
public class MoldQrcodeMobileResolveRequest {

    /** 二维码唯一内容（mold_qrcodes.id） */
    private String codeId;

    /** 扫码动作类型：1~9（见文档） */
    private Integer scanType;

    /** 可选幂等键（前端传同一请求ID可避免重复提交） */
    private String clientRequestId;

    // ========== 公共列表查询（scanType=4/5/7/8 需要）==========
    private Integer pageNum;
    private Integer pageSize;
    private Integer status;
    private String keyword;

    // ========== scanType=2（快速维修）==========
    private String repairReason;
    private String repairDescription;

    // ========== scanType=3（快速异常上报）==========
    private Integer abnormalType;
    private String measuredValue;
    private String thresholdValue;
    private String description;
    private Date occurredAt;

    // ========== scanType=4（维修记录列表）==========
    // 直接复用 pageNum/pageSize/status/keyword/moldId（后端从 codeId 推导）

    // ========== scanType=5（异常记录列表）==========
    private Integer sourceType;
    private Date startTime;
    private Date endTime;

    // ========== scanType=6（快速保养/点检上报）==========
    /**
     * 1=保养（maintenance_logs）
     * 2=温度巡检（temperature_logs）
     * 3=润滑巡检（lubrication_logs）
     */
    private Integer maintenanceOrCheckType;

    private String maintenanceType; // 保养类型
    private String details; // 详细内容（保养/异常等）

    // 温度巡检
    private String sensorLocation;
    private Double temperature;
    private Date operationTime;

    // 润滑巡检
    private Double oilLevelPercent;
    private Double pressureKpa;

    // 附件（本项目文件关联字段多为 file_ids，移动端可先不传）
    /**
     * 建议后端把 attachments list 转为 JSON 字符串或逗号分隔字符串写入 DB。
     * 本期先支持 fileIdsJson（字符串）/attachments（列表）。
     */
    private String fileIdsJson;
    private List<String> attachments;

    // ========== scanType=8（处理保养提醒）==========
    private String maintenanceReminderId;
    private String action; // COMPLETE / IGNORE
    private String maintenanceReminderDetails; // 完成时可选：保养记录 details
    private String maintenanceReminderType; // 完成时可选：maintenance_type

    // ========== scanType=9（二码生命周期管理）==========
    private String operation; // DISABLE / REPLACE
    private String newMoldId;
    private Integer newQrcodeType; // 可选
    private String disabledReason;

    // ========== scanType=6 额外字段（保养记录可选）==========
    private Double cost;
    private Date actualEndTime;

    // ========== scanType=7 额外字段（健康报告）==========
    private String healthReportId;
}

