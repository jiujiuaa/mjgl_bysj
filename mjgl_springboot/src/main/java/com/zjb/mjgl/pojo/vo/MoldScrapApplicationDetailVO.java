package com.zjb.mjgl.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class MoldScrapApplicationDetailVO {
    private MoldScrapApplicationVO application;
    private List<MoldScrapTimelineItemVO> timeline;
}

