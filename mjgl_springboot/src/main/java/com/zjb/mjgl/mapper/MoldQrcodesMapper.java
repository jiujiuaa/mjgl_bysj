package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.MoldQrcodes;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MoldQrcodesMapper {

    int insertQrcode(MoldQrcodes qrcode);

    int deleteByMoldId(String moldId);

    /**
     * 根据模具 ID 查询关联二维码列表（一个模具对应一个或多个类型的二维码）
     */
    List<MoldQrcodes> selectByMoldId(String moldId);
}

