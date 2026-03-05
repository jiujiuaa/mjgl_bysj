package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.AlertStatusEnum;
import com.zjb.mjgl.mapper.AlertRecordMapper;
import com.zjb.mjgl.pojo.dto.AlertRecordQueryParam;
import com.zjb.mjgl.pojo.entity.AlertRecord;
import com.zjb.mjgl.pojo.vo.AlertRecordVO;
import com.zjb.mjgl.service.AlertRecordService;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRecordServiceImpl implements AlertRecordService {

    private final AlertRecordMapper alertRecordMapper;

    @Override
    public PageInfo<AlertRecordVO> queryByCondition(AlertRecordQueryParam param, int pageNum, int pageSize) {
        AlertRecordQueryParam effective = Optional.ofNullable(param).orElseGet(AlertRecordQueryParam::new);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(alertRecordMapper.queryByCondition(effective));
    }

    @Override
    public Result<AlertRecordVO> getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报警ID不能为空");
        }
        AlertRecordVO vo = alertRecordMapper.selectVoById(id);
        return vo != null ? Result.success(vo) : Result.fail("报警记录不存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> resolve(String id, String remark) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报警ID不能为空");
        }
        AlertRecord record = alertRecordMapper.selectById(id);
        if (record == null) {
            return Result.fail("报警记录不存在");
        }
        if (AlertStatusEnum.ACTIVE.getCode().equals(record.getStatus())) {
            record.setStatus(AlertStatusEnum.RESOLVED.getCode());
            record.setResolvedAt(LocalDateTime.now());
            record.setResolvedBy(Optional.ofNullable(UserUtils.getCurrentUserId()).orElse(record.getResolvedBy()));
            alertRecordMapper.update(record);
        }
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> ignore(String id, String remark) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("报警ID不能为空");
        }
        AlertRecord record = alertRecordMapper.selectById(id);
        if (record == null) {
            return Result.fail("报警记录不存在");
        }
        if (AlertStatusEnum.ACTIVE.getCode().equals(record.getStatus())) {
            record.setStatus(AlertStatusEnum.IGNORED.getCode());
            alertRecordMapper.update(record);
        }
        return Result.success();
    }
}
