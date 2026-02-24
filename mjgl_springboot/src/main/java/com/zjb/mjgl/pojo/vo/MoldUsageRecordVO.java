package com.zjb.mjgl.pojo.vo;

// MoldUsageRecordVO.java
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MoldUsageRecordVO {
    private String id;
    private String moldCode;
    private String moldName;
    private String moldCategory;

    private Integer usageType;
    private String usageTypeDesc;

    // 预计时间
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;

    // 实际时间
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    // 计算字段（非数据库）
    private Long plannedDurationHours;   // 计划时长
    private Long actualDurationHours;    // 实际时长
    private Long startTimeDeviationHours; // 开始时间偏差（正=延迟，负=提前）

    private String applicantName;
    private String borrowerName;
    private String borrowerCompany;
    private String purpose;

    private Integer status;
    private String statusDesc;

    private Boolean inspectionPassed;
    private String returnRemarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}