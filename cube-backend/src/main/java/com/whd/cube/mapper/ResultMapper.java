package com.whd.cube.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.entity.MatchResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whd.cube.vo.CompetitionResultVO;
import com.whd.cube.vo.HistoryResultVO;
import com.whd.cube.vo.PersonalBestVO;
import com.whd.cube.vo.RankingVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 比赛成绩表 Mapper 接口
 * </p>
 *
 * @author WHD
 */
public interface ResultMapper extends BaseMapper<MatchResult> {

    /**
     * 查询某项目的排名 (支持分页)
     */
    IPage<RankingVO> selectGlobalRankings(Page<?> page,
                                          @Param("eventId") String eventId,
                                          @Param("type") String type,
                                          @Param("region") String region);

    // 查询指定选手的各项 PB
    List<PersonalBestVO> selectPersonalBests(@Param("displayId") String displayId);

    // 查询某场比赛的详细成绩（带选手信息）
    List<CompetitionResultVO> selectCompetitionDetails(
            @Param("competitionId") Long competitionId,
            @Param("eventId") String eventId,
            @Param("roundId") Long roundId // 新增
    );

    // 查询历史成绩
    List<HistoryResultVO> selectHistoryResults(@Param("displayId") String displayId);

    /**
     * 批量初始化第一轮 (从报名表导入)
     */
    void batchInitRound1(@Param("competitionId") Long competitionId,
                         @Param("eventId") String eventId,
                         @Param("roundId") Long roundId);

    /**
     * 批量晋级 (从上一轮前N名导入)
     */
    void batchPromote(@Param("competitionId") Long competitionId,
                      @Param("eventId") String eventId,
                      @Param("nextRoundId") Long nextRoundId,
                      @Param("currentRoundId") Long currentRoundId,
                      @Param("limit") Integer limit);
}
