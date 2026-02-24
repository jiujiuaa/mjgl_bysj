package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模具更新 DTO：更新模具主表 + 技术参数 + 二维码类型
 * 只包含允许修改的业务字段（统计类字段不在此处修改）。
 */
@Data
public class MoldUpdateDTO {

    /**
     * 要更新的模具 ID
     */
    private String id;

    // ===== molds 主表可更新字段 =====
    private String moldCode;
    private String name;
    private String category;
    private String productProject;
    private String location;
    private String manufacturer;
    private String supplier;
    /**
     * 采购/入库日期时间
     */
    private LocalDateTime purchaseDate;
    private Integer currentStatus;
    private Double purchaseCost;

    // ===== mold_specs 技术参数字段 =====
    private String dimensions;
    private String material;
    private Integer cavityCount;
    private Integer designLifeCycles;
    private Integer designLifeYears;
    private String keyDimensions;
    private String linkedDocuments;

    // ===== mold_qrcodes 二维码类型列表（更新时会整体替换） =====
    private List<Integer> qrcodeTypes;
}

