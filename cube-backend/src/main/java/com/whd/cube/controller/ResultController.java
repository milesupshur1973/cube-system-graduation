package com.whd.cube.controller;

import com.whd.cube.common.Result;
import com.whd.cube.dto.ScoreDTO;
import com.whd.cube.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 比赛成绩表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/result")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping("/rankings")
    public Result getRankings(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam String eventId,
                              @RequestParam(defaultValue = "best") String type,
                              @RequestParam(required = false) String region) {

        return Result.success(resultService.getRankings(page, size, eventId, type, region));
    }

    // 获取选手 PB 接口
    @GetMapping("/person/{displayId}/pbs")
    public Result getPersonPBs(@PathVariable String displayId) {
        return Result.success(resultService.getPersonalBests(displayId));
    }

    /**
     * 获取某场比赛、某项目的所有成绩列表 (用于成绩录入页面回显)
     */
    @GetMapping("/list")
    public Result listResults(@RequestParam Long competitionId,
                              @RequestParam(required = false) String eventId) {
        return Result.success(resultService.getCompetitionDetails(competitionId, eventId));
    }

    /**
     * 主办方录入/更新成绩
     * 权限检查在 Service 层实现 (Token -> UserContext -> 比较 organizerId)
     */
    @PostMapping("/save")
    public Result saveScore(@RequestBody ScoreDTO dto) {
        try {
            resultService.saveScore(dto);
            return Result.success("成绩保存成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取选手历史成绩
    @GetMapping("/person/{displayId}/history")
    public Result getPersonHistory(@PathVariable String displayId) {
        return Result.success(resultService.getHistoryResults(displayId));
    }
}
