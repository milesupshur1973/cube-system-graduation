import request from "@/utils/request";

export function getRankings(params) {
  return request({
    url: "/result/rankings",
    method: "get",
    params,
  });
}

export function getPersonPBs(displayId) {
  return request({
    url: `/result/person/${displayId}/pbs`,
    method: "get",
  });
}

// 获取某项目的所有成绩 (用于录入回显)
export function getResultList(competitionId, eventId, roundId) {
  return request({
    url: "/result/list",
    method: "get",
    params: { competitionId, eventId, roundId },
  });
}

// 新增：获取历史成绩
export function getPersonHistory(displayId) {
  return request({
    url: `/result/person/${displayId}/history`,
    method: "get",
  });
}

// 一键初始化第一轮
export function initFirstRound(data) {
  return request({
    url: '/result/init-round1',
    method: 'post',
    data
  })
}

// 晋级下一轮
export function promoteCompetitors(data) {
  return request({
    url: '/result/promote',
    method: 'post',
    data
  })
}

// 录入/保存单条成绩
export function saveScore(data) {
  return request({
    url: '/result/save',  // 对应后端的 ResultController.enter 接口
    method: 'post',
    data
  })
}