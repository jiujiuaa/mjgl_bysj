package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.MoldSpecs;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MoldSpecsMapper {

    int insertSpecs(MoldSpecs specs);

    /**
     * 根据 moldId 更新技术参数
     */
    int updateSpecsByMoldId(MoldSpecs specs);

    int deleteById(String id);

    /**
     * 根据模具 ID 查询技术参数（一对一）
     */
    MoldSpecs selectByMoldId(String moldId);
}

