package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;

import java.util.List;

public interface MoldService {

    /**
     * 一次性创建模具：主表 + 技术参数 + 二维码
     */
    MoldDetailVO createMold(MoldParam param);

    /**
     * 更新模具基础信息 + 技术参数 + 二维码
     */
    void updateMold(MoldParam param);

    void deteleMold(String id);

    void deleteMoldsBatch(List<String> ids);

    /**
     * 分页查询所有模具（主表 + specs + 一个 qrcode + files）
     */
    PageInfo<MoldDetailVO> listAllAsDetail(int pageNum, int pageSize);

    PageInfo<MoldDetailVO> queryByCondition(MoldQueryParam param,Integer pageNum, Integer pageSize);
}
