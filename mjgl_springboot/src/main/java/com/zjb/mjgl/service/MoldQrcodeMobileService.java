package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldQrcodeMobileResolveRequest;

/**
 * 移动端二维码交互服务（resolve 分发）。
 */
public interface MoldQrcodeMobileService {
    Result<?> resolve(MoldQrcodeMobileResolveRequest request);
}

