package com.zjb.mjgl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldSpecVO {
    private String dimensions;
    private String material;
    private Integer cavityCount;
    private Integer designLifeCycles;
    private Integer designLifeYears;
    private String keyDimensions;
    // 注意：不暴露 id / moldId 给前端（除非需要）
}