package com.zjb.mjgl.pojo.dto;

// MoldUsageRecordDTO.java
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MoldUsageRecordDTO {
    private String id;

    // 创建时必填
    private String moldId;
    private Integer usageType;

    /**
     * 申请人ID（当前登录用户ID）
     */
    private String applicantId;

    /**
     * 申请人姓名（或登录名），直接冗余在记录表中，便于列表展示
     */
    private String applicantName;

    // 预计时间（申请时可选填）
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;

    // 实际时间（通常由后端自动设，但 DTO 保留以支持补录）
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    // 外借信息
    private String borrowerName;
    private String borrowerCompany;

    private String purpose;

    // 归还信息
    private Boolean inspectionPassed;
    private String returnRemarks;

    private String action; // "CREATE", "START", "RETURN", "CANCEL"
}