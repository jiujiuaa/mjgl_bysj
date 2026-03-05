package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.mapper.FilesMapper;
import com.zjb.mjgl.mapper.MoldQrcodesMapper;
import com.zjb.mjgl.mapper.MoldSpecsMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.entity.Files;
import com.zjb.mjgl.pojo.entity.MoldQrcodes;
import com.zjb.mjgl.pojo.entity.MoldSpecs;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.service.MoldService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.MinioUtil;
import com.zjb.mjgl.utils.QrcodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模具服务类
 */
@Slf4j
@Service
public class MoldServiceImpl implements MoldService {

    @Autowired
    private MoldsMapper moldsMapper;
    @Autowired
    private MoldSpecsMapper moldSpecsMapper;
    @Autowired
    private MoldQrcodesMapper moldQrcodesMapper;
    @Autowired
    private FilesMapper fileMapper;
    @Autowired
    private MinioUtil minioUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoldDetailVO createMold(MoldParam param) {
        Date now = new Date();
        String moldId = IdUtil.fastUUID();

        moldsMapper.insertMold(paramToMoldForCreate(param, moldId, now));
        moldSpecsMapper.insertSpecs(paramToSpecs(param, moldId));
        Optional.ofNullable(paramToQrcode(param, moldId, now)).ifPresent(moldQrcodesMapper::insertQrcode);
        return moldsMapper.selectDetailById(moldId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMold(MoldParam param) {
        Date now = new Date();
        Molds mold = paramToMoldForUpdate(param, now);

        moldsMapper.updateMold(mold);
        moldSpecsMapper.updateSpecsByMoldId(paramToSpecs(param, mold.getId()));
        moldQrcodesMapper.deleteByMoldId(mold.getId());
        Optional.ofNullable(paramToQrcode(param, mold.getId(), now)).ifPresent(moldQrcodesMapper::insertQrcode);
    }

    private Molds paramToMoldForCreate(MoldParam param, String moldId, Date now) {
        Molds m = new Molds();
        BeanUtils.copyProperties(param, m,
                "id", "totalUsageCount", "totalProductionTime", "lastMaintenanceDate",
                "totalRepairCost", "totalMaintenanceCost", "createdAt", "updatedAt", "specs", "qrcodeType");
        m.setId(moldId);
        m.setCurrentStatus(Optional.ofNullable(m.getCurrentStatus()).orElse(1));
        m.setTotalUsageCount(0);
        m.setTotalProductionTime(0.0);
        m.setLastMaintenanceDate(null);
        m.setTotalRepairCost(0.0);
        m.setTotalMaintenanceCost(0.0);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }

    private Molds paramToMoldForUpdate(MoldParam param, Date now) {
        Molds m = new Molds();
        BeanUtils.copyProperties(param, m,
                "totalUsageCount", "totalProductionTime", "lastMaintenanceDate",
                "totalRepairCost", "totalMaintenanceCost", "createdAt", "updatedAt", "specs", "qrcodeType");
        m.setUpdatedAt(LocalDateTime.now());
        return m;
    }

    private MoldSpecs paramToSpecs(MoldParam param, String moldId) {
        MoldSpecs s = Optional.ofNullable(param.getSpecs()).orElseGet(MoldSpecs::new);
        s.setMoldId(moldId);
        if (s.getId() == null) {
            s.setId(IdUtil.fastUUID());
        }
        return s;
    }

    /** 一对一：从参数生成一条二维码记录，无类型则返回 null */
    private MoldQrcodes paramToQrcode(MoldParam param, String moldId, Date now) {
        if (param.getQrcodeType() == null) {
            return null;
        }
        MoldQrcodes q = new MoldQrcodes();
        q.setId(QrcodeUtil.generateMoldQrcodeId(moldId));
        q.setMoldId(moldId);
        q.setQrcodeType(param.getQrcodeType());
        q.setIsActive(1);
        q.setCreatedAt(LocalDateTime.now());
        return q;
    }

    @Override
    public void deteleMold(String id) {
        moldsMapper.deleteById(id);
        moldQrcodesMapper.deleteByMoldId(id);
        moldSpecsMapper.deleteById(id);
        //TODO需要把其他表里面的相关记录删除
        List<Files> files = fileMapper.selectByMoldId(id);
        List<String> objects = files.stream().map(Files::getFilePath).collect(Collectors.toList());
        if(!objects.isEmpty()) {
            minioUtil.deleteFiles(objects);
        }
        fileMapper.deleteByMoldId(id);

    }

    @Override
    public PageInfo<MoldDetailVO> listAllAsDetail(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(moldsMapper.listAllAsDetail());
    }

    @Override
    public PageInfo<MoldDetailVO> queryByCondition(MoldQueryParam param, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(moldsMapper.selectDetailByCondition(param));
    }
}
