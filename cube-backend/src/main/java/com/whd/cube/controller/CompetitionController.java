package com.whd.cube.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whd.cube.common.Result;
import com.whd.cube.common.UserContext;
import com.whd.cube.entity.Competition;
import com.whd.cube.entity.CompetitionEvent;
import com.whd.cube.entity.User;
import com.whd.cube.service.CompetitionEventService;
import com.whd.cube.service.CompetitionService;
import com.whd.cube.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @Transactional(rollbackFor = Exception.class) // 事务：要么都成功，要么都失败
    public Result apply(@RequestBody Competition comp) {
        // 1. 基础校验
        if (comp.getName() == null || comp.getSlug() == null) {
            return Result.error("比赛名称和代号不能为空");
        }

        // 校验比赛时间：必须至少提前7天
        if (comp.getStartDate() != null) {
            LocalDate minDate = LocalDate.now().plusDays(7);
            if (comp.getStartDate().isBefore(minDate)) {
                return Result.error("比赛开始时间必须至少在 7 天之后，请预留充足的公示期");
            }
        } else {
            return Result.error("请选择比赛日期");
        }

        // ★★★ 2. 校验必须选项目 ★★★
        if (comp.getEventIds() == null || comp.getEventIds().isEmpty()) {
            return Result.error("请至少选择一个比赛项目");
        }

        // 3. 查重 Slug (保持原有逻辑)
        QueryWrapper<Competition> query = new QueryWrapper<>();
        query.eq("slug", comp.getSlug());
        if (competitionService.count(query) > 0) {
            return Result.error("赛事代号(Slug)已存在，请更换一个");
        }

        // 4. 填充默认值并保存主表
        comp.setStatus((byte)0); // 待审核
        Long currentUserId = UserContext.getUserId();
        comp.setOrganizerId(currentUserId);

        // 保存主表，保存后 comp.getId() 会自动获取到自增的ID
        competitionService.save(comp);

        // 5. ★★★ 保存项目关联表 (competition_event) ★★★
        List<CompetitionEvent> eventList = new ArrayList<>();

        for (String eventId : comp.getEventIds()) {
            com.whd.cube.entity.CompetitionEvent ce = new com.whd.cube.entity.CompetitionEvent();
            ce.setCompetitionId(comp.getId()); // 使用刚才生成的主键ID
            ce.setEventId(eventId);
            ce.setRoundCount(1); // 默认1轮
            eventList.add(ce);
        }

        // 批量保存
        competitionEventService.saveBatch(eventList);

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
    /**
     * 修改比赛信息 (用于驳回后重新提交)
     */
    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result update(@RequestBody Competition comp) {
        // 1. 检查ID
        if (comp.getId() == null) {
            return Result.error("比赛ID不能为空");
        }

        Competition oldComp = competitionService.getById(comp.getId());
        if (oldComp == null) return Result.error("比赛不存在");

        // 2. 权限检查
        Long currentUserId = UserContext.getUserId();
        if (!oldComp.getOrganizerId().equals(currentUserId)) {
            return Result.error("无权修改此比赛");
        }

        // 3. 基础校验
        if (comp.getEventIds() == null || comp.getEventIds().isEmpty()) {
            return Result.error("请至少选择一个比赛项目");
        }

        // 校验比赛时间：必须至少提前7天
        if (comp.getStartDate() != null) {
            LocalDate minDate = LocalDate.now().plusDays(7);
            if (comp.getStartDate().isBefore(minDate)) {
                return Result.error("比赛开始时间必须至少在 7 天之后");
            }
        }

        // 4. 更新其他基本信息
        oldComp.setName(comp.getName());
        oldComp.setProvince(comp.getProvince());
        oldComp.setCity(comp.getCity());
        oldComp.setLocation(comp.getLocation());
        oldComp.setStartDate(comp.getStartDate());
        oldComp.setEndDate(comp.getEndDate());
        oldComp.setContentDescription(comp.getContentDescription());
        oldComp.setContentRule(comp.getContentRule());
        oldComp.setCompetitorLimit(comp.getCompetitorLimit());
        oldComp.setRegStartTime(comp.getRegStartTime());
        oldComp.setRegEndTime(comp.getRegEndTime());

        // 5. 状态重置
        oldComp.setStatus((byte) 0); // 变回“待审核”
        oldComp.setAuditMsg("");     // 清空之前的驳回理由

        competitionService.updateById(oldComp);

        // 6. 更新关联项目 (先删后加)
        QueryWrapper<CompetitionEvent> delWrapper = new QueryWrapper<>();
        delWrapper.eq("competition_id", comp.getId());
        competitionEventService.remove(delWrapper);

        List<CompetitionEvent> eventList = new ArrayList<>();
        for (String eventId : comp.getEventIds()) {
            CompetitionEvent ce = new CompetitionEvent();
            ce.setCompetitionId(comp.getId());
            ce.setEventId(eventId);
            ce.setRoundCount(1);
            eventList.add(ce);
        }
        competitionEventService.saveBatch(eventList);

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
}