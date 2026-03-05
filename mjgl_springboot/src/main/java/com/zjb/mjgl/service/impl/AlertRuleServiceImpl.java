package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.mapper.AlertRuleMapper;
import com.zjb.mjgl.pojo.dto.AlertRuleSaveParam;
import com.zjb.mjgl.pojo.entity.AlertRule;
import com.zjb.mjgl.pojo.vo.AlertRuleVO;
import com.zjb.mjgl.service.AlertRuleService;
import com.zjb.mjgl.utils.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AlertRuleServiceImpl implements AlertRuleService {

    private final AlertRuleMapper alertRuleMapper;

    @Override
    public List<AlertRuleVO> listAll() {
        return alertRuleMapper.selectAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public AlertRuleVO getById(String id) {
        return Optional.ofNullable(alertRuleMapper.selectById(id))
                .map(this::toVO)
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(AlertRuleSaveParam param) {
        validateBySource(param);
        LocalDateTime now = LocalDateTime.now();
        if (param.getId() != null && !param.getId().isEmpty()) {
            AlertRule existing = alertRuleMapper.selectById(param.getId());
            if (existing == null) {
                throw new IllegalArgumentException("规则不存在: " + param.getId());
            }
            checkCodeUnique(param.getCode(), param.getId());
            AlertRule entity = new AlertRule();
            BeanUtils.copyProperties(param, entity);
            fillDaysThresholdForNonCountSource(entity.getSource(), entity);
            entity.setUpdatedAt(now);
            alertRuleMapper.updateById(entity);
            return entity.getId();
        }
        if (alertRuleMapper.selectByCode(param.getCode()) != null) {
            throw new IllegalArgumentException("规则编码已存在: " + param.getCode());
        }
        AlertRule entity = new AlertRule();
        BeanUtils.copyProperties(param, entity);
        fillDaysThresholdForNonCountSource(entity.getSource(), entity);
        entity.setId(IdUtil.fastUUID());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        if (entity.getDescription() == null || entity.getDescription().isEmpty()) {
            entity.setDescription(entity.getName());
        }
        alertRuleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        if (alertRuleMapper.selectById(id) == null) {
            throw new IllegalArgumentException("规则不存在: " + id);
        }
        alertRuleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setEnabled(String id, Integer enabled) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule == null) {
            throw new IllegalArgumentException("规则不存在: " + id);
        }
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        alertRuleMapper.updateById(rule);
    }

    @Override
    public List<AlertRule> listEnabledForEngine() {
        return alertRuleMapper.selectEnabledOrderBySortOrder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initDefaults() {
        int added = 0;
        if (alertRuleMapper.selectByCode("RECENT_30D_FAULT_GE_3") == null) {
            insertDefaultRule("RECENT_30D_FAULT_GE_3", "近30天故障≥3次", "repair", 30, 3, 10);
            added++;
        }
        if (alertRuleMapper.selectByCode("RECENT_30D_ABNORMAL_GE_3") == null) {
            insertDefaultRule("RECENT_30D_ABNORMAL_GE_3", "近30天异常≥3次", "abnormal", 30, 3, 20);
            added++;
        }
        return added;
    }

    private void validateBySource(AlertRuleSaveParam param) {
        String source = param.getSource();
        if ("repair".equals(source) || "abnormal".equals(source)) {
            if (param.getDays() == null || param.getDays() < 1 || param.getDays() > 365) {
                throw new IllegalArgumentException("统计天数需在 1～365 之间");
            }
            if (param.getThreshold() == null || param.getThreshold() < 1 || param.getThreshold() > 1000) {
                throw new IllegalArgumentException("次数阈值需在 1～1000 之间");
            }
            return;
        }
        if ("temperature".equals(source)) {
            if (param.getTimeWindowMinutes() == null || param.getTimeWindowMinutes() < 1 || param.getTimeWindowMinutes() > 365) {
                throw new IllegalArgumentException("请填写统计天数(1～365)");
            }
            if (param.getValueThreshold() == null) {
                throw new IllegalArgumentException("请填写温度阈值");
            }
            if (param.getCompareOp() == null || param.getCompareOp().isEmpty()) {
                throw new IllegalArgumentException("请选择比较方式");
            }
            boolean byCount = "count".equals(param.getTriggerMode());
            if (byCount) {
                if (param.getThreshold() == null || param.getThreshold() < 1 || param.getThreshold() > 10000) {
                    throw new IllegalArgumentException("按次数触发时，次数阈值需在 1～10000 之间");
                }
            } else {
                if (param.getMetricType() == null || (!"max".equals(param.getMetricType()) && !"avg".equals(param.getMetricType()))) {
                    throw new IllegalArgumentException("按聚合值触发时，请选择聚合方式：最大值或平均值");
                }
            }
            return;
        }
        if ("lubrication".equals(source)) {
            if (param.getTimeWindowMinutes() == null || param.getTimeWindowMinutes() < 1 || param.getTimeWindowMinutes() > 365) {
                throw new IllegalArgumentException("请填写统计天数(1～365)");
            }
            if (param.getValueThreshold() == null) {
                throw new IllegalArgumentException("请填写数值阈值");
            }
            if (param.getCompareOp() == null || param.getCompareOp().isEmpty()) {
                throw new IllegalArgumentException("请选择比较方式");
            }
            if (param.getMetricField() == null || (!"oil_level_percent".equals(param.getMetricField()) && !"pressure_kpa".equals(param.getMetricField()))) {
                throw new IllegalArgumentException("润滑指标仅支持：液位百分比、压力");
            }
            boolean byCount = "count".equals(param.getTriggerMode());
            if (byCount) {
                if (param.getThreshold() == null || param.getThreshold() < 1 || param.getThreshold() > 10000) {
                    throw new IllegalArgumentException("按次数触发时，次数阈值需在 1～10000 之间");
                }
            } else {
                if (param.getMetricType() == null || (!"max".equals(param.getMetricType()) && !"min".equals(param.getMetricType()) && !"avg".equals(param.getMetricType()))) {
                    throw new IllegalArgumentException("按聚合值触发时，请选择聚合方式：最大值、最小值或平均值");
                }
            }
            return;
        }
        throw new IllegalArgumentException("不支持的数据来源: " + source);
    }

    /** 温度/润滑规则：按聚合值时 days/threshold 填占位 0；按次数时 threshold 由前端传入，days 填 0 */
    private void fillDaysThresholdForNonCountSource(String source, AlertRule entity) {
        if ("temperature".equals(source) || "lubrication".equals(source)) {
            if (entity.getDays() == null) entity.setDays(0);
            if (!"count".equals(entity.getTriggerMode()) && entity.getThreshold() == null) {
                entity.setThreshold(0);
            }
            if (entity.getTriggerMode() == null) entity.setTriggerMode("value");
        }
    }

    private void checkCodeUnique(String code, String excludeId) {
        AlertRule byCode = alertRuleMapper.selectByCode(code);
        if (byCode != null && !byCode.getId().equals(excludeId)) {
            throw new IllegalArgumentException("规则编码已存在: " + code);
        }
    }

    private void insertDefaultRule(String code, String name, String source, int days, int threshold, int sortOrder) {
        LocalDateTime now = LocalDateTime.now();
        AlertRule r = new AlertRule();
        r.setId(IdUtil.fastUUID());
        r.setCode(code);
        r.setName(name);
        r.setDescription(name);
        r.setSource(source);
        r.setDays(days);
        r.setThreshold(threshold);
        r.setEnabled(1);
        r.setSortOrder(sortOrder);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        alertRuleMapper.insert(r);
    }

    private AlertRuleVO toVO(AlertRule e) {
        AlertRuleVO vo = new AlertRuleVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }
}
