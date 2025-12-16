import request from "@/utils/request";

// 获取某比赛的选手列表
export function getCompetitors(competitionId, params) {
  return request({
    url: `/registration/competitors/${competitionId}`,
    method: "get",
    params,
  });
}

// 修改：不再传 userId
export function getMyRegistration(competitionId) {
  return request({
    url: "/registration/my",
    method: "get",
    params: { competitionId },
  });
}

// 修改：不再传 userId
export function submitRegistration(data) {
  return request({
    url: "/registration/apply",
    method: "post",
    data,
  });
}

// 修改：不再传 userId
export function getMyRegistrationList() {
  return request({
    url: "/registration/my-list",
    method: "get",
  });
}
