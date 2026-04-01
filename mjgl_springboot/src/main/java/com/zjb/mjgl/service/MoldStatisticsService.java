package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldStatisticsQueryParam;
import com.zjb.mjgl.pojo.dto.MoldTrendsQueryParam;
import com.zjb.mjgl.pojo.vo.MoldStatVO;
import com.zjb.mjgl.pojo.vo.MoldTrendsResponseVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MoldStatisticsService {

    Result<List<MoldStatVO>> queryMoldStats(MoldStatisticsQueryParam param);

    Result<MoldTrendsResponseVO> queryTrends(MoldTrendsQueryParam param);

    ResponseEntity<byte[]> exportMoldStatsCsv(MoldStatisticsQueryParam param);

    ResponseEntity<byte[]> exportMoldStatsXlsx(MoldStatisticsQueryParam param);
}

