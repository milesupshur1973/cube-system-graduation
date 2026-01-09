package com.whd.cube.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompetitionApplyDTO {

    // --- 比赛基本信息 ---
    private Long id; // 修改时会用到
    private String name;
    private String slug;
    private String province;
    private String city;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contentDescription; // 详情
    private String contentRule;        // 规则
    private LocalDateTime regStartTime;
    private LocalDateTime regEndTime;
    private Integer competitorLimit;

    // --- 核心变化：项目与轮次配置 ---
    // 以前这里是 List<String> eventIds;
    // 现在我们要改成更复杂的结构
    private List<EventConfigDTO> events;

    /**
     * 内部类：单项配置
     * 对应前端的一个项目块 (e.g. 三阶魔方)
     */
    @Data
    public static class EventConfigDTO {
        private String eventId; // "333"

        // 该项目下的轮次列表
        private List<RoundConfigDTO> rounds;
    }

    /**
     * 内部类：轮次配置
     * 对应前端的一行轮次 (e.g. 初赛 - Top 12)
     */
    @Data
    public static class RoundConfigDTO {
        private Integer roundOrder;      // 1, 2, 3
        private String roundName;        // "初赛", "决赛"
        private String advancementRule;  // "TOP-12"
        // format 字段不需要传，因为是后端根据 eventId 自动查出来的
    }
}