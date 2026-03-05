package com.zjb.mjgl.pojo.vo;

import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RepairRecordVO extends RepairRecordDTO {

    // === 模具基础信息（来自 molds 表，便于列表直接展示） ===
    private String moldCode;
    private String moldName;
    private String moldCategory;

    // === 人员姓名冗余字段（来自 users 表） ===
    private String reporterName;
    private String maintainerName;
    private String verifierName;

    // === 状态描述（由后端翻译数值状态） ===
    private String statusDesc;

    // === 记录创建时间 ===
    private LocalDateTime createdAt;
}

