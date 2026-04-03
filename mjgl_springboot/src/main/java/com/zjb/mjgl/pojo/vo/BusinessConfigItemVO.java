package com.zjb.mjgl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessConfigItemVO {
    private String configKey;
    private String configValue;
    private String label;
    private String description;
    private String valueType;
    private Integer sortOrder;
}
