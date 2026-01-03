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
export function getResultList(competitionId, eventId) {
  return request({
    url: "/result/list",
    method: "get",
    params: { competitionId, eventId },
  });
}

// 新增：获取历史成绩
export function getPersonHistory(displayId) {
  return request({
    url: `/result/person/${displayId}/history`,
    method: "get",
  });
}