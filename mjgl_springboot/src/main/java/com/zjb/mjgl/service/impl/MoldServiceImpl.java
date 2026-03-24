package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.mapper.AlertRecordMapper;
import com.zjb.mjgl.mapper.LubricationLogMapper;
import com.zjb.mjgl.mapper.FilesMapper;
import com.zjb.mjgl.mapper.MaintenanceLogMapper;
import com.zjb.mjgl.mapper.MaintenancePlanMapper;
import com.zjb.mjgl.mapper.MaintenanceReminderMapper;
import com.zjb.mjgl.mapper.MoldQrcodesMapper;
import com.zjb.mjgl.mapper.MoldAbnormalRecordMapper;
import com.zjb.mjgl.mapper.MoldSpecsMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.mapper.RepairRecordMapper;
import com.zjb.mjgl.mapper.TemperatureLogMapper;
import com.zjb.mjgl.mapper.UseRecordMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.entity.Files;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.entity.MoldQrcodes;
import com.zjb.mjgl.pojo.entity.MoldSpecs;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import com.zjb.mjgl.pojo.vo.MoldUsageRecordVO;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.service.MoldService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.MinioUtil;
import com.zjb.mjgl.utils.QrcodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模具服务类
 */
@Slf4j
@Service
public class MoldServiceImpl implements MoldService {

    @Autowired
    private MoldsMapper moldsMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private MaintenancePlanMapper maintenancePlanMapper;

    @Autowired
    private MaintenanceReminderMapper maintenanceReminderMapper;

    @Autowired
    private MaintenanceLogMapper maintenanceLogMapper;

    @Autowired
    private LubricationLogMapper lubricationLogMapper;

    @Autowired
    private RepairRecordMapper repairRecordMapper;

    @Autowired
    private MoldAbnormalRecordMapper moldAbnormalRecordMapper;

    @Autowired
    private TemperatureLogMapper temperatureLogMapper;

    @Autowired
    private UseRecordMapper useRecordMapper;

