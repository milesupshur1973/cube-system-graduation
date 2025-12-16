package com.whd.cube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whd.cube.common.UserContext;
import com.whd.cube.dto.ScoreDTO;
import com.whd.cube.entity.Competition;
import com.whd.cube.entity.Event;
import com.whd.cube.entity.MatchResult;
import com.whd.cube.entity.User;
import com.whd.cube.mapper.CompetitionMapper;
import com.whd.cube.mapper.EventMapper;
import com.whd.cube.mapper.ResultMapper;
import com.whd.cube.mapper.UserMapper;
import com.whd.cube.service.RegistrationItemService;
import com.whd.cube.service.RegistrationService;
import com.whd.cube.service.ResultService;
import com.whd.cube.vo.CompetitionResultVO;
import com.whd.cube.vo.HistoryResultVO;
import com.whd.cube.vo.PersonalBestVO;
import com.whd.cube.vo.RankingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 比赛成绩表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class ResultServiceImpl extends ServiceImpl<ResultMapper, MatchResult> implements ResultService {

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationItemService registrationItemService;

    @Override
    public IPage<RankingVO> getRankings(Integer page, Integer size, String eventId, String type, String region) {
        // 1. 构建分页对象
        Page<RankingVO> pageInfo = new Page<>(page, size);

        // 2. 调用 Mapper (会自动执行分页 SQL)
        IPage<RankingVO> resultPage = baseMapper.selectGlobalRankings(pageInfo, eventId, type, region);

        // 3. 重新计算排名 (Rank)
        // 分页后的排名 = (当前页-1) * 每页条数 + 当前列表索引 + 1
        long startRank = (page - 1) * (long)size;
        List<RankingVO> records = resultPage.getRecords();
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setRank((int) (startRank + i + 1));
        }

        return resultPage;
    }

    @Override
    public List<PersonalBestVO> getPersonalBests(String displayId) {
        return baseMapper.selectPersonalBests(displayId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveScore(ScoreDTO dto) {
        // 1. 权限校验：必须是主办方
        Long currentUserId = UserContext.getUserId();
        Competition comp = competitionMapper.selectById(dto.getCompetitionId());
        if (comp == null) {
            throw new RuntimeException("赛事不存在");
        }

        // 如果比赛已经结束 (status = 3)，禁止再修改成绩
        if (comp.getStatus() == 3) {
            throw new RuntimeException("比赛已结束并归档，无法再修改成绩！");
        }

        // ★★★ 核心鉴权：判断当前登录用户是否为该比赛的主办方 ★★★
        if (!comp.getOrganizerId().equals(currentUserId)) {
            // 如果不是主办方，再看看是不是管理员(可选，这里严格点只允许主办方)
            // 简单起见，我们假设只有主办方能录
            throw new RuntimeException("无权操作：您不是本场比赛的主办方");
        }

        Event event = eventMapper.selectById(dto.getEventId());
        if (event == null) {
            throw new RuntimeException("该项目不存在");
        }
        String format = event.getFormat();

        // 1. 检查选手是否报名了这场比赛 (且状态为通过)
        QueryWrapper<com.whd.cube.entity.Registration> regQuery = new QueryWrapper<>();
        regQuery.eq("competition_id", dto.getCompetitionId());
        regQuery.eq("user_id", dto.getUserId());
        regQuery.eq("status", 1); // 必须是报名成功的
        com.whd.cube.entity.Registration reg = registrationService.getOne(regQuery);

        if (reg == null) {
            throw new RuntimeException("该选手未报名本场比赛或报名未通过");
        }

        // 2. 检查报名明细中是否包含当前项目
        QueryWrapper<com.whd.cube.entity.RegistrationItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("registration_id", reg.getId());
        itemQuery.eq("event_id", dto.getEventId());

        if (registrationItemService.count(itemQuery) == 0) {
            throw new RuntimeException("该选手未报名 [" + event.getName() + "] 项目，无法录入成绩");
        }

        // 2. 查找是否已有成绩记录 (如果有就更新，没有就新增)
        QueryWrapper<MatchResult> wrapper = new QueryWrapper<>();
        wrapper.eq("competition_id", dto.getCompetitionId());
        wrapper.eq("event_id", dto.getEventId());
        wrapper.eq("user_id", dto.getUserId());
        // 简单起见，我们默认只有一轮 "Final"
        wrapper.eq("round_type", "Final");

        MatchResult result = getOne(wrapper);
        if (result == null) {
            result = new MatchResult();
            result.setCompetitionId(dto.getCompetitionId());
            result.setEventId(dto.getEventId());
            result.setUserId(dto.getUserId());
            result.setRoundType("Final");
            // 冗余字段：UserDisplayId
            User competitor = userMapper.selectById(dto.getUserId());
            if (competitor != null) {
                result.setUserDisplayId(competitor.getDisplayId());
            }
        }

        // 3. 设置5次成绩
        // 前端传来的 null 我们转成 0，方便计算
        result.setValue1(dto.getValue1() == null ? 0 : dto.getValue1());
        result.setValue2(dto.getValue2() == null ? 0 : dto.getValue2());
        result.setValue3(dto.getValue3() == null ? 0 : dto.getValue3());
        result.setValue4(dto.getValue4() == null ? 0 : dto.getValue4());
        result.setValue5(dto.getValue5() == null ? 0 : dto.getValue5());

        // 4. 自动计算 Best 和 Average
        calculateStats(result, format);

        // 5. 保存入库
        return saveOrUpdate(result);
    }

    /**
     * 根据 format 计算 Best 和 Average (支持 DNF 逻辑)
     * 约定：-1 代表 DNF, -2 代表 DNS (本次逻辑中视为 DNF)
     */
    private void calculateStats(MatchResult r, String format) {
        List<Integer> values = new ArrayList<>();
        values.add(r.getValue1());
        values.add(r.getValue2());
        values.add(r.getValue3());
        values.add(r.getValue4());
        values.add(r.getValue5());

        // 1. 计算单次最佳 (Best)
        // 逻辑：取所有 > 0 的成绩中的最小值。如果全 <= 0，则 Best = -1 (DNF)
        int min = Integer.MAX_VALUE;
        boolean hasValidScore = false;
        for (Integer v : values) {
            if (v != null && v > 0) {
                min = Math.min(min, v);
                hasValidScore = true;
            }
        }
        r.setBest(hasValidScore ? min : -1);

        // 2. 计算平均 (Average)
        if (format == null) format = "ao5";
        format = format.toLowerCase();

        if ("ao5".equals(format)) {
            // === Ao5 逻辑 ===
            int dnfCount = 0;
            List<Integer> effectiveScores = new ArrayList<>();

            for (Integer v : values) {
                if (v == null || v <= 0) { // DNF 或 DNS
                    dnfCount++;
                    effectiveScores.add(Integer.MAX_VALUE); // DNF 视为无穷大，方便排序
                } else {
                    effectiveScores.add(v);
                }
            }

            // 规则：如果 DNF >= 2，平均成绩为 DNF (-1)
            if (dnfCount >= 2) {
                r.setAverage(-1);
                return;
            }

            // 排序：DNF (MAX_VALUE) 会排在最后
            Collections.sort(effectiveScores);

            // 去掉第1个(最小)和第5个(最大/DNF)，取中间3个
            // 注意：因为只有一个 DNF，它肯定在 index=4，会被当做最慢成绩去掉
            long sum = (long)effectiveScores.get(1) + effectiveScores.get(2) + effectiveScores.get(3);
            r.setAverage((int) Math.round(sum / 3.0));

        } else if ("mo3".equals(format)) {
            // === Mo3 逻辑 (前3次取算术平均) ===
            // 规则：只要有 1 个 DNF，平均就是 DNF
            long sum = 0;
            for (int i = 0; i < 3; i++) {
                Integer v = values.get(i);
                if (v == null || v <= 0) {
                    r.setAverage(-1);
                    return;
                }
                sum += v;
            }
            r.setAverage((int) Math.round(sum / 3.0));

        } else {
            // Bo3 或其他，没有平均成绩
            r.setAverage(0);
        }
    }

    @Override
     public List<CompetitionResultVO> getCompetitionDetails(Long competitionId, String eventId) {
         return baseMapper.selectCompetitionDetails(competitionId, eventId);
     }

    @Override
    public List<HistoryResultVO> getHistoryResults(String displayId) {
        return baseMapper.selectHistoryResults(displayId);
    }
}
