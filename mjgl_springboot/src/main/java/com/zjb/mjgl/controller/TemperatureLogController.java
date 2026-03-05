package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.TemperatureLogQueryParam;
import com.zjb.mjgl.pojo.entity.TemperatureLogs;
import com.zjb.mjgl.pojo.vo.TemperatureLogVO;
import com.zjb.mjgl.service.TemperatureLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/temperature-log")
@RequiredArgsConstructor
public class TemperatureLogController {

    private final TemperatureLogService temperatureLogService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody TemperatureLogs record) {
        if (record == null || record.getMoldId() == null) {
            log.warn("创建温度巡检记录失败, 请求体为空或模具ID缺失");
            return Result.fail("模具ID不能为空");
        }
        log.info("收到创建温度巡检记录请求, moldId={}", record.getMoldId());
        return temperatureLogService.create(record);
    }

    @PostMapping("/query")
    public Result<PageInfo<TemperatureLogVO>> query(@RequestBody(required = false) TemperatureLogQueryParam param,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        TemperatureLogQueryParam effective = Optional.ofNullable(param)
                .orElseGet(TemperatureLogQueryParam::new);
        return Result.success(temperatureLogService.queryByCondition(effective, pageNum, pageSize));
    }

    @GetMapping("/mold/{moldId}")
    public Result<PageInfo<TemperatureLogs>> listByMold(@PathVariable String moldId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return temperatureLogService.getByMoldId(moldId, pageNum, pageSize);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody TemperatureLogs log) {
        return temperatureLogService.update(log);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable String id) {
        return temperatureLogService.delete(id);
    }
}

