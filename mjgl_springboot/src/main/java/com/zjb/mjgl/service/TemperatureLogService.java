package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.TemperatureLogQueryParam;
import com.zjb.mjgl.pojo.entity.TemperatureLogs;
import com.zjb.mjgl.pojo.vo.TemperatureLogVO;

public interface TemperatureLogService {

    /**
     * 创建温度巡检记录，返回新记录ID
     */
    Result<String> create(TemperatureLogs log);

    /**
     * 按模具ID分页查询温度记录（原始实体）
     */
    Result<PageInfo<TemperatureLogs>> getByMoldId(String moldId, int pageNum, int pageSize);

    /**
     * 条件分页查询温度记录（带模具、操作人名称）
     */
    PageInfo<TemperatureLogVO> queryByCondition(TemperatureLogQueryParam param, int pageNum, int pageSize);

    Result<?> update(TemperatureLogs log);

    Result<?> delete(String id);
}

