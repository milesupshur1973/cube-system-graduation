package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext;
import com.whd.cube.dto.CompetitionApplyDTO;
import com.whd.cube.entity.Competition;
import com.whd.cube.entity.CompetitionEvent;
import com.whd.cube.entity.CompetitionRound;
import com.whd.cube.entity.User;
import com.whd.cube.service.CompetitionEventService;
import com.whd.cube.service.CompetitionRoundService;
import com.whd.cube.service.CompetitionService;
import com.whd.cube.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 赛事主表 前端控制器
 * </p>
 *
 * @author WHD
 */
@RestController
@RequestMapping("/competition")
@CrossOrigin
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    @Autowired
    private UserService userService; // 注入用户服务，用于检查权限

    @Autowired
    private CompetitionEventService competitionEventService;

    @Autowired
    private CompetitionRoundService competitionRoundService;

    // 首页/列表页用的接口 (只看已发布的)
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String year,      // 新增：年份筛选
                       @RequestParam(required = false) String province, // 新增：省份筛选
                       @RequestParam(required = false) Byte status) {

        // 1. 构建分页对象
        Page<Competition> pageInfo = new Page<>(page, size);

        // 2. 构建查询条件
        QueryWrapper<Competition> wrapper = new QueryWrapper<>();
        wrapper.gt("status", 0); // 只看已发布的(状态>0)

        // 如果有传 status，就用精确查询；否则查所有已发布的
        if (status != null) {
            wrapper.eq("status", status);
        } else {
            wrapper.gt("status", 0); // 默认逻辑不变：只看已发布的
        }

        // 3. 动态拼接筛选条件 (如果有传值，就拼SQL)
        if (StringUtils.hasText(year)) {
            // 假设 startDate 格式是 "2025-12-21"，用 like '2025%' 匹配
            wrapper.like("start_date", year);
        }
        if (StringUtils.hasText(province)) {
            wrapper.eq("province", province);
        }

        wrapper.orderByDesc("start_date");

        // 4. 执行分页查询
        return Result.success(competitionService.page(pageInfo, wrapper));
    }

    /**
     * 管理员获取所有“待审核”的比赛
     */
    @GetMapping("/pending")
    public Result getPending(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = UserContext.getUserId();
        User currentUser = userService.getById(currentUserId);
        if (!"admin".equals(currentUser.getRole())) {
            return Result.error("无权访问：需要管理员权限");
        }

        Page<Competition> pageInfo = new Page<>(page, size);
        QueryWrapper<Competition> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);
        wrapper.orderByDesc("create_time");

        // 执行查询
        Page<Competition> resultPage = competitionService.page(pageInfo, wrapper);

        // ★★★ 新增逻辑：遍历结果，填入主办方名字 ★★★
        for (Competition comp : resultPage.getRecords()) {
            if (comp.getOrganizerId() != null) {
                User u = userService.getById(comp.getOrganizerId());
                if (u != null) {
                    // 把查到的名字塞进去
                    comp.setOrganizerName(u.getName());
                } else {
                    comp.setOrganizerName("未知用户");
                }
            }
        }

        return Result.success(resultPage);
    }

    /**
     * 获取首页“近期赛事”
     */
    @GetMapping("/upcoming")
    public Result upcoming() {
        QueryWrapper<Competition> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.ge("start_date", java.time.LocalDate.now());
        wrapper.orderByAsc("start_date");
        wrapper.last("LIMIT 8");
        return Result.success(competitionService.list(wrapper));
    }

    // 详情页用的接口
    @GetMapping("/slug/{slug}")
    public Result getBySlug(@PathVariable String slug) {
        QueryWrapper<Competition> wrapper = new QueryWrapper<>();
        wrapper.eq("slug", slug);
        Competition comp = competitionService.getOne(wrapper);
        if (comp == null) return Result.error("赛事不存在");
        return Result.success(comp);
    }

    /**
     * 申请比赛 (主办方调用)
     */
    @PostMapping("/apply")
    @Transactional(rollbackFor = Exception.class)
    public Result apply(@RequestBody CompetitionApplyDTO dto) { // ★★★ 参数改为 DTO
        // 1. 基础校验
        if (dto.getName() == null || dto.getSlug() == null) {
            return Result.error("比赛名称和代号不能为空");
        }

        // 2. 校验 DTO 中的复杂项目配置
        if (dto.getEvents() == null || dto.getEvents().isEmpty()) {
            return Result.error("请至少配置一个比赛项目");
        }

        // 3. 查重 Slug (逻辑不变)
        QueryWrapper<Competition> query = new QueryWrapper<>();
        query.eq("slug", dto.getSlug());
        if (competitionService.count(query) > 0) {
            return Result.error("赛事代号(Slug)已存在，请更换一个");
        }

        // 4. DTO 转 Entity 并保存主表
        Competition comp = new Competition();
        BeanUtils.copyProperties(dto, comp); // 复制基本属性
        comp.setStatus((byte)0); // 默认为待审核
        comp.setOrganizerId(UserContext.getUserId());

        competitionService.save(comp); // 保存主表，获取 comp.getId()

        // 5. ★★★ 核心：保存项目和轮次 ★★★
        saveEventsAndRounds(comp.getId(), dto.getEvents());

        return Result.success("申请已提交，等待管理员审核");
    }

    /**
     * 【重写】审核比赛 (管理员调用)
     * POST /audit/{id}?pass=true&msg=...
     */
    @PostMapping("/audit/{id}")
    public Result audit(@PathVariable Long id,
                        @RequestParam boolean pass,
                        @RequestParam(required = false) String msg) {

        // 1. 安全检查：必须是管理员
        Long currentUserId = UserContext.getUserId();
        User currentUser = userService.getById(currentUserId);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            return Result.error("权限不足：只有管理员可以审核比赛");
        }

        // 2. 获取比赛
        Competition comp = competitionService.getById(id);
        if (comp == null) return Result.error("比赛不存在");

        // 3. 执行审核逻辑
        if (pass) {
            comp.setStatus((byte)1); // 1 = 已发布/报名中
            comp.setAuditMsg("审核通过");
        } else {
            comp.setStatus((byte)4); // 4 = 驳回
            comp.setAuditMsg(msg != null ? msg : "信息不完善，请修改后重新提交");
        }

        competitionService.updateById(comp);
        return Result.success(pass ? "已通过审核" : "已驳回申请");
    }

    /**
     * 获取“我主办的”比赛列表
     */
    @GetMapping("/my-organized")
    public Result getMyOrganized() {
        // 1. 获取当前登录用户ID
        Long currentUserId = UserContext.getUserId();

        // 2. 查询 organizer_id = currentUserId 的比赛
        QueryWrapper<Competition> wrapper = new QueryWrapper<>();
        wrapper.eq("organizer_id", currentUserId);

        // 按创建时间倒序（最新申请的在前面）
        wrapper.orderByDesc("create_time");

        return Result.success(competitionService.list(wrapper));
    }

    // 获取年份列表（给前端筛选框用）
    @GetMapping("/years")
    public Result getYears() {
        return Result.success(competitionService.getYearList());
    }


    /**
     * 修改比赛信息 (用于驳回后重新提交)
     */
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result update(@RequestBody CompetitionApplyDTO dto) {
        if (dto.getId() == null) return Result.error("比赛ID不能为空");

        Competition oldComp = competitionService.getById(dto.getId());
        if (oldComp == null) return Result.error("比赛不存在");

        // 鉴权
        if (!oldComp.getOrganizerId().equals(UserContext.getUserId())) {
            return Result.error("无权修改此比赛");
        }

        // 更新基本信息
        BeanUtils.copyProperties(dto, oldComp, "id", "organizerId", "createTime", "status");
        // 状态重置为待审核
        oldComp.setStatus((byte) 0);
        oldComp.setAuditMsg("");

        competitionService.updateById(oldComp);

        // ★★★ 清理旧数据 (项目 + 轮次) ★★★
        // 1. 删除旧的项目关联
        QueryWrapper<CompetitionEvent> delEventWrapper = new QueryWrapper<>();
        delEventWrapper.eq("competition_id", dto.getId());
        competitionEventService.remove(delEventWrapper);

        // 2. 删除旧的轮次配置
        QueryWrapper<CompetitionRound> delRoundWrapper = new QueryWrapper<>();
        delRoundWrapper.eq("competition_id", dto.getId());
        competitionRoundService.remove(delRoundWrapper);

        // ★★★ 重新保存 ★★★
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            saveEventsAndRounds(dto.getId(), dto.getEvents());
        }

        return Result.success("修改成功，已重新提交审核");
    }

    @PostMapping("/status")
    public Result updateStatus(@RequestBody Competition comp) {
        if (comp.getId() == null || comp.getStatus() == null) {
            return Result.error("参数不完整");
        }

        Competition oldComp = competitionService.getById(comp.getId());
        if (oldComp == null) return Result.error("比赛不存在");

        // 鉴权
        Long currentUserId = UserContext.getUserId();
        if (!oldComp.getOrganizerId().equals(currentUserId)) {
            return Result.error("无权操作");
        }

        // 更新状态
        oldComp.setStatus(comp.getStatus());
        competitionService.updateById(oldComp);

        return Result.success("状态更新成功");
    }

    /**
     * 删除比赛（仅允许删除审核中或被驳回的比赛）
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result delete(@PathVariable Long id) {
        // 1. 检查比赛是否存在
        Competition comp = competitionService.getById(id);
        if (comp == null) return Result.error("比赛不存在");

        // 2. 检查权限
        Long currentUserId = UserContext.getUserId();
        if (!comp.getOrganizerId().equals(currentUserId)) {
            return Result.error("无权删除该比赛");
        }

        // 3. 检查状态，只能删除审核中或被驳回的比赛
        if (comp.getStatus() != 0 && comp.getStatus() != 4) {
            return Result.error("只能删除审核中或被驳回的比赛");
        }

        // 4. 删除比赛（会自动删除关联的competition_event记录）
        competitionService.removeById(id);
        return Result.success("比赛删除成功");
    }

    /**
     * 辅助方法：保存复杂的项目和轮次结构
     */
    private void saveEventsAndRounds(Long competitionId, List<CompetitionApplyDTO.EventConfigDTO> events) {
        List<CompetitionEvent> eventList = new ArrayList<>();
        List<CompetitionRound> roundList = new ArrayList<>();

        for (CompetitionApplyDTO.EventConfigDTO eventConfig : events) {
            String eventId = eventConfig.getEventId();

            // 1. 准备保存 CompetitionEvent (关联表)
            CompetitionEvent ce = new CompetitionEvent();
            ce.setCompetitionId(competitionId);
            ce.setEventId(eventId);
            // 轮次数 = 前端传来的轮次列表长度
            int roundCount = (eventConfig.getRounds() != null) ? eventConfig.getRounds().size() : 1;
            ce.setRoundCount(roundCount);
            eventList.add(ce);

            // 2. 准备保存 CompetitionRound (轮次详情表)
            if (eventConfig.getRounds() != null) {
                for (CompetitionApplyDTO.RoundConfigDTO roundConfig : eventConfig.getRounds()) {
                    CompetitionRound round = new CompetitionRound();
                    round.setCompetitionId(competitionId);
                    round.setEventId(eventId);
                    round.setRoundOrder(roundConfig.getRoundOrder()); // e.g., 1
                    round.setRoundName(roundConfig.getRoundName());   // e.g., "初赛"
                    round.setAdvancementRule(roundConfig.getAdvancementRule()); // e.g., "TOP-12"

                    roundList.add(round);
                }
            }
        }

        // 批量插入
        if (!eventList.isEmpty()) competitionEventService.saveBatch(eventList);
        if (!roundList.isEmpty()) competitionRoundService.saveBatch(roundList);
    }

    /**
     * ★★★ 新增：获取比赛详情（专门用于编辑回显） ★★★
     * 返回结构完全匹配 CompetitionApplyDTO，前端直接回填表单
     */
    @GetMapping("/detail-for-edit/{id}")
    public Result getDetailForEdit(@PathVariable Long id) {
        // 1. 查主表
        Competition comp = competitionService.getById(id);
        if (comp == null) return Result.error("比赛不存在");

        // 2. 权限校验 (只能看自己的，或者管理员)
        Long currentUserId = UserContext.getUserId();
        if (!comp.getOrganizerId().equals(currentUserId)) {
            // 这里简单处理，如果不是主办方也不是管理员... (实际业务可按需加)
            // return Result.error("无权查看");
        }

        // 3. 准备 DTO
        CompetitionApplyDTO dto = new CompetitionApplyDTO();
        BeanUtils.copyProperties(comp, dto);

        // 4. ★★★ 组装核心：项目 + 轮次 ★★★

        // 4.1 查出该比赛所有的项目 (CompetitionEvent)
        QueryWrapper<CompetitionEvent> ceQuery = new QueryWrapper<>();
        ceQuery.eq("competition_id", id);
        List<CompetitionEvent> ceList = competitionEventService.list(ceQuery);

        // 4.2 查出该比赛所有的轮次 (CompetitionRound)
        QueryWrapper<CompetitionRound> roundQuery = new QueryWrapper<>();
        roundQuery.eq("competition_id", id);
        roundQuery.orderByAsc("round_order"); // 必须按顺序排
        List<CompetitionRound> allRounds = competitionRoundService.list(roundQuery);

        // 4.3 拼装 List<EventConfigDTO>
        List<CompetitionApplyDTO.EventConfigDTO> eventConfigList = new ArrayList<>();

        for (CompetitionEvent ce : ceList) {
            CompetitionApplyDTO.EventConfigDTO eventDTO = new CompetitionApplyDTO.EventConfigDTO();
            eventDTO.setEventId(ce.getEventId());

            // 筛选出属于当前 eventId 的轮次，并转为 RoundConfigDTO
            List<CompetitionApplyDTO.RoundConfigDTO> roundDTOs = allRounds.stream()
                    .filter(r -> r.getEventId().equals(ce.getEventId())) // 过滤
                    .map(r -> {
                        CompetitionApplyDTO.RoundConfigDTO rDto = new CompetitionApplyDTO.RoundConfigDTO();
                        rDto.setRoundOrder(r.getRoundOrder());
                        rDto.setRoundName(r.getRoundName());
                        rDto.setAdvancementRule(r.getAdvancementRule());
                        return rDto;
                    })
                    .collect(Collectors.toList());

            // 如果万一数据库里只有 CompetitionEvent 没有 Round (脏数据兜底)，手动补一个默认决赛
            if (roundDTOs.isEmpty()) {
                CompetitionApplyDTO.RoundConfigDTO defaultRound = new CompetitionApplyDTO.RoundConfigDTO();
                defaultRound.setRoundOrder(1);
                defaultRound.setRoundName("决赛");
                defaultRound.setAdvancementRule("");
                roundDTOs.add(defaultRound);
            }

            eventDTO.setRounds(roundDTOs);
            eventConfigList.add(eventDTO);
        }

        dto.setEvents(eventConfigList);

        return Result.success(dto);
    }
}