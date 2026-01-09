package com.whd.cube.controller;

import com.whd.cube.common.Result;
import com.whd.cube.dto.ScoreDTO;
import com.whd.cube.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                              @RequestParam String eventId,
                              @RequestParam(required = false) Long roundId) {

        // 如果前端没传 roundId (比如旧页面)，我们可以先临时查一下第一轮，或者报错
        // 建议前端一定要传。这里为了健壮性，如果没传 roundId，就查该项目下的所有成绩
        return Result.success(resultService.getCompetitionDetails(competitionId, eventId, roundId));
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

    /**
     * 作用：把“报名成功”的选手全部拉入“第1轮”的成绩表中（创建一个空成绩占位）
     */
    @PostMapping("/init-round1")
    public Result initRound1(@RequestBody Map<String, Object> params) {
        Long competitionId = Long.valueOf(params.get("competitionId").toString());
        String eventId = (String) params.get("eventId");
        Long roundId = Long.valueOf(params.get("roundId").toString()); // 第一轮的 roundId

        resultService.initFirstRound(competitionId, eventId, roundId);
        return Result.success("选手名单初始化成功");
    }

    /**
     * 作用：根据当前轮次的排名，取前 N 名，复制到下一轮
     */
    @PostMapping("/promote")
    public Result promote(@RequestBody Map<String, Object> params) {
        Long competitionId = Long.valueOf(params.get("competitionId").toString());
        String eventId = (String) params.get("eventId");
        Long currentRoundId = Long.valueOf(params.get("currentRoundId").toString());
        Long nextRoundId = Long.valueOf(params.get("nextRoundId").toString());
        Integer topN = Integer.valueOf(params.get("topN").toString()); // 前端直接告诉我晋级多少人

        resultService.promoteCompetitors(competitionId, eventId, currentRoundId, nextRoundId, topN);
        return Result.success("晋级成功，" + topN + " 位选手已进入下一轮");
    }
}
