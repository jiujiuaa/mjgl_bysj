package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.mapper.LubricationLogMapper;
import com.zjb.mjgl.pojo.dto.LubricationLogQueryParam;
import com.zjb.mjgl.pojo.entity.LubricationLogs;
import com.zjb.mjgl.pojo.vo.LubricationLogVO;
import com.zjb.mjgl.service.LubricationLogService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LubricationLogServiceImpl implements LubricationLogService {

    private final LubricationLogMapper lubricationLogMapper;

    @Override
    public Result<String> create(LubricationLogs log) {
        if (log == null || log.getMoldId() == null) {
            return Result.fail("模具ID不能为空");
        }
        log.setId(Optional.ofNullable(log.getId()).orElseGet(IdUtil::fastUUID));
        Optional.ofNullable(UserUtils.getCurrentUserDetails())
                .ifPresent(user -> log.setOperatorId(
                        Optional.ofNullable(log.getOperatorId()).orElse(user.getId())
                ));
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(new Date());
        }
        int rows = lubricationLogMapper.insert(log);
        return rows > 0 ? Result.success(log.getId()) : Result.fail("保存润滑巡检记录失败");
    }

    @Override
    public Result<PageInfo<LubricationLogs>> getByMoldId(String moldId, int pageNum, int pageSize) {
        if (moldId == null || moldId.trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<LubricationLogs> pageInfo = new PageInfo<>(lubricationLogMapper.listByMoldId(moldId));
        return Result.success(pageInfo);
    }

    @Override
    public PageInfo<LubricationLogVO> queryByCondition(LubricationLogQueryParam param, int pageNum, int pageSize) {
        LubricationLogQueryParam effective = Optional.ofNullable(param)
                .orElseGet(LubricationLogQueryParam::new);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(lubricationLogMapper.queryByCondition(effective));
    }

    @Override
    public Result<?> update(LubricationLogs log) {
        if (log == null || log.getId() == null || log.getId().trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = lubricationLogMapper.update(log);
        return rows > 0 ? Result.success() : Result.fail("更新润滑巡检记录失败");
    }

    @Override
    public Result<?> delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = lubricationLogMapper.deleteById(id);
        return rows > 0 ? Result.success() : Result.fail("删除润滑巡检记录失败");
    }
}