    @Autowired
    private MoldSpecsMapper moldSpecsMapper;
    @Autowired
    private MoldQrcodesMapper moldQrcodesMapper;
    @Autowired
    private FilesMapper fileMapper;
    @Autowired
    private MinioUtil minioUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoldDetailVO createMold(MoldParam param) {
        Date now = new Date();
        String moldId = IdUtil.fastUUID();

        moldsMapper.insertMold(paramToMoldForCreate(param, moldId, now));
        moldSpecsMapper.insertSpecs(paramToSpecs(param, moldId));
        Optional.ofNullable(paramToQrcode(param, moldId, now)).ifPresent(moldQrcodesMapper::insertQrcode);
        return moldsMapper.selectDetailById(moldId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMold(MoldParam param) {
        Date now = new Date();
        Molds mold = paramToMoldForUpdate(param, now);

        moldsMapper.updateMold(mold);
        moldSpecsMapper.updateSpecsByMoldId(paramToSpecs(param, mold.getId()));
        moldQrcodesMapper.deleteByMoldId(mold.getId());
        Optional.ofNullable(paramToQrcode(param, mold.getId(), now)).ifPresent(moldQrcodesMapper::insertQrcode);
    }

    private Molds paramToMoldForCreate(MoldParam param, String moldId, Date now) {
        Molds m = new Molds();
        BeanUtils.copyProperties(param, m,
                "id", "totalUsageCount", "totalProductionTime", "lastMaintenanceDate",
                "totalRepairCost", "totalMaintenanceCost", "createdAt", "updatedAt", "specs", "qrcodeType");
        m.setId(moldId);
        m.setCurrentStatus(Optional.ofNullable(m.getCurrentStatus()).orElse(1));
        m.setTotalUsageCount(0);
        m.setTotalProductionTime(0.0);
        m.setLastMaintenanceDate(null);
        m.setTotalRepairCost(0.0);
        m.setTotalMaintenanceCost(0.0);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }

    private Molds paramToMoldForUpdate(MoldParam param, Date now) {
        Molds m = new Molds();
        BeanUtils.copyProperties(param, m,
                "totalUsageCount", "totalProductionTime", "lastMaintenanceDate",
                "totalRepairCost", "totalMaintenanceCost", "createdAt", "updatedAt", "specs", "qrcodeType");
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }

    private MoldSpecs paramToSpecs(MoldParam param, String moldId) {
        MoldSpecs s = Optional.ofNullable(param.getSpecs()).orElseGet(MoldSpecs::new);
        s.setMoldId(moldId);
        if (s.getId() == null) {
            s.setId(IdUtil.fastUUID());
        }
        return s;
    }

    /** 一对一：从参数生成一条二维码记录，无类型则返回 null */
    private MoldQrcodes paramToQrcode(MoldParam param, String moldId, Date now) {
        if (param.getQrcodeType() == null) {
            return null;
        }
        MoldQrcodes q = new MoldQrcodes();
        q.setId(QrcodeUtil.generateMoldQrcodeId(moldId));
        q.setMoldId(moldId);
        q.setQrcodeType(param.getQrcodeType());
        q.setIsActive(1);
        q.setCreatedAt(LocalDateTime.now());
        return q;
    }

    @Override
    public void deteleMold(String id) {
        String moldId = Optional.ofNullable(id)
                .map(String::trim)
                .orElse(null);
        if (moldId == null || moldId.isEmpty()) {
            log.warn("删除模具失败: moldId 为空");
            return;
        }

        // 级联清理顺序：先删子表，再删父表（避免外键约束失败）
        alertRecordMapper.deleteByMoldId(moldId);

        // 保养提醒：maintenance_reminders（通过 listByMoldId 再逐条删）
        Optional.ofNullable(maintenanceReminderMapper.listByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(MaintenanceReminderVO::getId)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .forEach(maintenanceReminderMapper::deleteById);

        // 保养记录：maintenance_logs
        Optional.ofNullable(maintenanceLogMapper.getByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .forEach(logEntity -> {
                    if (logEntity != null && logEntity.getId() != null) {
                        maintenanceLogMapper.deleteById(logEntity.getId());
                    }
                });

        // 保养计划：maintenance_plans（本项目约定 specific_mold_id 每模具只有一条）
        // 注意：maintenance_logs 依赖 plan_id，需在删除计划前先清理日志
        MaintenancePlans plan = maintenancePlanMapper.getByMoldId(moldId);
        Optional.ofNullable(plan)
                .map(MaintenancePlans::getId)
                .filter(Objects::nonNull)
                .ifPresent(maintenancePlanMapper::deleteById);

        // 润滑记录：lubrication_logs
        Optional.ofNullable(lubricationLogMapper.listByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .forEach(l -> {
                    if (l != null && l.getId() != null) {
                        lubricationLogMapper.deleteById(l.getId());
                    }
                });

        // 维修记录：repair_records
        Optional.ofNullable(repairRecordMapper.getrecordByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .forEach(r -> {
                    if (r != null && r.getId() != null) {
                        repairRecordMapper.deleteById(r.getId());
                    }
                });

        // 异常上报：abnormal_records
        Optional.ofNullable(moldAbnormalRecordMapper.listByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .forEach(a -> {
                    if (a != null && a.getId() != null) {
                        moldAbnormalRecordMapper.deleteById(a.getId());
                    }
                });

        // 温度记录：temperature_logs
        Optional.ofNullable(temperatureLogMapper.listByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .forEach(t -> {
                    if (t != null && t.getId() != null) {
                        temperatureLogMapper.deleteById(t.getId());
                    }
                });

        // 使用/借出记录：mold_usage_records
        Optional.ofNullable(useRecordMapper.listByMoldId(moldId))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(MoldUsageRecordVO::getId)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .forEach(useRecordMapper::deleteById);

        // 二维码与规格
        moldQrcodesMapper.deleteByMoldId(moldId);
        moldSpecsMapper.deleteById(moldId);

        // 文件（含 MinIO 对象）
        List<Files> files = Optional.ofNullable(fileMapper.selectByMoldId(moldId))
                .orElseGet(Collections::emptyList);
        List<String> objects = files.stream()
                .map(Files::getFilePath)
                .filter(Objects::nonNull)
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
        if (!objects.isEmpty()) {
            minioUtil.deleteFiles(objects);
        }
        fileMapper.deleteByMoldId(moldId);

        // 最后删除模具主表
        moldsMapper.deleteById(moldId);

    }

    @Override
    public void deleteMoldsBatch(List<String> rawIds) {
        BatchIdsDTO.normalizeList(rawIds).forEach(this::deteleMold);
    }

    @Override
    public PageInfo<MoldDetailVO> listAllAsDetail(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(moldsMapper.listAllAsDetail());
    }

    @Override
    public PageInfo<MoldDetailVO> queryByCondition(MoldQueryParam param, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(moldsMapper.selectDetailByCondition(param));
    }
}
