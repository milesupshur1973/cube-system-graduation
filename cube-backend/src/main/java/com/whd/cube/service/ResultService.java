package com.whd.cube.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whd.cube.dto.ScoreDTO;
import com.whd.cube.entity.MatchResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whd.cube.vo.CompetitionResultVO;
import com.whd.cube.vo.HistoryResultVO;
import com.whd.cube.vo.PersonalBestVO;
import com.whd.cube.vo.RankingVO;

import java.util.List;

/**
 * <p>
 * 比赛成绩表 服务类
 * </p>
 *
 * @author WHD
 */
public interface ResultService extends IService<MatchResult> {

    // 获取全站排名
    IPage<RankingVO> getRankings(Integer page, Integer size, String eventId, String type, String region);

    // 查询指定选手的个人最佳
    List<PersonalBestVO> getPersonalBests(String displayId);

    // 保存/更新成绩
    boolean saveScore(ScoreDTO dto);

    List<CompetitionResultVO> getCompetitionDetails(Long competitionId, String eventId);

    List<HistoryResultVO> getHistoryResults(String displayId);
}
