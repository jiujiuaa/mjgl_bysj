package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.dto.BusinessConfigBatchUpdateDTO;
import com.zjb.mjgl.pojo.vo.BusinessConfigItemVO;

import java.util.List;

public interface SystemBusinessConfigService {

    void reloadCache();

    List<BusinessConfigItemVO> listAllForAdmin();

    void updateBatch(BusinessConfigBatchUpdateDTO dto);

    String getEffectiveString(String key);

    int getEffectiveInt(String key);

    long getEffectiveLong(String key);

    boolean isWebSocketOriginAllowed(String originHeader);

    String getEffectiveCron(String key);
}
