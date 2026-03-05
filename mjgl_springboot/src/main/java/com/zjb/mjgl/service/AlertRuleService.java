package com.zjb.mjgl.service;

import com.zjb.mjgl.pojo.dto.AlertRuleSaveParam;
import com.zjb.mjgl.pojo.entity.AlertRule;
import com.zjb.mjgl.pojo.vo.AlertRuleVO;

import java.util.List;

/**
 * 智能预警规则：增删改查、启用/禁用、初始化默认规则
 */
public interface AlertRuleService {

    List<AlertRuleVO> listAll();

    AlertRuleVO getById(String id);

    String save(AlertRuleSaveParam param);

    void deleteById(String id);

    void setEnabled(String id, Integer enabled);

    /** 供规则引擎调用：仅返回启用的规则，按 sort_order 排序 */
    List<AlertRule> listEnabledForEngine();

    /** 初始化默认规则（若对应 code 不存在则插入） */
    int initDefaults();
}
