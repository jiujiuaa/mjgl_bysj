package com.zjb.mjgl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 业务定时任务专用调度器（与配置中心中的 Cron 联动）。
 */
@Configuration
public class BusinessTaskSchedulerConfig {

    @Bean(name = "businessTaskScheduler")
    public ThreadPoolTaskScheduler businessTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("biz-cron-");
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }
}
