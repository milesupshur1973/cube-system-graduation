import request from "@/utils/request";

export function getArticleList(params) {
  return request({
    url: "/article/list",
    method: "get",
    params
  });
}


export function publishArticle(data) {
  return request({
    url: "/article/publish",
    method: "post",
    data
  });
}