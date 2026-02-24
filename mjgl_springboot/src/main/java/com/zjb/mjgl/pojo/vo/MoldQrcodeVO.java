package com.zjb.mjgl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoldQrcodeVO {
    private String id;            // 二维码内容（扫码后传给后端）
    private Integer qrcodeType;   // 1=详情, 2=报修...
    private Boolean isActive;
    private LocalDateTime createdAt;


}