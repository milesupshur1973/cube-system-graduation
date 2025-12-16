package com.whd.cube.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.entity.Registration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whd.cube.vo.CompetitorVO;
import com.whd.cube.vo.MyRegistrationVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 选手报名记录表 Mapper 接口
 * </p>
 *
 * @author WHD
 */
public interface RegistrationMapper extends BaseMapper<Registration> {

    //查询一场比赛中选手的列表
    IPage<CompetitorVO> selectCompetitorList(Page<?> page, @Param("competitionId") Long competitionId);

    // 查询某个用户的报名列表
    List<MyRegistrationVO> selectMyRegistrations(@Param("userId") Long userId);
}
