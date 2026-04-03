package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.BusinessConfigKeys;
import com.zjb.mjgl.mapper.SystemBusinessConfigMapper;
import com.zjb.mjgl.pojo.dto.BusinessConfigBatchUpdateDTO;
import com.zjb.mjgl.pojo.dto.BusinessConfigUpdateItemDTO;
import com.zjb.mjgl.pojo.entity.SystemBusinessConfig;
import com.zjb.mjgl.pojo.vo.BusinessConfigItemVO;
import com.zjb.mjgl.service.SystemBusinessConfigService;
import com.zjb.mjgl.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemBusinessConfigServiceImpl implements SystemBusinessConfigService {

    private final SystemBusinessConfigMapper systemBusinessConfigMapper;
    private final Environment environment;
    private final com.zjb.mjgl.job.BusinessJobScheduler businessJobScheduler;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Autowired
    public SystemBusinessConfigServiceImpl(SystemBusinessConfigMapper systemBusinessConfigMapper,
                                           Environment environment,
                                           @Lazy com.zjb.mjgl.job.BusinessJobScheduler businessJobScheduler) {
        this.systemBusinessConfigMapper = systemBusinessConfigMapper;
        this.environment = environment;
        this.businessJobScheduler = businessJobScheduler;
    }

    @PostConstruct
    public void bootstrap() {
        ensureDefaults();
        reloadCache();
    }

    @Override
    public void reloadCache() {
        cache.clear();
        Optional.ofNullable(systemBusinessConfigMapper.selectAllOrdered())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(r -> r.getConfigKey() != null)
                .forEach(r -> cache.put(r.getConfigKey(), r.getConfigValue() == null ? "" : r.getConfigValue()));
        log.info("业务配置缓存已刷新, 条数={}", cache.size());
    }

    @Override
    public List<BusinessConfigItemVO> listAllForAdmin() {
        return Optional.ofNullable(systemBusinessConfigMapper.selectAllOrdered())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(r -> new BusinessConfigItemVO(
                        r.getConfigKey(),
                        r.getConfigValue(),
                        r.getLabel(),
                        r.getDescription(),
                        r.getValueType(),
                        r.getSortOrder()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatch(BusinessConfigBatchUpdateDTO dto) {
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("无有效配置项");
        }
        boolean cronTouched = false;
        for (BusinessConfigUpdateItemDTO item : dto.getItems()) {
            if (item == null || !StringUtils.hasText(item.getConfigKey())) {
                continue;
            }
            SystemBusinessConfig existing = systemBusinessConfigMapper.selectByKey(item.getConfigKey());
            if (existing == null) {
                throw new IllegalArgumentException("未知配置项: " + item.getConfigKey());
            }
            String raw = item.getConfigValue() == null ? "" : item.getConfigValue().trim();
            validateValue(existing.getConfigKey(), existing.getValueType(), raw);
            systemBusinessConfigMapper.updateValueByKey(existing.getConfigKey(), raw);
            if (isCronKey(existing.getConfigKey())) {
                cronTouched = true;
            }
        }
        reloadCache();
        if (cronTouched) {
            businessJobScheduler.rescheduleAll();
        }
    }

    private static boolean isCronKey(String key) {
        return BusinessConfigKeys.JOB_ALERT_RULE_CRON.equals(key)
                || BusinessConfigKeys.JOB_HEALTH_REPORT_DAILY_CRON.equals(key)
                || BusinessConfigKeys.JOB_MAINTENANCE_REMINDER_CRON.equals(key);
    }

    private void validateValue(String key, String valueType, String raw) {
        if (!StringUtils.hasText(valueType)) {
            throw new IllegalArgumentException("配置项缺少类型: " + key);
        }
        switch (valueType.toUpperCase()) {
            case "CRON":
                parseCronOrThrow(raw);
                return;
            case "INT":
                if (!StringUtils.hasText(raw)) {
                    throw new IllegalArgumentException("数值不能为空");
                }
                int iv;
                try {
                    iv = Integer.parseInt(raw.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("请输入合法整数");
                }
                validateIntRange(key, iv);
                return;
            case "LONG":
                if (!StringUtils.hasText(raw)) {
                    throw new IllegalArgumentException("数值不能为空");
                }
                long lv;
                try {
                    lv = Long.parseLong(raw.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("请输入合法整数");
                }
                validateLongRange(key, lv);
                return;
            case "STRING":
                if (raw.length() > 4000) {
                    throw new IllegalArgumentException("文本过长（≤4000）");
                }
                return;
            default:
                throw new IllegalArgumentException("未知值类型: " + valueType);
        }
    }

    private static void parseCronOrThrow(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("Cron 不能为空");
        }
        try {
            CronExpression.parse(raw.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Cron 表达式无效: " + e.getMessage());
        }
    }

    private static void validateIntRange(String key, int v) {
        if (BusinessConfigKeys.PAGINATION_DEFAULT_PAGE_SIZE.equals(key)) {
            if (v < 1 || v > 500) {
                throw new IllegalArgumentException("默认每页条数需在 1～500");
            }
            return;
        }
        if (BusinessConfigKeys.ALERT_RULE_DEFAULT_DAYS.equals(key)) {
            if (v < 1 || v > 365) {
                throw new IllegalArgumentException("预警默认统计天数需在 1～365");
            }
            return;
        }
        if (BusinessConfigKeys.ALERT_RULE_DEFAULT_THRESHOLD.equals(key)) {
            if (v < 1 || v > 10000) {
                throw new IllegalArgumentException("预警默认次数阈值需在 1～10000");
            }
            return;
        }
        if (BusinessConfigKeys.ALERT_METRIC_DEFAULT_WINDOW_DAYS.equals(key)) {
            if (v < 1 || v > 365) {
                throw new IllegalArgumentException("温/润滑默认统计窗口天数需在 1～365");
            }
            return;
        }
        if (BusinessConfigKeys.MAINTENANCE_REMINDER_CALENDAR_INTERVAL_DAYS.equals(key)) {
            if (v < 1 || v > 90) {
                throw new IllegalArgumentException("保养日历展示间隔天数需在 1～90");
            }
            return;
        }
        if (BusinessConfigKeys.MAINTENANCE_REMINDER_DAYS_BEFORE_DUE.equals(key)) {
            if (v < 0 || v > 60) {
                throw new IllegalArgumentException("保养提前提醒天数需在 0～60");
            }
            return;
        }
        if (BusinessConfigKeys.MAINTENANCE_REMINDER_REMAINING_USAGE.equals(key)) {
            if (v < 0 || v > 1_000_000) {
                throw new IllegalArgumentException("保养按模次提前阈值需在合理范围");
            }
            return;
        }
        if (BusinessConfigKeys.MINIO_PRESIGN_EXPIRE_DAYS.equals(key)) {
            if (v < 1 || v > 30) {
                throw new IllegalArgumentException("MinIO 预签名有效期（天）建议在 1～30");
            }
            return;
        }
        throw new IllegalArgumentException("未注册校验范围的 INT 配置: " + key);
    }

    private static void validateLongRange(String key, long v) {
        if (BusinessConfigKeys.JWT_EXPIRATION_MS.equals(key)) {
            if (v < 60_000L || v > 365L * 24 * 60 * 60 * 1000) {
                throw new IllegalArgumentException("JWT 过期时间（毫秒）需在 1 分钟～365 天之间");
            }
            return;
        }
        throw new IllegalArgumentException("未注册校验范围的 LONG 配置: " + key);
    }

    @Override
    public String getEffectiveString(String key) {
        String v = cache.get(key);
        if (StringUtils.hasText(v)) {
            return v.trim();
        }
        return envFallback(key);
    }

    @Override
    public int getEffectiveInt(String key) {
        String v = cache.get(key);
        if (StringUtils.hasText(v)) {
            return Integer.parseInt(v.trim());
        }
        return Integer.parseInt(envFallback(key));
    }

    @Override
    public long getEffectiveLong(String key) {
        String v = cache.get(key);
        if (StringUtils.hasText(v)) {
            return Long.parseLong(v.trim());
        }
        return Long.parseLong(envFallback(key));
    }

    @Override
    public String getEffectiveCron(String key) {
        String v = getEffectiveString(key);
        parseCronOrThrow(v);
        return v.trim();
    }

    @Override
    public boolean isWebSocketOriginAllowed(String originHeader) {
        String raw = getEffectiveString(BusinessConfigKeys.WEBSOCKET_ALLOWED_ORIGIN_PATTERNS);
        if (!StringUtils.hasText(raw) || "*".equals(raw.trim())) {
            return true;
        }
        if (!StringUtils.hasText(originHeader)) {
            return true;
        }
        String origin = originHeader.trim();
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> origin.equalsIgnoreCase(pattern) || "*".equals(pattern));
    }

    private String envFallback(String key) {
        switch (key) {
            case BusinessConfigKeys.JOB_ALERT_RULE_CRON:
                return environment.getProperty("app.job.alert-rule.cron", "0 0 4 * * ?");
            case BusinessConfigKeys.JOB_HEALTH_REPORT_DAILY_CRON:
                return environment.getProperty("app.job.health-report.daily-cron", "0 10 0 * * ?");
            case BusinessConfigKeys.JOB_MAINTENANCE_REMINDER_CRON:
                return environment.getProperty("app.job.maintenance-reminder.cron", "0 0 3 * * ?");
            case BusinessConfigKeys.PAGINATION_DEFAULT_PAGE_SIZE:
                return environment.getProperty("app.pagination.default-page-size", "10");
            case BusinessConfigKeys.ALERT_RULE_DEFAULT_DAYS:
                return environment.getProperty("app.alert.rule-default-days", "30");
            case BusinessConfigKeys.ALERT_RULE_DEFAULT_THRESHOLD:
                return environment.getProperty("app.alert.rule-default-threshold", "3");
            case BusinessConfigKeys.ALERT_METRIC_DEFAULT_WINDOW_DAYS:
                return environment.getProperty("app.alert.metric-default-window-days", "7");
            case BusinessConfigKeys.MAINTENANCE_REMINDER_CALENDAR_INTERVAL_DAYS:
                return environment.getProperty("app.maintenance.reminder-calendar-interval-days", "30");
            case BusinessConfigKeys.MAINTENANCE_REMINDER_DAYS_BEFORE_DUE:
                return "5";
            case BusinessConfigKeys.MAINTENANCE_REMINDER_REMAINING_USAGE:
                return "10";
            case BusinessConfigKeys.MINIO_PRESIGN_EXPIRE_DAYS:
                return environment.getProperty("minio.presign-expire-days", "7");
            case BusinessConfigKeys.WEBSOCKET_ALLOWED_ORIGIN_PATTERNS:
                return environment.getProperty("app.websocket.allowed-origin-patterns", "*");
            case BusinessConfigKeys.JWT_EXPIRATION_MS:
                return environment.getProperty("jwt.expiration-ms", "86400000");
            default:
                return "";
        }
    }

    private void ensureDefaults() {
        insertIfAbsent(BusinessConfigKeys.JOB_ALERT_RULE_CRON,
                environment.getProperty("app.job.alert-rule.cron", "0 0 4 * * ?"),
                "智能预警规则引擎 Cron", "每天何时执行智能预警规则（Spring 6 域，例 0 0 4 * * ? 为凌晨 4 点）", "CRON", 10);
        insertIfAbsent(BusinessConfigKeys.JOB_HEALTH_REPORT_DAILY_CRON,
                environment.getProperty("app.job.health-report.daily-cron", "0 10 0 * * ?"),
                "健康报告调度 Cron", "每日检查是否生成周/月/季健康报告的任务触发时间", "CRON", 20);
        insertIfAbsent(BusinessConfigKeys.JOB_MAINTENANCE_REMINDER_CRON,
                environment.getProperty("app.job.maintenance-reminder.cron", "0 0 3 * * ?"),
                "保养提醒扫描 Cron", "扫描保养提醒并推送通知的触发时间", "CRON", 30);
        insertIfAbsent(BusinessConfigKeys.PAGINATION_DEFAULT_PAGE_SIZE,
                environment.getProperty("app.pagination.default-page-size", "10"),
                "默认每页条数", "列表接口未传 pageSize 时的默认值", "INT", 40);
        insertIfAbsent(BusinessConfigKeys.ALERT_RULE_DEFAULT_DAYS,
                environment.getProperty("app.alert.rule-default-days", "30"),
                "预警默认统计天数", "规则未填天数时的默认滑动窗口（天）", "INT", 50);
        insertIfAbsent(BusinessConfigKeys.ALERT_RULE_DEFAULT_THRESHOLD,
                environment.getProperty("app.alert.rule-default-threshold", "3"),
                "预警默认次数阈值", "规则未填次数阈值时的默认值", "INT", 60);
        insertIfAbsent(BusinessConfigKeys.ALERT_METRIC_DEFAULT_WINDOW_DAYS,
                environment.getProperty("app.alert.metric-default-window-days", "7"),
                "温/润滑默认统计窗口（天）", "消息文案等缺省窗口", "INT", 70);
        insertIfAbsent(BusinessConfigKeys.MAINTENANCE_REMINDER_CALENDAR_INTERVAL_DAYS,
                environment.getProperty("app.maintenance.reminder-calendar-interval-days", "30"),
                "保养日历型提醒展示间隔（天）", "按月固定日策略在提醒上的间隔展示近似值", "INT", 80);
        insertIfAbsent(BusinessConfigKeys.MAINTENANCE_REMINDER_DAYS_BEFORE_DUE, "5",
                "保养提前天数（按日期）", "下次保养日期前多少天内开始推送提醒", "INT", 85);
        insertIfAbsent(BusinessConfigKeys.MAINTENANCE_REMINDER_REMAINING_USAGE, "10",
                "保养提前模次阈值（按使用次数）", "剩余模次小于等于该值时推送提醒", "INT", 90);
        insertIfAbsent(BusinessConfigKeys.MINIO_PRESIGN_EXPIRE_DAYS,
                environment.getProperty("minio.presign-expire-days", "7"),
                "MinIO 预签名 URL 有效天数", "文件预览/下载链接有效期", "INT", 100);
        insertIfAbsent(BusinessConfigKeys.WEBSOCKET_ALLOWED_ORIGIN_PATTERNS,
                environment.getProperty("app.websocket.allowed-origin-patterns", "*"),
                "WebSocket 允许的来源", "逗号分隔；填 * 表示不限制（生产建议填具体前端域名）", "STRING", 110);
        insertIfAbsent(BusinessConfigKeys.JWT_EXPIRATION_MS,
                environment.getProperty("jwt.expiration-ms", "86400000"),
                "JWT 过期时间", "新签发 token 的有效期；管理端按「分钟」配置，存储为毫秒", "LONG", 120);
    }

    private void insertIfAbsent(String key, String value, String label, String description, String valueType, int sortOrder) {
        if (systemBusinessConfigMapper.selectByKey(key) != null) {
            return;
        }
        SystemBusinessConfig row = new SystemBusinessConfig();
        row.setId(IdUtil.fastUUID());
        row.setConfigKey(key);
        row.setConfigValue(value);
        row.setLabel(label);
        row.setDescription(description);
        row.setValueType(valueType);
        row.setSortOrder(sortOrder);
        systemBusinessConfigMapper.insert(row);
        log.info("已初始化业务配置项: {}", key);
    }
}
