package com.zjb.mjgl.pojo.entity;

import lombok.Data;

@Data
public class SystemBusinessConfig {
    private String id;
    private String configKey;
    private String configValue;
    private String label;
    private String description;
    private String valueType;
    private Integer sortOrder;
}
