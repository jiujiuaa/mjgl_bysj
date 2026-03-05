package com.zjb.mjgl.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
public class RepairQueryParam {


    /**
     * 模具ID，精确筛选该模具的维修记录
     */
    private String moldId;

    /**
     * 模具名称搜索
     */
    private String keyword;

    /**
     * 维修开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 维修结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 送修人ID，可为空
     */
    private String reporterId;

    /**
     * 维修人ID
     */
    private String maintainerId;

    /**
     * 验证人ID
     */
    private String verifierId;


    private Integer status;
}
