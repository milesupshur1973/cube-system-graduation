import { ref } from "vue";
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", () => {
  // 1. 初始化时，从 localStorage 读取 token 和 用户信息
  const token = ref(localStorage.getItem("cube_token") || "");
  const userInfo = ref(JSON.parse(localStorage.getItem("cube_user") || "{}"));

  // 2. 登录动作：接收后端返回的 dataMap { token: '...', user: {...} }
  const login = (dataMap) => {
    // 存 Token
    token.value = dataMap.token;
    localStorage.setItem("cube_token", dataMap.token);

    // 存 用户信息
    userInfo.value = dataMap.user;
    localStorage.setItem("cube_user", JSON.stringify(dataMap.user));
  };

  // 3. 退出/清除动作
  const logout = () => {
    token.value = "";
    userInfo.value = {};
    localStorage.removeItem("cube_token");
    localStorage.removeItem("cube_user");
  };

  return { token, userInfo, login, logout };
});
