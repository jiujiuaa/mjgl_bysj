package com.zjb.mjgl.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 业务配置中心项主键（与表 system_business_config.config_key 一致）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusinessConfigKeys {

    public static final String JOB_ALERT_RULE_CRON = "job.alert_rule_cron";
    public static final String JOB_HEALTH_REPORT_DAILY_CRON = "job.health_report_daily_cron";
    public static final String JOB_MAINTENANCE_REMINDER_CRON = "job.maintenance_reminder_cron";

    public static final String PAGINATION_DEFAULT_PAGE_SIZE = "pagination.default_page_size";

    public static final String ALERT_RULE_DEFAULT_DAYS = "alert.rule_default_days";
    public static final String ALERT_RULE_DEFAULT_THRESHOLD = "alert.rule_default_threshold";
    public static final String ALERT_METRIC_DEFAULT_WINDOW_DAYS = "alert.metric_default_window_days";

    public static final String MAINTENANCE_REMINDER_CALENDAR_INTERVAL_DAYS = "maintenance.reminder_calendar_interval_days";
    public static final String MAINTENANCE_REMINDER_DAYS_BEFORE_DUE = "maintenance.reminder_days_before_due";
    public static final String MAINTENANCE_REMINDER_REMAINING_USAGE = "maintenance.reminder_remaining_usage_threshold";

    public static final String MINIO_PRESIGN_EXPIRE_DAYS = "minio.presign_expire_days";
    public static final String WEBSOCKET_ALLOWED_ORIGIN_PATTERNS = "websocket.allowed_origin_patterns";

    public static final String JWT_EXPIRATION_MS = "jwt.expiration_ms";
}
