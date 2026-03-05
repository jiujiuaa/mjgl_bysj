package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.enums.AlertStatusEnum;
import com.zjb.mjgl.common.enums.AlertTypeEnum;
import com.zjb.mjgl.mapper.AlertRecordMapper;
import com.zjb.mjgl.mapper.LubricationLogMapper;
import com.zjb.mjgl.mapper.MoldAbnormalRecordMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.mapper.RepairRecordMapper;
import com.zjb.mjgl.mapper.TemperatureLogMapper;
import com.zjb.mjgl.pojo.entity.AlertRecord;
import com.zjb.mjgl.pojo.entity.AlertRule;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.AlertRuleEngineService;
import com.zjb.mjgl.service.AlertRuleService;
import com.zjb.mjgl.utils.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 智能预警规则引擎实现。从数据库读取已启用的规则，满足条件则写入 alerts 表并推送 WebSocket。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleEngineServiceImpl implements AlertRuleEngineService {

    private final RepairRecordMapper repairRecordMapper;
    private final MoldAbnormalRecordMapper moldAbnormalRecordMapper;
    private final TemperatureLogMapper temperatureLogMapper;
    private final LubricationLogMapper lubricationLogMapper;
    private final MoldsMapper moldsMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final AlertMessageService alertMessageService;
    private final AlertRuleService alertRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runAllRules() {
        List<AlertRule> rules = alertRuleService.listEnabledForEngine();
        if (rules.isEmpty()) {
            log.info("未配置启用的预警规则，跳过执行");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        rules.forEach(rule -> {
            try {
                runRule(rule, now);
            } catch (Exception e) {
                log.warn("规则执行异常 rule={}", rule.getCode(), e);
            }
        });
    }

    private void runRule(AlertRule rule, LocalDateTime now) {
        String source = rule.getSource();
        if ("repair".equals(source)) {
            int days = Optional.ofNullable(rule.getDays()).orElse(30);
            int threshold = Optional.ofNullable(rule.getThreshold()).orElse(3);
            LocalDateTime since = now.minusDays(days);
            List<String> moldIds = repairRecordMapper.listMoldIdsWithRepairCountGeSince(since, threshold);
            moldIds.forEach(moldId -> ensureAlertForMold(rule, moldId, now));
        } else if ("abnormal".equals(source)) {
            int days = Optional.ofNullable(rule.getDays()).orElse(30);
            int threshold = Optional.ofNullable(rule.getThreshold()).orElse(3);
            Date since = Date.from(now.minusDays(days).atZone(ZoneId.systemDefault()).toInstant());
            List<String> moldIds = moldAbnormalRecordMapper.listMoldIdsWithAbnormalCountGeSince(since, threshold);
            moldIds.forEach(moldId -> ensureAlertForMold(rule, moldId, now));
        } else if ("temperature".equals(source)) {
            Integer days = rule.getTimeWindowMinutes();
            BigDecimal val = rule.getValueThreshold();
            String compareOp = rule.getCompareOp();
            boolean byCount = "count".equals(rule.getTriggerMode());
            if (days == null || days < 1 || val == null || compareOp == null) {
                return;
            }
            if (!byCount && rule.getMetricType() == null) return;
            if (byCount && (rule.getThreshold() == null || rule.getThreshold() < 1)) return;
            Date since = Date.from(now.minusDays(days).atZone(ZoneId.systemDefault()).toInstant());
            List<String> moldIds = byCount
                    ? temperatureLogMapper.listMoldIdsWithTemperatureMeetCountGeSince(since, compareOp, val.doubleValue(), rule.getThreshold())
                    : temperatureLogMapper.listMoldIdsWithTemperatureMeetCondition(since, rule.getMetricType(), compareOp, val.doubleValue());
            moldIds.forEach(moldId -> ensureAlertForMold(rule, moldId, now));
        } else if ("lubrication".equals(source)) {
            Integer days = rule.getTimeWindowMinutes();
            BigDecimal val = rule.getValueThreshold();
            String compareOp = rule.getCompareOp();
            String metricField = rule.getMetricField();
            boolean byCount = "count".equals(rule.getTriggerMode());
            if (days == null || days < 1 || val == null || compareOp == null || metricField == null) {
                return;
            }
            if (!byCount && rule.getMetricType() == null) return;
            if (byCount && (rule.getThreshold() == null || rule.getThreshold() < 1)) return;
            Date since = Date.from(now.minusDays(days).atZone(ZoneId.systemDefault()).toInstant());
            List<String> moldIds = byCount
                    ? lubricationLogMapper.listMoldIdsWithLubricationMeetCountGeSince(since, metricField, compareOp, val.doubleValue(), rule.getThreshold())
                    : lubricationLogMapper.listMoldIdsWithLubricationMeetCondition(since, metricField, rule.getMetricType(), compareOp, val.doubleValue());
            moldIds.forEach(moldId -> ensureAlertForMold(rule, moldId, now));
        }
    }

    private void ensureAlertForMold(AlertRule rule, String moldId, LocalDateTime now) {
        String source = rule.getSource();
        Integer alertType;
        String message;
        String triggerCondition = Optional.ofNullable(rule.getDescription()).orElse(rule.getName());
        Molds mold = Optional.ofNullable(moldsMapper.selectById(moldId)).orElse(new Molds());
        String moldName = Optional.ofNullable(mold.getName()).orElse(moldId);
        String moldCode = Optional.ofNullable(mold.getMoldCode()).orElse("");

        if ("repair".equals(source)) {
            alertType = AlertTypeEnum.FREQUENT_FAULT.getCode();
            int days = Optional.ofNullable(rule.getDays()).orElse(30);
            int threshold = Optional.ofNullable(rule.getThreshold()).orElse(3);
            message = String.format("模具 %s（%s）在近%d天内维修/故障达到%d次，请关注。", moldName, moldCode, days, threshold);
        } else if ("abnormal".equals(source)) {
            alertType = AlertTypeEnum.FREQUENT_FAULT.getCode();
            int days = Optional.ofNullable(rule.getDays()).orElse(30);
            int threshold = Optional.ofNullable(rule.getThreshold()).orElse(3);
            message = String.format("模具 %s（%s）在近%d天内异常记录达到%d次，请关注。", moldName, moldCode, days, threshold);
        } else if ("temperature".equals(source)) {
            alertType = AlertTypeEnum.TEMPERATURE_ANOMALY.getCode();
            int days = Optional.ofNullable(rule.getTimeWindowMinutes()).orElse(7);
            String opDesc = compareOpDesc(rule.getCompareOp());
            if ("count".equals(rule.getTriggerMode())) {
                int k = Optional.ofNullable(rule.getThreshold()).orElse(1);
                message = String.format("模具 %s（%s）在近%d天内有%d次温度%s阈值 %s℃，请关注。",
                        moldName, moldCode, days, k, opDesc, rule.getValueThreshold());
            } else {
                message = String.format("模具 %s（%s）在近%d天内温度%s阈值 %s℃，请关注。",
                        moldName, moldCode, days, opDesc, rule.getValueThreshold());
            }
        } else if ("lubrication".equals(source)) {
            alertType = AlertTypeEnum.LUBRICATION_ANOMALY.getCode();
            int days = Optional.ofNullable(rule.getTimeWindowMinutes()).orElse(7);
            String fieldDesc = "oil_level_percent".equals(rule.getMetricField()) ? "液位" : "压力";
            String opDesc = compareOpDesc(rule.getCompareOp());
            if ("count".equals(rule.getTriggerMode())) {
                int k = Optional.ofNullable(rule.getThreshold()).orElse(1);
                message = String.format("模具 %s（%s）在近%d天内有%d次润滑%s%s阈值 %s，请关注。",
                        moldName, moldCode, days, k, fieldDesc, opDesc, rule.getValueThreshold());
            } else {
                message = String.format("模具 %s（%s）在近%d天内润滑%s%s阈值 %s，请关注。",
                        moldName, moldCode, days, fieldDesc, opDesc, rule.getValueThreshold());
            }
        } else {
            return;
        }

        List<AlertRecord> existing = alertRecordMapper.selectByMoldIdAndAlertTypeAndTriggerConditionAndStatus(
                moldId, alertType, triggerCondition, AlertStatusEnum.ACTIVE.getCode());
        if (!existing.isEmpty()) {
            return;
        }

        AlertRecord record = new AlertRecord();
        record.setId(IdUtil.fastUUID());
        record.setMoldId(moldId);
        record.setAlertType(alertType);
        record.setTriggerCondition(triggerCondition);
        record.setSeverity(2);
        record.setMessage(message);
        record.setStatus(AlertStatusEnum.ACTIVE.getCode());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        alertRecordMapper.insert(record);

        MoldAlertMessage msg = new MoldAlertMessage();
        msg.setTitle(triggerCondition);
        msg.setContent(message);
        msg.setType("WARNING");
        msg.setBiz_type("RULE_ALERT");
        msg.setTime(now);
        alertMessageService.broadcastAlert(msg);
        log.info("智能预警已创建 alertId={} moldId={} rule={}", record.getId(), moldId, rule.getCode());
    }

    private static String compareOpDesc(String compareOp) {
        if (compareOp == null) return "";
        switch (compareOp) {
            case "gt": return "超过";
            case "ge": return "达到或超过";
            case "lt": return "低于";
            case "le": return "达到或低于";
            default: return "";
        }
    }
}
