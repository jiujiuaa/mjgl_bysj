package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模具创建 DTO：一次性提交模具主表 + 技术参数 + 二维码类型
 */
@Data
public class MoldCreateDTO {

    // ===== molds 主表字段 =====
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
    /**
     * 当前状态：1=在库, 2=使用中, 3=维修中, 4=外借, 5=待报废
     */
    private Integer currentStatus;
    /**
     * 采购成本（元）
     */
    private Double purchaseCost;
    /**
     * 创建人 ID（前端可传当前登录用户 ID）
     */
    private String createdBy;

    // ===== mold_specs 技术参数字段 =====
    private String dimensions;
    private String material;
    private Integer cavityCount;
    private Integer designLifeCycles;
    private Integer designLifeYears;
    private String keyDimensions;
    private String linkedDocuments;

    // ===== mold_qrcodes 二维码类型列表，如 [1,2] 表示生成“详情+报修”二维码 =====
    private List<Integer> qrcodeTypes;
}

