package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.whd.cube.common.Result;
import com.whd.cube.entity.CompetitionEvent;
import com.whd.cube.service.CompetitionEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 赛事开设项目表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/competition-event")
public class CompetitionEventController {

    @Autowired
    private CompetitionEventService competitionEventService;

    @GetMapping("/list/{competitionId}")
    public Result list(@PathVariable Long competitionId) {
        QueryWrapper<CompetitionEvent> query = new QueryWrapper<>();
        query.eq("competition_id", competitionId);
        return Result.success(competitionEventService.list(query));
    }

}
