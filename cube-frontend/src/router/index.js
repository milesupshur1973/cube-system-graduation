import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus"; // 引入消息提示

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 首页
    {
      path: "/",
      name: "home",
      component: () => import("../views/HomeView.vue"),
      meta: { title: "首页" },
    },
    // 登录
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
      meta: { title: "用户登录" },
    },

    // 1. 赛事列表
    {
      path: "/competition",
      name: "competition-list",
      component: () => import("../views/competition/CompetitionList.vue"),
      meta: { title: "赛事列表" },
    },

    // 申请比赛 (需要登录)
    {
      path: "/competition/apply",
      name: "competition-apply",
      component: () => import("../views/competition/Apply.vue"),
      meta: { title: "申请办赛", requiresAuth: true },
    },

    // 2. 赛事详情 (嵌套路由)
    {
      path: "/competition/:slug",
      name: "competition-detail-layout", // 给父级也加个名字
      component: () => import("../views/competition/DetailLayout.vue"),
      // 默认跳转到 info
      redirect: { name: 'competition-info' },
      children: [
        {
          path: "", // 默认子路由
          name: "competition-info", // ★ 给子路由加 name
          component: () => import("../views/competition/tabs/Info.vue"),
          meta: { title: "比赛详情" },
        },
        {
          path: "regulations",
          name: "competition-rules",
          component: () => import("../views/competition/tabs/Rules.vue"),
          meta: { title: "比赛规则" },
        },
        {
          path: "competitors",
          name: "competition-competitors",
          component: () => import("../views/competition/tabs/Competitors.vue"),
          meta: { title: "选手列表" },
        },
        {
          path: "registration",
          name: "competition-registration",
          component: () => import("../views/competition/tabs/Registration.vue"),
          meta: { title: "在线报名" },
        },
      ],
    },

    // 3. 个人中心
    {
      path: "/user/profile",
      name: "profile",
      component: () => import("../views/user/Profile.vue"),
      meta: { title: "个人中心", requiresAuth: true },
    },

    // 4. 我的报名
    {
      path: "/user/my-competitions",
      name: "my-competitions",
      component: () => import("../views/user/MyCompetitions.vue"),
      meta: { title: "我的比赛", requiresAuth: true },
    },

    // 5. 成绩排名
    {
      path: "/results/rank",
      name: "rankings",
      component: () => import("../views/result/Rankings.vue"),
      meta: { title: "全站排名" },
    },

    // 6. 选手列表
    {
      path: "/results/person",
      name: "competitor-list",
      component: () => import("../views/result/CompetitorList.vue"),
      meta: { title: "选手数据库" },
    },

    // 7. 选手个人主页
    {
      path: "/results/person/:displayId",
      name: "person-profile",
      component: () => import("../views/user/PersonProfile.vue"),
      meta: { title: "选手档案" },
    },

    // 8. 管理员控制台
    {
      path: "/admin/dashboard",
      name: "admin-dashboard",
      component: () => import("../views/admin/Dashboard.vue"),
      meta: { title: "管理员控制台", requiresAuth: true, requiresAdmin: true }, // ★ 用 meta 标记权限
    },
    // 9. 用户管理页面
    {
      path: "/admin/users",
      name: "admin-users",
      component: () => import("../views/admin/UserManage.vue"),
      meta: { title: "用户管理", requiresAuth: true, requiresAdmin: true },
    },

    // 10. 成绩录入控制台 (替换为新文件)
    {
      path: "/competition/:slug/score",
      name: "score-entry", // 给个好记的名字
      // ★★★ 这里替换成了我们在上一轮做的新文件 ★★★
      component: () => import("../views/competition/ResultEntry.vue"),
      meta: { title: "成绩录入系统", requiresAuth: true },
    },

    // 成绩 - 历史赛事列表
    {
      path: "/results/competition",
      name: "past-competitions",
      component: () => import("../views/result/PastCompetitionList.vue"),
      meta: { title: "历史赛事" },
    },
    // 成绩 - 赛事成绩详情
    {
      path: "/results/competition/:slug",
      name: "competition-result-detail",
      component: () => import("../views/result/CompetitionResultDetail.vue"),
      meta: { title: "详细成绩" },
    },
  ],
});

// ★★★ 全局前置守卫 (核心优化点) ★★★
router.beforeEach((to, from, next) => {
  // 1. 设置浏览器标题 document.title
  if (to.meta.title) {
    document.title = `${to.meta.title} - Cube System`;
  } else {
    document.title = "Cube System";
  }

  // 2. 获取用户信息
  const userStr = localStorage.getItem("cube_user");
  const userInfo = userStr ? JSON.parse(userStr) : null;
  const isLoggedIn = !!userInfo && !!userInfo.id;
  const isAdmin = isLoggedIn && userInfo.role === "admin";

  // 3. 判断是否需要登录 (requiresAuth)
  if (to.meta.requiresAuth && !isLoggedIn) {
    ElMessage.warning("请先登录");
    next({ name: "login", query: { redirect: to.fullPath } }); // 登录后跳回原页面
    return;
  }

  // 4. 判断是否需要管理员权限 (requiresAdmin)
  if (to.meta.requiresAdmin && !isAdmin) {
    ElMessage.error("无权访问");
    next({ name: "home" }); // 踢回首页
    return;
  }

  // 5. 放行
  next();
});

export default router;