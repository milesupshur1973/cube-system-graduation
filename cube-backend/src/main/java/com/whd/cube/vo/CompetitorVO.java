package com.whd.cube.vo;

import lombok.Data;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class CompetitorVO {
    private Long id; // 用户主键ID
    private String name;        // 姓名
    private String gender;      // 性别
    private String province;    // 地区
    private String displayId;   // WCAID/展示ID

    // 数据库查出来的原始字符串，例如 "333,333oh,444"
    private String eventIdsStr;

    // 给前端用的列表，自动根据 eventIdsStr 生成
    private List<String> eventIds;

    // MyBatis 查出 eventIdsStr 后会自动调用这个 setter
    public void setEventIdsStr(String eventIdsStr) {
        this.eventIdsStr = eventIdsStr;
        if (eventIdsStr != null && !eventIdsStr.isEmpty()) {
            this.eventIds = Arrays.asList(eventIdsStr.split(","));
        } else {
            this.eventIds = Collections.emptyList();
        }
    }
}