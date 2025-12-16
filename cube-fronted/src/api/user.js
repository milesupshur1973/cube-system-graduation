import request from "@/utils/request";

export function login(data) {
  return request({
    url: "/user/login",
    method: "post",
    data,
  });
}

export function register(data) {
  return request({
    url: "/user/register",
    method: "post",
    data,
  });
}

// 获取用户列表 (带分页搜索)
export function getUserList(params) {
  return request({
    url: "/user/list",
    method: "get",
    params, // 会自动拼接成 ?page=1&size=10...
  });
}

// 获取用户详情 (ID)
export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: "get",
  });
}

// 获取用户公开信息 (DisplayID)
export function getPublicUser(displayId) {
  return request({
    url: `/user/public/${displayId}`,
    method: "get",
  });
}

// 更新头像
export function updateUser(data) {
  return request({
    url: "/user/update",
    method: "post",
    data,
  });
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: "/user/update-password",
    method: "post",
    data,
  });
}

// 修改用户角色（仅管理员可用）
export function updateUserRole(data) {
  return request({
    url: "/user/role",
    method: "post",
    data,
  });
}