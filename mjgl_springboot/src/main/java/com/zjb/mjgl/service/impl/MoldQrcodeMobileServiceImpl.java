package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.RepairStatusEnum;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.MaintenancePlanMapper;
import com.zjb.mjgl.mapper.MoldQrcodesMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.MoldAbnormalRecordQueryParam;
import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import com.zjb.mjgl.pojo.dto.RepairQueryParam;
import com.zjb.mjgl.pojo.dto.MoldQrcodeMobileResolveRequest;
import com.zjb.mjgl.pojo.entity.*;
import com.zjb.mjgl.pojo.vo.HealthReportVO;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.pojo.vo.MoldAbnormalRecordVO;
import com.zjb.mjgl.pojo.vo.RepairRecordVO;
import com.zjb.mjgl.service.*;
import com.zjb.mjgl.utils.QrcodeUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 移动端二维码交互 resolve 实现：
 * - 二维码内容提供 codeId
 * - scanType 决定后续业务（先扫码后选动作）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoldQrcodeMobileServiceImpl implements MoldQrcodeMobileService {

    private final MoldQrcodesMapper moldQrcodesMapper;
    private final MoldsMapper moldsMapper;

    private final RepairService repairService;
    private final MoldAbnormalRecordService moldAbnormalRecordService;

    private final MaintenancePlanMapper maintenancePlanMapper;
    private final MaintenanceLogService maintenanceLogService;
    private final MaintenanceReminderService maintenanceReminderService;
    private final TemperatureLogService temperatureLogService;
    private final LubricationLogService lubricationLogService;

    private final HealthReportService healthReportService;

    @Override
    public Result<?> resolve(MoldQrcodeMobileResolveRequest request) {
        if (request == null) {
            return Result.fail("请求体不能为空");
        }
        String codeId = Optional.ofNullable(request.getCodeId())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);
        Integer scanType = request.getScanType();
        if (codeId == null) {
            return Result.fail("codeId不能为空");
        }
        if (scanType == null) {
            return Result.fail("scanType不能为空");
        }

        MoldQrcodes qrcode = lookupQrcodeByCodeId(codeId);
        if (qrcode == null) {
            return Result.fail(404, "QR_CODE_NOT_FOUND");
        }
        if (!Integer.valueOf(1).equals(qrcode.getIsActive())) {
            return Result.fail(403, "QR_CODE_DISABLED");
        }
        String moldId = qrcode.getMoldId();
        if (moldId == null || moldId.trim().isEmpty()) {
            return Result.fail(500, "QR_CODE_MOLD_ID_MISSING");
        }

        // 基础页码默认值
        int pageNum = Optional.ofNullable(request.getPageNum()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize()).orElse(10);

        // 管理/敏感操作：仅管理员
        if (Integer.valueOf(9).equals(scanType)) {
            RoleEnum role = Optional.ofNullable(UserUtils.getCurrentUserDetails())
                    .map(com.zjb.mjgl.pojo.entity.User::getRole)
                    .orElse(RoleEnum.USER);
            if (role != RoleEnum.ADMIN) {
                return Result.fail(403, "SCAN_TYPE_PERMISSION_DENIED");
            }
        }

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("scanType", scanType);

        switch (scanType) {
            case 1: {
                MoldDetailVO vo = moldsMapper.selectDetailById(moldId);
                wrapper.put("result", vo);
                return Result.success(wrapper);
            }
            case 2: {
                RepairRecordDTO dto = new RepairRecordDTO();
                dto.setMoldId(moldId);
                dto.setRepairReason(Optional.ofNullable(request.getRepairReason())
                        .filter(s -> !s.trim().isEmpty())
                        .orElseGet(() -> Optional.ofNullable(request.getRepairDescription())
                                .orElse(null)));
                dto.setRepairDescription(Optional.ofNullable(request.getRepairDescription())
                        .filter(s -> !s.trim().isEmpty())
                        .orElse(Optional.ofNullable(request.getDetails()).orElse(null)));

                String repairId = repairService.createRepairRecord(dto);
                if (repairId == null || repairId.trim().isEmpty()) {
                    return Result.fail("创建维修记录失败");
                }
                Map<String, Object> result = new HashMap<>();
                result.put("repairId", repairId);
                result.put("moldId", moldId);
                result.put("status", RepairStatusEnum.PENDING.getCode());
                wrapper.put("result", result);
                return Result.success(wrapper);
            }
            case 3: {
                MoldAbnormalRecord record = new MoldAbnormalRecord();
                record.setMoldId(moldId);
                record.setAbnormalType(request.getAbnormalType());
                record.setMeasuredValue(request.getMeasuredValue());
                record.setThresholdValue(request.getThresholdValue());
                record.setDescription(Optional.ofNullable(request.getDescription())
                        .orElse(request.getDetails()));
                record.setOccurredAt(request.getOccurredAt());
                // sourceType 默认在 service 内设置为 4（人工录入）
                Result<String> created = moldAbnormalRecordService.createManual(record);
                if (created == null || created.getCode() != 200) {
                    return Result.fail(created == null ? 500 : created.getCode(),
                            created == null ? "创建异常记录失败" : created.getMessage());
                }
                Map<String, Object> result = new HashMap<>();
                result.put("abnormalReportId", created.getData());
                result.put("moldId", moldId);
                wrapper.put("result", result);
                return Result.success(wrapper);
            }
            case 4: {
                RepairQueryParam param = new RepairQueryParam();
                param.setMoldId(moldId);
                param.setKeyword(request.getKeyword());
                param.setStatus(request.getStatus());
                PageInfo<RepairRecordVO> page = repairService.queryByCondition(param, pageNum, pageSize);
                wrapper.put("result", page);
                return Result.success(wrapper);
            }
            case 5: {
                MoldAbnormalRecordQueryParam param = new MoldAbnormalRecordQueryParam();
                param.setMoldId(moldId);
                param.setAbnormalType(request.getAbnormalType());
                param.setSourceType(request.getSourceType());
                param.setStartTime(request.getStartTime());
                param.setEndTime(request.getEndTime());
                PageInfo<MoldAbnormalRecordVO> page = moldAbnormalRecordService
                        .queryByCondition(param, pageNum, pageSize);
                wrapper.put("result", page);
                return Result.success(wrapper);
            }
            case 6: {
                int type = Optional.ofNullable(request.getMaintenanceOrCheckType()).orElse(1);
                Integer status = type;
                // maintenance logs
                if (type == 1) {
                    MaintenancePlans plan = maintenancePlanMapper.getByMoldId(moldId);
                    if (plan == null || plan.getId() == null || plan.getId().trim().isEmpty()) {
                        return Result.fail(400, "MAINTENANCE_PLAN_NOT_FOUND");
                    }
                    MaintenanceLogs logs = new MaintenanceLogs();
                    logs.setMoldId(moldId);
                    logs.setPlanId(plan.getId());
                    logs.setMaintenanceType(Optional.ofNullable(request.getMaintenanceType())
                            .filter(s -> !s.trim().isEmpty())
                            .orElse(plan.getName()));
                    logs.setDetails(request.getDetails());
                    logs.setActualStartTime(new Date());
                    logs.setActualEndTime(request.getActualEndTime()); // 可为 null

                    if (request.getCost() != null) {
                        logs.setCost(request.getCost());
                    }
                    if (request.getFileIdsJson() != null) {
                        logs.setFileIds(request.getFileIdsJson());
                    }

                    Result<String> created = maintenanceLogService.create(logs);
                    if (created == null || created.getCode() != 200) {
                        return Result.fail(created == null ? 500 : created.getCode(),
                                created == null ? "创建保养记录失败" : created.getMessage());
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("maintenanceLogId", created.getData());
                    result.put("moldId", moldId);
                    result.put("planId", plan.getId());
                    result.put("type", status);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                // temperature logs
                if (type == 2) {
                    TemperatureLogs log = new TemperatureLogs();
                    log.setMoldId(moldId);
                    log.setSensorLocation(request.getSensorLocation());
                    log.setTemperature(request.getTemperature());
                    log.setOperationTime(Optional.ofNullable(request.getOperationTime()).orElse(new Date()));
                    log.setDescription(request.getDetails());
                    Result<String> created = temperatureLogService.create(log);
                    if (created == null || created.getCode() != 200) {
                        return Result.fail(created == null ? 500 : created.getCode(),
                                created == null ? "创建温度巡检记录失败" : created.getMessage());
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("temperatureLogId", created.getData());
                    result.put("moldId", moldId);
                    result.put("type", status);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                // lubrication logs
                if (type == 3) {
                    LubricationLogs log = new LubricationLogs();
                    log.setMoldId(moldId);
                    log.setOilLevelPercent(request.getOilLevelPercent());
                    log.setPressureKpa(request.getPressureKpa());
                    log.setOperationTime(Optional.ofNullable(request.getOperationTime()).orElse(new Date()));
                    log.setDescription(request.getDetails());
                    Result<String> created = lubricationLogService.create(log);
                    if (created == null || created.getCode() != 200) {
                        return Result.fail(created == null ? 500 : created.getCode(),
                                created == null ? "创建润滑巡检记录失败" : created.getMessage());
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("lubricationLogId", created.getData());
                    result.put("moldId", moldId);
                    result.put("type", status);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                return Result.fail(400, "UNSUPPORTED_MAINTENANCE_OR_CHECK_TYPE");
            }
            case 7: {
                if (request.getHealthReportId() != null && !request.getHealthReportId().trim().isEmpty()) {
                    Result<HealthReportVO> report = healthReportService.getById(request.getHealthReportId());
                    if (report == null || report.getCode() != 200) {
                        return Result.fail(report == null ? 500 : report.getCode(),
                                report == null ? "查询健康报告失败" : report.getMessage());
                    }
                    wrapper.put("result", report.getData());
                    return Result.success(wrapper);
                }

                // latest report: query all and take max by periodEnd
                com.zjb.mjgl.pojo.dto.HealthReportQueryParam param = new com.zjb.mjgl.pojo.dto.HealthReportQueryParam();
                param.setMoldId(moldId);
                // status 可选：不传则包含所有状态，便于取“最新”
                if (request.getStatus() != null) {
                    param.setStatus(request.getStatus());
                }
                PageInfo<HealthReportVO> page = healthReportService.queryByCondition(param, 1, 50);
                List<HealthReportVO> list = page != null && page.getList() != null ? page.getList() : Collections.emptyList();
                HealthReportVO latest = list.stream()
                        .filter(Objects::nonNull)
                        .max(Comparator.comparing(HealthReportVO::getReportPeriodEnd,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null);
                if (latest == null) {
                    return Result.fail(404, "HEALTH_REPORT_NOT_FOUND");
                }
                wrapper.put("result", latest);
                return Result.success(wrapper);
            }
            case 8: {
                // 先拉取候选提醒列表（当 action 或 reminderId 为空时）
                List<MaintenanceReminderVO> reminders = Optional.ofNullable(
                                maintenanceReminderService.listByMoldId(moldId).getData())
                        .orElseGet(Collections::emptyList);
                if (request.getMaintenanceReminderId() == null
                        || request.getAction() == null
                        || request.getAction().trim().isEmpty()) {
                    List<MaintenanceReminderVO> candidates = reminders.stream()
                            .filter(Objects::nonNull)
                            .filter(r -> r.getStatus() == null || r.getStatus() == 1 || r.getStatus() == 2)
                            .collect(Collectors.toList());
                    Map<String, Object> result = new HashMap<>();
                    result.put("moldId", moldId);
                    result.put("candidates", candidates);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                String reminderId = request.getMaintenanceReminderId();
                String action = request.getAction().trim().toUpperCase(Locale.ROOT);
                if ("IGNORE".equals(action)) {
                    Result<?> ignored = maintenanceReminderService.ignoreReminder(reminderId);
                    if (ignored == null || ignored.getCode() != 200) {
                        return Result.fail(ignored == null ? 500 : ignored.getCode(),
                                ignored == null ? "忽略提醒失败" : ignored.getMessage());
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("maintenanceReminderId", reminderId);
                    result.put("newStatus", 4);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                if ("COMPLETE".equals(action)) {
                    MaintenanceReminder reminder = maintenanceReminderService.getById(reminderId).getData();
                    if (reminder == null) {
                        return Result.fail(404, "MAINTENANCE_REMINDER_NOT_FOUND");
                    }
                    // 按 reminder.planId 为准（更可靠）
                    MaintenanceLogs logs = new MaintenanceLogs();
                    logs.setMoldId(reminder.getMoldId());
                    logs.setPlanId(reminder.getPlanId());
                    logs.setMaintenanceType(Optional.ofNullable(request.getMaintenanceReminderType())
                            .filter(s -> !s.trim().isEmpty())
                            .orElse(reminder.getPlanName()));
                    logs.setDetails(request.getMaintenanceReminderDetails());
                    logs.setActualStartTime(new Date());
                    logs.setActualEndTime(new Date());

                    Result<String> created = maintenanceLogService.create(logs);
                    if (created == null || created.getCode() != 200) {
                        return Result.fail(created == null ? 500 : created.getCode(),
                                created == null ? "完成提醒失败" : created.getMessage());
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("maintenanceReminderId", reminderId);
                    result.put("maintenanceLogId", created.getData());
                    result.put("newStatus", 1);
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }

                return Result.fail(400, "UNSUPPORTED_ACTION");
            }
            case 9: {
                String op = Optional.ofNullable(request.getOperation())
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .orElse("");
                if ("DISABLE".equals(op)) {
                    int rows = moldQrcodesMapper.updateIsActiveById(qrcode.getId(), 0);
                    if (rows <= 0) {
                        return Result.fail("停用二维码失败");
                    }
                    Map<String, Object> result = new HashMap<>();
                    result.put("qrcodeId", qrcode.getId());
                    result.put("newStatus", "DISABLED");
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }
                if ("REPLACE".equals(op)) {
                    String newMoldId = Optional.ofNullable(request.getNewMoldId())
                            .map(String::trim)
                            .orElse(null);
                    if (newMoldId == null || newMoldId.isEmpty()) {
                        return Result.fail(400, "NEW_MOLD_ID_REQUIRED");
                    }

                    // 1) 停用旧二维码
                    moldQrcodesMapper.updateIsActiveById(qrcode.getId(), 0);

                    // 2) 生成新二维码记录（最小实现：不写 replaced_by）
                    String generated = QrcodeUtil.generateMoldQrcodeId(newMoldId);
                    String codeForQr = generated.length() > 32 ? generated.substring(0, 32) : generated;

                    MoldQrcodes newCode = new MoldQrcodes();
                    newCode.setId(codeForQr);
                    newCode.setMoldId(newMoldId);
                    newCode.setQrcodeType(Optional.ofNullable(request.getNewQrcodeType()).orElse(
                            Optional.ofNullable(qrcode.getQrcodeType()).orElse(1)
                    ));
                    newCode.setIsActive(1);
                    newCode.setCreatedAt(java.time.LocalDateTime.now());
                    moldQrcodesMapper.insertQrcode(newCode);

                    Map<String, Object> result = new HashMap<>();
                    result.put("oldQrcodeId", qrcode.getId());
                    result.put("newQrcodeId", newCode.getId());
                    result.put("newStatus", "ACTIVE");
                    wrapper.put("result", result);
                    return Result.success(wrapper);
                }
                return Result.fail(400, "UNSUPPORTED_OPERATION");
            }
            default:
                return Result.fail(400, "UNSUPPORTED_SCAN_TYPE");
        }
    }

    private MoldQrcodes lookupQrcodeByCodeId(String codeId) {
        if (codeId == null || codeId.trim().isEmpty()) {
            return null;
        }
        String trimmed = codeId.trim();
        MoldQrcodes found = moldQrcodesMapper.selectById(trimmed);
        if (found != null) {
            return found;
        }
        // 兼容：若 DB 字段长度被截断（mold_id 为 32），可能只存了前缀（moldId）
        if (trimmed.contains("-")) {
            String prefix = trimmed.split("-", 2)[0];
            if (!prefix.trim().isEmpty()) {
                found = moldQrcodesMapper.selectById(prefix);
                if (found != null) {
                    return found;
                }
            }
        }
        if (trimmed.length() > 32) {
            found = moldQrcodesMapper.selectById(trimmed.substring(0, 32));
        }
        return found;
    }
}

