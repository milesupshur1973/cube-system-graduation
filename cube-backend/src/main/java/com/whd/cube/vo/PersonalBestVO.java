package com.whd.cube.vo;

import lombok.Data;

@Data
public class PersonalBestVO {
    private String eventName; // 项目名称 (如 "三阶魔方")
    private Integer best;     // 单次 PB
    private Integer average;  // 平均 PB
    private String bestCompName;    // 创造单次PB的比赛
    private String averageCompName; // 创造平均PB的比赛
}