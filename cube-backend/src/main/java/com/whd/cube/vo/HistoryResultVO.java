package com.whd.cube.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class HistoryResultVO {
    // 基础成绩信息
    private Integer value1;
    private Integer value2;
    private Integer value3;
    private Integer value4;
    private Integer value5;
    private Integer best;
    private Integer average;

    // 辅助信息
    private String roundType; // 轮次 (Final/First)

    // 关联信息
    private String eventName;       // 项目名称 (如 "三阶魔方")
    private String competitionName; // 比赛名称
    private String competitionSlug; // 用于跳转
    private LocalDate competitionDate; // 用于排序
}