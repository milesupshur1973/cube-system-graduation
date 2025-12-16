package com.whd.cube.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MyRegistrationVO {
    private Long registrationId;    // 报名记录ID
    private Long competitionId;     // 比赛ID
    private String competitionName; // 比赛名称
    private String slug;            // 比赛代号(用于跳转)
    private String location;        // 比赛地点
    private LocalDate startDate;    // 比赛开始日期
    private LocalDate endDate;      // 比赛结束日期

    // 状态：0-待审核, 1-已通过, 2-已取消 (对应 Registration.status)
    private Byte status;

    private LocalDateTime createTime; // 报名时间

    // 报名的项目名称拼接字符串，例如 "三阶, 二阶, 单手"
    private String eventNames;
}