package com.zjb.mjgl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldDetailVO {

    // === 来自 molds 表 ===
    private String id;
    private String moldCode;
    private String name;
    private String category;
    private String productProject;
    private String location;
    private String manufacturer;
    /**
     * 采购/入库日期时间
     */
    private LocalDateTime purchaseDate;
    private Integer currentStatus; // 1=在库, 2=使用中...
    private Integer totalUsageCount;
    private BigDecimal totalProductionTime;
    /**
     * 最后保养日期时间
     */
    private LocalDateTime lastMaintenanceDate;
    private BigDecimal purchaseCost;

    // === 关联数据 ===
    private MoldSpecVO specs;           // 一对一
    private List<FileVO> files;         // 一对多
    private MoldQrcodeVO qrcode;        // 一对一：一个模具对应一个二维码

    // getters / setters
}