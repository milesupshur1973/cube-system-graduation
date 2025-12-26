package com.whd.cube.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whd.cube.dto.RegistrationDTO;
import com.whd.cube.entity.Registration;
import com.baomidou.mybatisplus.extension.service.IService;
import com.whd.cube.vo.CompetitorVO;
import com.whd.cube.vo.MyRegistrationVO;

import java.util.List;

/**
 * <p>
 * 选手报名记录表 服务类
 * </p>
 *
 * @author WHD
 */
public interface RegistrationService extends IService<Registration> {

    // 获取某比赛的选手列表
    IPage<CompetitorVO> getCompetitorList(Long competitionId, Integer page, Integer size);

    // 提交报名
    boolean submitRegistration(RegistrationDTO dto, Long userId);

    // 获取我当前的报名信信息
    RegistrationDTO getMyRegistration(Long competitionId, Long userId);

    // 获取我的所有报名列表
    List<MyRegistrationVO> getMyRegistrationList(Long userId);
    
    // 取消报名
    void cancelRegistration(Long competitionId, Long userId);
}
