package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.LubricationLogQueryParam;
import com.zjb.mjgl.pojo.entity.LubricationLogs;
import com.zjb.mjgl.pojo.vo.LubricationLogVO;

public interface LubricationLogService {

    /**
     * 创建润滑巡检记录，返回新记录ID
     */
    Result<String> create(LubricationLogs log);

    /**
     * 按模具ID分页查询润滑记录（原始实体）
     */
    Result<PageInfo<LubricationLogs>> getByMoldId(String moldId, int pageNum, int pageSize);

    /**
     * 条件分页查询润滑记录（带模具、操作人名称）
     */
    PageInfo<LubricationLogVO> queryByCondition(LubricationLogQueryParam param, int pageNum, int pageSize);

    Result<?> update(LubricationLogs log);

    Result<?> delete(String id);
}

