package com.whd.cube.dto;

import lombok.Data;

@Data
public class ScoreDTO {
    private Long competitionId;
    private String eventId;
    private Long roundId;
    private Long userId; // 选手的ID

    // 5次成绩（单位：毫秒，-1代表DNF，-2代表DNS，0代表未录入）
    private Integer value1;
    private Integer value2;
    private Integer value3;
    private Integer value4;
    private Integer value5;
}