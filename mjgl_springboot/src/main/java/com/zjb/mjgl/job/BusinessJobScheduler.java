package com.zjb.mjgl.job;

import com.zjb.mjgl.common.BusinessConfigKeys;
import com.zjb.mjgl.service.SystemBusinessConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;

/**
 * 根据配置中心 Cron 动态注册业务定时任务。
 */
@Slf4j
@Component
public class BusinessJobScheduler {

    private final ThreadPoolTaskScheduler scheduler;
    private final SystemBusinessConfigService businessConfigService;
    private final AlertRuleEngineJob alertRuleEngineJob;
    private final HealthReportJob healthReportJob;
    private final MaintenanceReminderJob maintenanceReminderJob;

    private volatile ScheduledFuture<?> alertFuture;
    private volatile ScheduledFuture<?> healthFuture;
    private volatile ScheduledFuture<?> maintenanceFuture;

    public BusinessJobScheduler(
            @Qualifier("businessTaskScheduler") ThreadPoolTaskScheduler scheduler,
            SystemBusinessConfigService businessConfigService,
            AlertRuleEngineJob alertRuleEngineJob,
            HealthReportJob healthReportJob,
            MaintenanceReminderJob maintenanceReminderJob) {
        this.scheduler = scheduler;
        this.businessConfigService = businessConfigService;
        this.alertRuleEngineJob = alertRuleEngineJob;
        this.healthReportJob = healthReportJob;
        this.maintenanceReminderJob = maintenanceReminderJob;
    }

    @PostConstruct
    public void init() {
        rescheduleAll();
    }

    public synchronized void rescheduleAll() {
        cancelQuietly(alertFuture);
        cancelQuietly(healthFuture);
        cancelQuietly(maintenanceFuture);
        try {
            String c1 = businessConfigService.getEffectiveCron(BusinessConfigKeys.JOB_ALERT_RULE_CRON);
            alertFuture = scheduler.schedule(alertRuleEngineJob::runRules, new CronTrigger(c1));
            log.info("已调度智能预警任务, cron={}", c1);
        } catch (Exception e) {
            log.error("调度智能预警任务失败", e);
        }
        try {
            String c2 = businessConfigService.getEffectiveCron(BusinessConfigKeys.JOB_HEALTH_REPORT_DAILY_CRON);
            healthFuture = scheduler.schedule(healthReportJob::runDaily, new CronTrigger(c2));
            log.info("已调度健康报告任务, cron={}", c2);
        } catch (Exception e) {
            log.error("调度健康报告任务失败", e);
        }
        try {
            String c3 = businessConfigService.getEffectiveCron(BusinessConfigKeys.JOB_MAINTENANCE_REMINDER_CRON);
            maintenanceFuture = scheduler.schedule(maintenanceReminderJob::checkReminders, new CronTrigger(c3));
            log.info("已调度保养提醒任务, cron={}", c3);
        } catch (Exception e) {
            log.error("调度保养提醒任务失败", e);
        }
    }

    private static void cancelQuietly(ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }
}
