package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whd.cube.common.Result;
import com.whd.cube.entity.CompetitionRound;
import com.whd.cube.service.CompetitionRoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 赛事轮次配置表 前端控制器
 * </p>
 *
 * @author whd
 * @since 2026-01-xx
 */
@RestController
@RequestMapping("/competition-round")
public class CompetitionRoundController {

    @Autowired
    private CompetitionRoundService competitionRoundService;

    /**
     * 根据比赛ID和项目ID，获取轮次列表
     * 对应前端请求：GET /competition-round/list?competitionId=18&eventId=333
     */
    @GetMapping("/list")
    public Result<List<CompetitionRound>> list(@RequestParam Long competitionId,
                                               @RequestParam String eventId) {

        // 构造查询条件
        LambdaQueryWrapper<CompetitionRound> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompetitionRound::getCompetitionId, competitionId); // 匹配比赛
        queryWrapper.eq(CompetitionRound::getEventId, eventId);             // 匹配项目
        queryWrapper.orderByAsc(CompetitionRound::getRoundOrder);           // 按轮次顺序排序 (初赛->复赛->决赛)

        // 查询数据库
        List<CompetitionRound> list = competitionRoundService.list(queryWrapper);

        return Result.success(list);
    }
}