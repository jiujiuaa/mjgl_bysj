package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.SystemBusinessConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemBusinessConfigMapper {

    List<SystemBusinessConfig> selectAllOrdered();

    SystemBusinessConfig selectByKey(@Param("configKey") String configKey);

    int insert(SystemBusinessConfig row);

    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
