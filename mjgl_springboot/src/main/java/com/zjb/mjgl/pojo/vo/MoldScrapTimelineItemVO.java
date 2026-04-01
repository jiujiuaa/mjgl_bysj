package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoldScrapTimelineItemVO {
    private String eventType; // SUBMITTED / APPROVED / REJECTED / EXECUTED
    private String eventTypeDesc;
    private String operatorName; // 申请人/审批人/处理人
    private LocalDateTime eventTime;
    private String comment;
}

