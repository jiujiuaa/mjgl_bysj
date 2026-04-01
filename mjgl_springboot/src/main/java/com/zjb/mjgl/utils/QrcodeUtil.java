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
     * 生成用于二维码内容的唯一标识（写入 `mold_qrcodes.id`）。
     * <p>
     * 数据库表 `mold_qrcodes.id` 为 `char(32)`，必须保证长度恒为 32 位，
     * 否则会触发 MySQL: Data too long for column 'id'。
     *
     * @param moldId 模具 ID（当前仅用于校验非空；实际 id 直接使用 UUID32）
     * @return 用于二维码内容的唯一字符串
     */
    public static String generateMoldQrcodeId(String moldId) {
        if (moldId == null || moldId.trim().isEmpty()) {
            throw new IllegalArgumentException("moldId cannot be null or empty");
        }
        // 直接使用 32 位 UUID，避免拼接 moldId 导致超长（moldId(32) + '-' + UUID(32)）
        return IdUtil.fastUUID();
    }
}

