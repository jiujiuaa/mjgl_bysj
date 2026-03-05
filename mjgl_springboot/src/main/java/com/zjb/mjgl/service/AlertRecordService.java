package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.AlertRecordQueryParam;
import com.zjb.mjgl.pojo.entity.AlertRecord;
import com.zjb.mjgl.pojo.vo.AlertRecordVO;

/**
 * 报警记录服务：闭环管理（活跃/已解决/已忽略）
 */
public interface AlertRecordService {

    /**
     * 分页条件查询报警记录
     */
    PageInfo<AlertRecordVO> queryByCondition(AlertRecordQueryParam param, int pageNum, int pageSize);

    /**
     * 根据ID查询单条
     */
    Result<AlertRecordVO> getById(String id);

    /**
     * 将报警标记为「已解决」
     *
     * @param id     报警ID
     * @param remark 处理备注（可选）
     */
    Result<Void> resolve(String id, String remark);

    /**
     * 将报警标记为「已忽略」
     *
     * @param id     报警ID
     * @param remark 忽略备注（可选）
     */
    Result<Void> ignore(String id, String remark);
}
