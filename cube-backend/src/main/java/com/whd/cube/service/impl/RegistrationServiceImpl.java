package com.whd.cube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.dto.RegistrationDTO;
import com.whd.cube.entity.Competition;
import com.whd.cube.entity.Registration;
import com.whd.cube.entity.RegistrationItem;
import com.whd.cube.entity.User;
import com.whd.cube.mapper.CompetitionMapper;
import com.whd.cube.mapper.RegistrationMapper;
import com.whd.cube.service.RegistrationItemService;
import com.whd.cube.service.RegistrationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whd.cube.service.UserService;
import com.whd.cube.vo.CompetitorVO;
import com.whd.cube.vo.MyRegistrationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 选手报名记录表 服务实现类
 * </p>
 *
 * @author WHD
 */
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Autowired
    private RegistrationItemService registrationItemService;

    @Autowired
    private UserService userService;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Override
    public IPage<CompetitorVO> getCompetitorList(Long competitionId, Integer page, Integer size) {
        // 1. 构建分页对象
        Page<CompetitorVO> pageInfo = new Page<>(page, size);
        // 2. 调用 Mapper
        return baseMapper.selectCompetitorList(pageInfo, competitionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitRegistration(RegistrationDTO dto, Long userId) {
        // 1. 先锁定比赛记录
        Competition comp = competitionMapper.selectByIdForUpdate(dto.getCompetitionId());
        if (comp == null) {
            throw new RuntimeException("比赛不存在");
        }

        // 2. 校验比赛状态
        if (comp.getStatus() != 1) {
            throw new RuntimeException("当前比赛未开启报名通道");
        }

        // 3. 校验报名时间
        LocalDateTime now = LocalDateTime.now();
        if (comp.getRegStartTime() != null && now.isBefore(comp.getRegStartTime())) {
            throw new RuntimeException("报名尚未开始");
        }
        if (comp.getRegEndTime() != null && now.isAfter(comp.getRegEndTime())) {
            throw new RuntimeException("报名已经截止");
        }

        // 4. 查询用户是否已报名
        QueryWrapper<Registration> wrapper = new QueryWrapper<>();
        wrapper.eq("competition_id", dto.getCompetitionId());
        wrapper.eq("user_id", userId);
        Registration reg = getOne(wrapper);

        // 5.新用户报名时，统一检查人数限制
        if (reg == null) {
            // 在事务锁保护下检查人数
            if (comp.getCompetitorLimit() != null && comp.getCompetitorLimit() > 0) {
                // 使用 MyBatis-Plus的count
                long count = count(new QueryWrapper<Registration>()
                        .eq("competition_id", dto.getCompetitionId())
                        .eq("status", 1));

                if (count >= comp.getCompetitorLimit()) {
                    throw new RuntimeException("很抱歉，报名人数已满");
                }
            }

            // 初始化新对象
            reg = new Registration();
            reg.setCompetitionId(dto.getCompetitionId());
            reg.setUserId(userId);
            reg.setStatus((byte) 1);

            User user = userService.getById(userId);
            if (user != null) {
                reg.setUserName(user.getName());
                reg.setUserDisplayId(user.getDisplayId());
            }
            save(reg);
        }

        // 6. 处理报名项目
        QueryWrapper<RegistrationItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("registration_id", reg.getId());
        registrationItemService.remove(itemWrapper);

        if (dto.getEventIds() != null && !dto.getEventIds().isEmpty()) {
            List<RegistrationItem> itemList = new ArrayList<>();
            for (String eventId : dto.getEventIds()) {
                RegistrationItem item = new RegistrationItem();
                item.setRegistrationId(reg.getId());
                item.setEventId(eventId);
                itemList.add(item);
            }
            registrationItemService.saveBatch(itemList);
        }

        return true;
    }

    @Override
    public RegistrationDTO getMyRegistration(Long competitionId, Long userId) {
        // 1. 查主记录
        QueryWrapper<Registration> wrapper = new QueryWrapper<>();
        wrapper.eq("competition_id", competitionId);
        wrapper.eq("user_id", userId);
        Registration reg = getOne(wrapper);

        if (reg == null) return null; // 没报过名

        // 2. 查项目明细
        QueryWrapper<RegistrationItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("registration_id", reg.getId());
        List<RegistrationItem> items = registrationItemService.list(itemWrapper);

        // 3. 组装返回
        RegistrationDTO dto = new RegistrationDTO();
        dto.setCompetitionId(competitionId);
        // 提取 eventId 列表
        List<String> eventIds = items.stream().map(RegistrationItem::getEventId).collect(Collectors.toList());
        dto.setEventIds(eventIds);

        return dto;
    }

    @Override
    public List<MyRegistrationVO> getMyRegistrationList(Long userId) {
        return baseMapper.selectMyRegistrations(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(Long competitionId, Long userId) {
        // 1. 查询用户的报名记录
        QueryWrapper<Registration> wrapper = new QueryWrapper<>();
        wrapper.eq("competition_id", competitionId);
        wrapper.eq("user_id", userId);
        Registration reg = getOne(wrapper);
        
        if (reg == null) {
            throw new RuntimeException("未找到报名记录");
        }
        
        // 2. 更新报名状态为已取消
        reg.setStatus((byte) 2);
        updateById(reg);
        
        // 3. 删除关联的报名项目
        QueryWrapper<RegistrationItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("registration_id", reg.getId());
        registrationItemService.remove(itemWrapper);
    }
}
