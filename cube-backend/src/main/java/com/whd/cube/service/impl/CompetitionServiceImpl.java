package com.whd.cube.service.impl;

import com.whd.cube.entity.Competition;
import com.whd.cube.mapper.CompetitionMapper;
import com.whd.cube.service.CompetitionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 赛事主表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {

    @Override
    public List<Integer> getYearList() {
        return baseMapper.selectDistinctYears();
    }
}
