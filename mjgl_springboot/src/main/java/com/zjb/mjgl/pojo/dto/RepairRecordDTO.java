package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepairRecordDTO {

    /**
     * 维修记录ID（更新/详情时使用）
     */
    private String id;

    /**
     * 模具ID，对应 molds.id
     */
    private String moldId;

    /**
     * 送修人ID，可为空
     */
    private String reporterId;

    /**
     * 维修人ID
     */
    private String maintainerId;

    /**
     * 验证人ID
     */
    private String verifierId;

    /**
     * 故障原因
     */
    private String repairReason;

    /**
     * 维修内容描述
     */
    private String repairDescription;

    /**
     * 维修开始时间
     */
    private LocalDateTime startTime;

    /**
     * 维修结束时间
     */
    private LocalDateTime endTime;

    /**
     * 维修状态：1=待处理, 2=维修中, 3=已修复, 4=已验收
     */
    private Integer status;

    /**
     * 维修费用（元）
     */
    private BigDecimal cost;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 合理性审批状态：0=未审核, 1=合理, 2=存在问题
     */
    private Integer repairApprovalStatus;

    /**
     * 合理性审批意见
     */
    private String repairApprovalComment;

    /**
     * 合理性审批人ID
     */
    private String repairApproverId;

    /**
     * 合理性审批时间
     */
    private LocalDateTime repairApprovalTime;
}

