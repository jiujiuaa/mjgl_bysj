package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BusinessConfigBatchUpdateDTO {
    private List<BusinessConfigUpdateItemDTO> items;
}
