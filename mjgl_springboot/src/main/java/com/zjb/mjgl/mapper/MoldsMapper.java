package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.MoldMetaVO;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MoldsMapper {

    int insertMold(Molds mold);

    int updateMold(Molds mold);

    void deleteById(String id);

    /**
     * 分页用：列表全部模具为详情 VO（specs/qrcode/files 由 resultMap 嵌套查询）
     */
    List<MoldDetailVO> listAllAsDetail();

    /**
     * 按 id 查询单条模具详情（创建/更新后返回用）
     */
    MoldDetailVO selectDetailById(String id);

    /**
     * 根据条件查询模具详情列表（配合 PageHelper 分页）
     */
    List<MoldDetailVO> selectDetailByCondition(MoldQueryParam param);

    int getStatus(String id);

    int updateStatus(@Param("id") String id, @Param("status") int status);

    Molds selectById(String id);

    /**
     * 健康评估/统计场景：只返回模具的基础信息，避免加载 specs/qrcode/files 造成性能问题。
     */
    List<MoldMetaVO> listMeta();
}

