package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldAbnormalRecordQueryParam;
import com.zjb.mjgl.pojo.entity.MoldAbnormalRecord;
import com.zjb.mjgl.pojo.vo.MoldAbnormalRecordVO;

public interface MoldAbnormalRecordService {

    /**
     * 人工录入异常上报
     */
    Result<String> createManual(MoldAbnormalRecord record);

    /**
     * 按模具 ID 分页查询异常记录（原始实体）
     */
    Result<PageInfo<MoldAbnormalRecord>> getByMoldId(String moldId, int pageNum, int pageSize);

    /**
     * 按条件分页查询（带模具、操作人等信息的 VO）
     */
    PageInfo<MoldAbnormalRecordVO> queryByCondition(MoldAbnormalRecordQueryParam param, int pageNum, int pageSize);

    /**
     * 更新异常记录
     */
    Result<?> update(MoldAbnormalRecord record);

    /**
     * 删除异常记录
     */
    Result<?> delete(String id);
}

