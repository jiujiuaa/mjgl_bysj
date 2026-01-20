package com.zjb.mjgl.pojo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 用户查询条件 DTO
 */
@Data
public class UserQueryDTO {
    
    /**
     * 真实姓名（模糊查询）
     */
    private String realName;
    
    /**
     * 角色（ADMIN / INSPECTOR / USER）
     */
    private String role;
    
    /**
     * 状态（0=禁用，1=启用）
     */
    private Integer enabled;
    
    /**
     * 创建日期开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    
    /**
     * 创建日期结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
}
