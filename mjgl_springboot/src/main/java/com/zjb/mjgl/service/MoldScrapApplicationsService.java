package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldScrapApplicationDTO;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationDetailVO;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationVO;

import java.util.List;

public interface MoldScrapApplicationsService {
    Result<String> create(MoldScrapApplicationDTO dto);

    Result<?> approve(String id, Integer approvalStatus, String comment);

    Result<?> execute(String id, String handlerComment);

    Result<MoldScrapApplicationDetailVO> getDetail(String id);

    Result<List<MoldScrapApplicationVO>> listByCondition(String moldId, Integer status);
}

