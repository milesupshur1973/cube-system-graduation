package com.whd.cube.vo;

import com.whd.cube.entity.MatchResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionResultVO extends MatchResult {
    // 补充 MatchResult 里没有的选手信息字段
    private String userName;
    private String userProvince;
    private String userGender;
}