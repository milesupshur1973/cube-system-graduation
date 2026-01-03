import axios from "axios";
import { ElMessage } from "element-plus";
// 引入刚刚改好的 store，为了获取 token 和 执行 logout
import { useUserStore } from "@/stores/user";
import router from "@/router";

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000,
});

// 下面的代码是AOP（面向切面编程的一种实现方式）抽离通用逻辑，在切点加入通用逻辑
// 1. 请求拦截器：每次请求都带上 Token
service.interceptors.request.use(
  (config) => {
    // 注意：这里不能在最外面引入 store，必须在函数内部引入，否则会报错
    const userStore = useUserStore();
    if (userStore.token) {
      // 后端 LoginInterceptor 里检查的是 request.getHeader("token")
      // 所以这里的 key 必须是 'token'
      config.headers["token"] = userStore.token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 2. 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 如果没有 response，说明网络断了
    if (!error.response) {
      ElMessage.error("网络连接失败，请检查后端是否启动");
      return Promise.reject(error);
    }

    // 获取状态码
    const status = error.response.status;
    const msg = error.response.data.msg || "未知错误";

    if (status === 401) {
      // 401 说明 Token 过期或未登录
      ElMessage.error("登录已过期，请重新登录");

      const userStore = useUserStore();
      userStore.logout(); // 清除本地缓存
      router.push("/login"); // 强制跳转登录页
    } else if (status === 500) {
      // 500 一般是后端逻辑报错
      ElMessage.error(msg);
    } else {
      ElMessage.error(msg);
    }

    return Promise.reject(error);
  }
);

export default service;
