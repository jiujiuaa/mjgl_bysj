package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.mapper.MoldAbnormalRecordMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MoldAbnormalRecordQueryParam;
import com.zjb.mjgl.pojo.entity.MoldAbnormalRecord;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.pojo.vo.MoldAbnormalRecordVO;
import com.zjb.mjgl.service.AlertMessageService;
import com.zjb.mjgl.service.MoldAbnormalRecordService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoldAbnormalRecordServiceImpl implements MoldAbnormalRecordService {

    private final MoldAbnormalRecordMapper moldAbnormalRecordMapper;
    private final AlertMessageService alertMessageService;

    @Override
    public Result<String> createManual(MoldAbnormalRecord record) {
        if (record == null || record.getMoldId() == null) {
            return Result.fail("模具ID不能为空");
        }

        record.setId(Optional.ofNullable(record.getId()).orElseGet(IdUtil::fastUUID));
        Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .ifPresent(user -> record.setOperatorId(
                        Optional.ofNullable(record.getOperatorId()).orElse(user.getId())
                ));
        record.setSourceType(Optional.ofNullable(record.getSourceType()).orElse(4));
        if (record.getOccurredAt() == null) {
            record.setOccurredAt(new Date());
        }

        int rows = moldAbnormalRecordMapper.insert(record);
        if (rows <= 0) {
            return Result.fail("保存异常记录失败");
        }

        MoldAlertMessage message = new MoldAlertMessage();
        message.setTitle("人工异常上报");
        String content = String.format("模具[%s]发生异常，类型=%d，实际值=%s，阈值=%s",
                record.getMoldId(),
                Optional.ofNullable(record.getAbnormalType()).orElse(0),
                Optional.ofNullable(record.getMeasuredValue()).orElse("-"),
                Optional.ofNullable(record.getThresholdValue()).orElse("-"));
        message.setContent(content);
        message.setType("WARNING");
        message.setBiz_type("manual_abnormal");
        message.setTime(LocalDateTime.now());

        alertMessageService.broadcastAlert(message);

        log.info("人工异常上报成功, moldId={}, recordId={}", record.getMoldId(), record.getId());
        return Result.success(record.getId());
    }

    @Override
    public Result<PageInfo<MoldAbnormalRecord>> getByMoldId(String moldId, int pageNum, int pageSize) {
        if (moldId == null || moldId.trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<MoldAbnormalRecord> pageInfo =
                new PageInfo<>(moldAbnormalRecordMapper.listByMoldId(moldId));
        return Result.success(pageInfo);
    }

    @Override
    public PageInfo<MoldAbnormalRecordVO> queryByCondition(MoldAbnormalRecordQueryParam param, int pageNum, int pageSize) {
        MoldAbnormalRecordQueryParam effective = Optional.ofNullable(param)
                .orElseGet(MoldAbnormalRecordQueryParam::new);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(moldAbnormalRecordMapper.queryByCondition(effective));
    }

    @Override
    public Result<?> update(MoldAbnormalRecord record) {
        if (record == null || record.getId() == null || record.getId().trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = moldAbnormalRecordMapper.update(record);
        return rows > 0 ? Result.success() : Result.fail("更新异常记录失败");
    }

    @Override
    public Result<?> delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = moldAbnormalRecordMapper.deleteById(id);
        return rows > 0 ? Result.success() : Result.fail("删除异常记录失败");
    }

    @Override
    public Result<?> deleteBatch(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的异常记录");
        }
        return ids.stream()
                .map(this::delete)
                .filter(r -> r.getCode() != 200)
                .findFirst()
                .orElseGet(Result::success);
    }
}

