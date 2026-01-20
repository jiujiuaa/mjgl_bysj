package com.zjb.mjgl.utils;



import java.util.UUID;

/**
 * ID 工具类：生成 32 位无连字符的 UUID（如：550e8400e29b41d4a716446655440000）
 */
public final class IdUtil {

    private IdUtil() {
        // 私有构造，防止实例化
    }

    /**
     * 生成 32 位小写 UUID（不含连字符）
     * @return 32位字符串，例如 "550e8400e29b41d4a716446655440000"
     */
    public static String fastUUID() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 生成 32 位大写 UUID（不含连字符）
     * @return 32位字符串，例如 "550E8400E29B41D4A716446655440000"
     */
    public static String fastUUIDUpper() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 示例与测试
     */
    public static void main(String[] args) {
        System.out.println("32位小写UUID: " + fastUUID());
        System.out.println("32位大写UUID: " + fastUUIDUpper());
        // 输出示例：
        // 32位小写UUID: a1b2c3d4e5f678901234567890abcdef
        // 32位大写UUID: A1B2C3D4E5F678901234567890ABCDEF
    }
}