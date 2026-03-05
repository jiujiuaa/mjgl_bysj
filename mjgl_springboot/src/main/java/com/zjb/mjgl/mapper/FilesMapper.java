package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FilesMapper {

    int deleteByMoldId(String id);

    /**
     * 根据模具 ID 查询关联文件列表（内部使用 biz_type='mold' + biz_id）
     */
    List<Files> selectByMoldId(String moldId);

    int insertBatch(List<Files> files);

    /**
     * 根据文件ID列表查询文件（用于删除前获取 MinIO 对象名等信息）
     */
    List<Files> selectByIds(@Param("ids")List<String> ids);

    /**
     * 根据文件ID列表批量删除数据库记录
     */
    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 根据单个文件ID查询
     */
    Files selectById(String id);

    /**
     * 通用：根据业务类型 + 业务ID（可选文件类型）查询文件
     */
    List<Files> selectByBiz(@Param("bizType") String bizType,
                            @Param("bizId") String bizId,
                            @Param("fileType") String fileType);
}
