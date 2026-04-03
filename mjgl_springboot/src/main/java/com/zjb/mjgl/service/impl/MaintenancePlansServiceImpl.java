package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.mapper.MaintenancePlanMapper;
import com.zjb.mjgl.mapper.MaintenanceReminderMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MaintenancePlanQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.MaintenancePlanWithMoldVO;
import com.zjb.mjgl.common.BusinessConfigKeys;
import com.zjb.mjgl.service.MaintenancePlansService;
import com.zjb.mjgl.service.SystemBusinessConfigService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MaintenancePlansServiceImpl implements MaintenancePlansService {

    @Autowired
    private SystemBusinessConfigService systemBusinessConfigService;

    @Autowired
    private MaintenancePlanMapper maintenancePlanMapper;
    @Autowired
    private MaintenanceReminderMapper maintenanceReminderMapper;
    @Autowired
    private MoldsMapper moldsMapper;
    @Override
    public Result<?> insert(MaintenancePlans maintenancePlans) {
        if(maintenancePlans.getSpecificMoldId() == null){
            log.warn("创建保养计划失败, 模具为空");
            return Result.fail("模具不能为空");
        }
        // 执行策略校验：只能二选一，且至少选一个
        boolean hasInterval = maintenancePlans.getIntervalHours() != null;
        boolean hasScheduledDay = maintenancePlans.getScheduledDayOfMonth() != null;
        if (hasInterval == hasScheduledDay) {
            log.warn("创建保养计划失败, 策略配置非法, hasInterval={}, hasScheduledDay={}",
                    hasInterval, hasScheduledDay);
            return Result.fail("不能同时指定间隔小时和每月固定日，且必须二选一");
        }
        if (hasScheduledDay) {
            int day = maintenancePlans.getScheduledDayOfMonth();
            if (day < 1 || day > 31) {
                log.warn("创建保养计划失败, 每月固定日不在 1-31 范围内, day={}", day);
                return Result.fail("每月固定日需在 1-31 范围内");
            }
        }
        maintenancePlans.setId(IdUtil.fastUUID());
        maintenancePlans.setCreatedBy(UserUtils.getCurrentUserId());
        Date now = new Date();
        maintenancePlans.setCreatedAt(now);
        maintenancePlans.setUpdatedAt(now);
        log.info("开始创建保养计划, id={}, moldId={}, name={}", maintenancePlans.getId(),
                maintenancePlans.getSpecificMoldId(), maintenancePlans.getName());
        int success = maintenancePlanMapper.insert(maintenancePlans);
        if(success==1){
            // 为该保养计划插入首条保养提醒
            createInitialReminderForPlan(maintenancePlans);
            log.info("创建保养计划成功, id={}", maintenancePlans.getId());
            return Result.success();
        }
        log.warn("创建保养计划失败, 数据库插入失败, moldId={}", maintenancePlans.getSpecificMoldId());
        return Result.fail("创建失败");
    }

    @Override
    public Result<?> insertBatch(List<MaintenancePlans> maintenancePlans) {
        String currentUserId = UserUtils.getCurrentUserId();
        Date now = new Date();
        List<MaintenancePlans> validPlans = maintenancePlans.stream()
                .filter(this::chuangjianjiacha)
                .map(plan -> {
                    plan.setId(IdUtil.fastUUID());
                    plan.setCreatedBy(currentUserId);
                    plan.setCreatedAt(now);
                    plan.setUpdatedAt(now);
                    return plan;
                })
                .collect(Collectors.toList());
        log.info("开始批量创建保养计划, 有效数量={}, 原始数量={}", validPlans.size(),
                maintenancePlans != null ? maintenancePlans.size() : 0);
        maintenancePlanMapper.insertBatch(validPlans);
        // 为批量创建的计划生成首条保养提醒（忽略单条失败，不影响整体）
        validPlans.forEach(this::createInitialReminderForPlan);
        if(validPlans.size()!=maintenancePlans.size()){
            log.warn("批量创建保养计划部分失败, 成功数量={}, 原始数量={}", validPlans.size(), maintenancePlans.size());
            return Result.fail("成功插入 "+validPlans.size()+" 条·,部分插入失败，请检查");
        }

        return Result.success();
    }

    @Override
    public Result<?> deletePlan(String id) {

       int success =  maintenancePlanMapper.deleteById(id);
       if(success==1){
           log.info("删除保养计划成功, id={}", id);
           return Result.success();
       }
        log.warn("删除保养计划失败, id={}", id);
        return Result.fail("删除失败");
    }

    @Override
    public Result<?> deletePlansBatch(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的保养计划");
        }
        return ids.stream()
                .map(this::deletePlan)
                .filter(r -> r.getCode() != 200)
                .findFirst()
                .orElseGet(Result::success);
    }

    @Override
    public Result<MaintenancePlans> update(MaintenancePlans maintenancePlans) {
        if (maintenancePlans.getId() == null || maintenancePlans.getId().trim().isEmpty()) {
            return Result.fail("保养计划ID不能为空");
        }
        int success = maintenancePlanMapper.update(maintenancePlans);
        if(success==1){
            log.info("更新保养计划成功, id={}", maintenancePlans.getId());
            return Result.success();
        }
        log.warn("更新保养计划失败, id={}", maintenancePlans.getId());
        return Result.fail("更新失败");
    }

    @Override
    public PageInfo<MaintenancePlanWithMoldVO> query(MaintenancePlanQueryParam maintenancePlanQueryParam,
                                                     int pageNum,
                                                     int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MaintenancePlanWithMoldVO> list = maintenancePlanMapper.query(maintenancePlanQueryParam);
        return new PageInfo<>(list);
    }

    @Override
    public Result<?> enablePlan(String id) {
        int success = maintenancePlanMapper.updateActiveStatus(id, 1);
        if (success == 1) {
            log.info("启用保养计划成功, id={}", id);
            return Result.success();
        }
        log.warn("启用保养计划失败, id={}", id);
        return Result.fail("启用失败");
    }

    @Override
    public Result<?> disablePlan(String id) {
        int success = maintenancePlanMapper.updateActiveStatus(id, 0);
        if (success == 1) {
            log.info("停用保养计划成功, id={}", id);
            return Result.success();
        }
        log.warn("停用保养计划失败, id={}", id);
        return Result.fail("停用失败");
    }

    /**
     * 为新建的保养计划生成首条保养提醒记录。
     * 失败不会影响保养计划本身的创建，只记录日志。
     */
    private void createInitialReminderForPlan(MaintenancePlans plan) {
        try {
            String moldId = plan.getSpecificMoldId();
            if (moldId == null || moldId.trim().isEmpty()) {
                log.warn("创建首条保养提醒失败, 模具ID为空, planId={}", plan.getId());
                return;
            }
            Molds mold = Optional.ofNullable(moldsMapper.selectById(moldId))
                    .orElse(null);
            MaintenanceReminder reminder = new MaintenanceReminder();
            reminder.setId(IdUtil.fastUUID());
            reminder.setMoldId(moldId);
            reminder.setPlanId(plan.getId());
            reminder.setPlanName(plan.getName());

            Date now = new Date();
            reminder.setCreatedAt(now);
            reminder.setUpdatedAt(now);
            reminder.setStatus(1); // 待处理

            // 1) 按时间周期：scheduledDayOfMonth 不为空
            if (plan.getScheduledDayOfMonth() != null) {
                reminder.setReminderType(1);
                // 按配置近似展示“日历周期”间隔天数（如按月固定日）
                reminder.setIntervalValue(systemBusinessConfigService.getEffectiveInt(BusinessConfigKeys.MAINTENANCE_REMINDER_CALENDAR_INTERVAL_DAYS));
                // 计算下一次保养日期：本月的该日，如果已过则下个月
                LocalDate today = LocalDate.now();
                int day = plan.getScheduledDayOfMonth();
                LocalDate candidate = today.withDayOfMonth(Math.min(day, today.lengthOfMonth()));
                if (!candidate.isAfter(today)) {
                    LocalDate nextMonth = today.plusMonths(1);
                    candidate = nextMonth.withDayOfMonth(Math.min(day, nextMonth.lengthOfMonth()));
                }
                reminder.setNextDueDate(Date.from(candidate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            // 2) 按使用次数：intervalHours 不为空时使用累计使用次数近似表示
            else if (plan.getIntervalHours() != null) {
                reminder.setReminderType(2);
                int interval = Math.max(plan.getIntervalHours(), 1);
                reminder.setIntervalValue(interval);
                int currentCycles = Optional.ofNullable(mold)
                        .map(Molds::getTotalUsageCount)
                        .orElse(0);
                reminder.setNextDueCycles(currentCycles + interval);
            } else {
                // 理论上不会发生（已在上游校验），防御性返回
                log.warn("创建首条保养提醒失败, 策略配置非法, planId={}", plan.getId());
                return;
            }

            maintenanceReminderMapper.insert(reminder);
            log.info("创建首条保养提醒成功, reminderId={}, planId={}, moldId={}", reminder.getId(), plan.getId(), moldId);
        } catch (Exception e) {
            log.warn("创建首条保养提醒异常, planId={}, err={}", plan.getId(), e.getMessage());
        }
    }

    private boolean chuangjianjiacha(MaintenancePlans plan) {
        if(plan.getSpecificMoldId() == null){
            return false;
        }
        boolean hasInterval = plan.getIntervalHours() != null;
        boolean hasScheduledDay = plan.getScheduledDayOfMonth() != null;

        if (hasInterval == hasScheduledDay) { // 两个都空 或 两个都有 → 非法
            return false;
        }

        // 3. 如果是日历模式，检查日期范围
        if (hasScheduledDay) {
            int day = plan.getScheduledDayOfMonth();
            if (day < 1 || day > 31) {
                return false;
            }
        }
        MaintenancePlans planById = maintenancePlanMapper.getByMoldId(plan.getSpecificMoldId());
        if(planById!=null){
            return false;
        }
        return true;

    }
}
