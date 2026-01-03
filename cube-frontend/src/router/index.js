import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 首页
    {
      path: "/",
      name: "home",
      component: () => import("../views/HomeView.vue"),
    },
    // 登录
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },

    // 1. 赛事列表
    {
      path: "/competition",
      name: "competition-list",
      component: () => import("../views/competition/CompetitionList.vue"),
    },

    {
      path: "/competition/apply",
      name: "competition-apply",
      component: () => import("../views/competition/Apply.vue"),
    },

    // 2. 赛事详情 (嵌套路由)
    {
      path: "/competition/:slug",
      component: () => import("../views/competition/DetailLayout.vue"),
      children: [
        {
          path: "",
          component: () => import("../views/competition/tabs/Info.vue"),
        },
        {
          path: "regulations",
          component: () => import("../views/competition/tabs/Rules.vue"),
        },
        {
          path: "competitors",
          component: () => import("../views/competition/tabs/Competitors.vue"),
        },
        {
          path: "registration",
          component: () => import("../views/competition/tabs/Registration.vue"),
        },
        // 注意：如果你还没创建 Competitors.vue (选手列表)，这行先注释掉，不然报错
        // { path: 'competitors', component: () => import('../views/competition/tabs/Competitors.vue') }
      ],
    },

    // 3. 个人中心
    {
      path: "/user/profile",
      name: "profile",
      component: () => import("../views/user/Profile.vue"),
    },

    // 4. 我的报名
    {
      path: "/user/my-competitions",
      name: "my-competitions",
      component: () => import("../views/user/MyCompetitions.vue"),
    },

    // 5. 成绩排名
    {
      path: "/results/rank",
      name: "rankings",
      component: () => import("../views/result/Rankings.vue"),
    },

    // 6. 选手列表
    {
      path: "/results/person",
      name: "competitor-list",
      component: () => import("../views/result/CompetitorList.vue"),
    },

    // 7. 选手个人主页
    {
      path: "/results/person/:displayId",
      component: () => import("../views/user/PersonProfile.vue"),
    },
    // 8. 管理员控制台
    {
      path: "/admin/dashboard",
      name: "admin-dashboard",
      component: () => import("../views/admin/Dashboard.vue"),
      // 路由独享守卫：只允许 admin 进入
      beforeEnter: (to, from, next) => {
        const userInfo = JSON.parse(localStorage.getItem("cube_user") || "{}");
        if (userInfo.role === "admin") {
          next();
        } else {
          next("/"); // 普通用户想进就踢回首页
        }
      },
    },
    // 9. 用户管理页面
    {
      path: "/admin/users",
      name: "admin-users",
      component: () => import("../views/admin/UserManage.vue"),
      // 同样加上管理员守卫
      beforeEnter: (to, from, next) => {
        const userInfo = JSON.parse(localStorage.getItem("cube_user") || "{}");
        if (userInfo.role === "admin") {
          next();
        } else {
          next("/");
        }
      },
    },
    // 10. 成绩录入控制台
    {
      path: "/competition/:slug/score",
      component: () => import("../views/competition/ScoreManager.vue"),
    },
    // 成绩 - 历史赛事列表
    {
      path: "/results/competition",
      component: () => import("../views/result/PastCompetitionList.vue"),
    },
    // 成绩 - 赛事成绩详情
    {
      path: "/results/competition/:slug",
      component: () => import("../views/result/CompetitionResultDetail.vue"),
    },
  ],
});

export default router;
