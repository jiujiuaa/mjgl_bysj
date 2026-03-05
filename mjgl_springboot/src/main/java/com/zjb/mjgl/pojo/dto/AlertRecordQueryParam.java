package com.zjb.mjgl.pojo.dto;

import lombok.Data;

/**
 * 报警记录查询参数（与 alerts 表字段对应）
 */
@Data
public class AlertRecordQueryParam {

    private String moldId;
    /** 状态: 1=活跃, 2=已解决, 3=已忽略 */
    private Integer status;
    /** 报警类型: 1-故障频发, 2-保养超期, 3-温度异常 */
    private Integer alertType;
    private String keyword;  // 模具编号/名称模糊
}
