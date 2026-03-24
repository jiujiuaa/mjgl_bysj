package com.zjb.mjgl.pojo.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 批量操作：请求体 {@code {"ids":["a","b"]}}，空值与重复 id 会在规范化时剔除。
 */
@Data
public class BatchIdsDTO {
    private List<String> ids;

    public static List<String> normalizeList(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
