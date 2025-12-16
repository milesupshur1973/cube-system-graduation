package com.whd.cube.mapper;

import com.whd.cube.entity.Competition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 赛事主表 Mapper 接口
 * </p>
 *
 * @author WHD
 */
public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("SELECT DISTINCT YEAR(start_date) FROM competition ORDER BY YEAR(start_date) DESC")
    List<Integer> selectDistinctYears();

    @Select("SELECT * FROM competition WHERE id = #{id} FOR UPDATE")
    Competition selectByIdForUpdate(Long id);
}
