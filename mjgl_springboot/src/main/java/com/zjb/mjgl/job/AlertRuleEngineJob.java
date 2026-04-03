package com.zjb.mjgl.job;

import com.zjb.mjgl.service.AlertRuleEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 智能预警由定时任务触发。
 * 按配置的 cron 表达式（默认每天 04:00）执行规则引擎，对满足条件的模具生成报警并写入 alerts 表、推送 WebSocket。
 * 规则示例：近30天维修≥3次、近30天异常≥3次。
 * 执行频率在「业务配置中心」或 app.job.alert-rule.cron 中配置，由 {@link BusinessJobScheduler} 注册。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertRuleEngineJob {

    private final AlertRuleEngineService alertRuleEngineService;

    /**
     * 定时触发智能预警规则评估（由 {@link BusinessJobScheduler} 按配置 Cron 调用）
     */
    public void runRules() {
        log.info("开始执行智能预警规则引擎（定时任务）");
        try {
            alertRuleEngineService.runAllRules();
        } catch (Exception e) {
            log.error("智能预警规则引擎执行异常", e);
        }
        log.info("智能预警规则引擎执行结束");
    }
}
