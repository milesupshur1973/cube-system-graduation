package com.whd.cube.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RankingVO {
    private Integer rank;           // 排名 (1, 2, 3...)
    private String userName;        // 选手姓名
    private String userDisplayId;   // WCAID
    private String userProvince;    // 选手省份
    private Integer bestScore;      // 成绩 (毫秒)
    private String competitionName; // 在哪场比赛创造的
    private String competitionSlug; // 用于跳转
    private LocalDate competitionDate; // 比赛日期
}