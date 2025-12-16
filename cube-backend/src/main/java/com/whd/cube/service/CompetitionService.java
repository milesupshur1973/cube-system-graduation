package com.whd.cube.service;

import com.whd.cube.entity.Competition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 赛事主表 服务类
 * </p>
 *
 * @author WHD
 */
public interface CompetitionService extends IService<Competition> {

    // 获取存在比赛的年份列表
    List<Integer> getYearList();
}
