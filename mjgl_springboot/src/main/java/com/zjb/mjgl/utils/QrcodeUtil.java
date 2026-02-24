package com.zjb.mjgl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 二维码相关工具类。
 * <p>
 * 当前实现：根据模具 ID 生成唯一的二维码 ID（作为二维码内容使用）。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QrcodeUtil {

    /**
     * 根据模具 ID 生成一个全局唯一的二维码标识。
     * 规则：moldId + "-" + fastUUID
     *
     * @param moldId 模具 ID（必填）
     * @return 用于二维码内容的唯一字符串
     */
    public static String generateMoldQrcodeId(String moldId) {
        if (moldId == null || moldId.trim().isEmpty()) {
            throw new IllegalArgumentException("moldId cannot be null or empty");
        }
        return moldId + "-" + IdUtil.fastUUID();
    }
}

