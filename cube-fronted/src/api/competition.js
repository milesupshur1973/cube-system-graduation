import request from "@/utils/request";

// 获取所有赛事 (支持分页和筛选)
export function getCompetitionList(params) {
  return request({
    url: "/competition/list",
    method: "get",
    params,
  });
}

// 获取近期赛事（首页用）
export function getUpcomingCompetitions() {
  return request({
    url: "/competition/upcoming",
    method: "get",
  });
}

// 根据 slug 获取详情
export function getCompetitionBySlug(slug) {
  return request({
    url: `/competition/slug/${slug}`,
    method: "get",
  });
}

// 申请比赛
export function applyCompetition(data) {
  return request({
    url: "/competition/apply",
    method: "post",
    data,
  });
}

// 获取比赛下的项目列表 (对应后端 CompetitionEventController)
export function getCompetitionEvents(competitionId) {
  return request({
    url: `/competition-event/list/${competitionId}`,
    method: "get",
  });
}

// 获取待审核比赛列表
export function getPendingCompetitions(params) {
  return request({
    url: "/competition/pending",
    method: "get",
    params,
  });
}

// 审核比赛
export function auditCompetition(id, pass, msg) {
  return request({
    url: `/competition/audit/${id}`,
    method: "post",
    params: { pass, msg },
  });
}

// 获取我主办的比赛列表
export function getMyOrganizedCompetitions() {
  return request({
    url: "/competition/my-organized",
    method: "get",
  });
}

// 获取比赛年份列表
export function getCompetitionYears() {
  return request({
    url: "/competition/years",
    method: "get",
  });
}

// 更新/重新提交比赛
export function updateCompetition(data) {
  return request({
    url: "/competition/update",
    method: "post",
    data,
  });
}

// 更新比赛状态
export function updateCompetitionStatus(data) {
  return request({
    url: "/competition/status",
    method: "post",
    data,
  });
}