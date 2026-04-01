package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoldScrapApplicationDTO {
    private String id;
    private String moldId;
    private String reason;

    // 申请时必填：只需要 reason 与 moldId

    // 审批时使用
    private Integer approvalStatus; // 1=待审批? / 2=已批准 / 3=已拒绝
    private String approvalComment;

    // 执行时使用
    private String handlerComment;

    // 查询/详情展示用（不一定由前端传）
    private LocalDateTime approvedAt;
    private LocalDateTime handledAt;
}

