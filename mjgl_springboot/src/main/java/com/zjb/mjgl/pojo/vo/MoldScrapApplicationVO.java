package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoldScrapApplicationVO {
    private String id;
    private String moldId;
    private String moldCode;
    private String moldName;

    private String reason;
    private Integer status;
    private String statusDesc;

    private String applicantName;
    private String approverName;
    private String handlerName;

    private String approvalComment;
    private String handlerComment;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime handledAt;
}

