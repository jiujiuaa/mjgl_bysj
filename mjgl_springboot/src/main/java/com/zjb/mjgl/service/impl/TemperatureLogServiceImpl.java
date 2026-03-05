package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.mapper.TemperatureLogMapper;
import com.zjb.mjgl.pojo.dto.TemperatureLogQueryParam;
import com.zjb.mjgl.pojo.entity.TemperatureLogs;
import com.zjb.mjgl.pojo.vo.TemperatureLogVO;
import com.zjb.mjgl.service.TemperatureLogService;
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
public class TemperatureLogServiceImpl implements TemperatureLogService {

    private final TemperatureLogMapper temperatureLogMapper;

    @Override
    public Result<String> create(TemperatureLogs log) {
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
        int rows = temperatureLogMapper.insert(log);
        return rows > 0 ? Result.success(log.getId()) : Result.fail("保存温度巡检记录失败");
    }

    @Override
    public Result<PageInfo<TemperatureLogs>> getByMoldId(String moldId, int pageNum, int pageSize) {
        if (moldId == null || moldId.trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<TemperatureLogs> pageInfo = new PageInfo<>(temperatureLogMapper.listByMoldId(moldId));
        return Result.success(pageInfo);
    }

    @Override
    public PageInfo<TemperatureLogVO> queryByCondition(TemperatureLogQueryParam param, int pageNum, int pageSize) {
        TemperatureLogQueryParam effective = Optional.ofNullable(param)
                .orElseGet(TemperatureLogQueryParam::new);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(temperatureLogMapper.queryByCondition(effective));
    }

    @Override
    public Result<?> update(TemperatureLogs log) {
        if (log == null || log.getId() == null || log.getId().trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = temperatureLogMapper.update(log);
        return rows > 0 ? Result.success() : Result.fail("更新温度巡检记录失败");
    }

    @Override
    public Result<?> delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        int rows = temperatureLogMapper.deleteById(id);
        return rows > 0 ? Result.success() : Result.fail("删除温度巡检记录失败");
    }
}

