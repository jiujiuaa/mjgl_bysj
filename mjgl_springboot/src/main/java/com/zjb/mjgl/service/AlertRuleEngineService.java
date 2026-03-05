package com.zjb.mjgl.service;

/**
 * 智能预警规则引擎：基于规则（如近30天故障≥3次）评估并生成/更新报警记录
 */
public interface AlertRuleEngineService {

    /**
     * 执行全部已配置规则，对满足条件的模具创建或更新报警（仅 ACTIVE 状态会推送通知）
     */
    void runAllRules();
}
