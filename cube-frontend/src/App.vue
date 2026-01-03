<template>
  <div class="common-layout">
    <el-container>
      <el-header class="site-header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')" style="cursor: pointer">
            <el-icon class="logo-icon"><Grid /></el-icon>
            <span class="logo-text">中国魔方赛事网</span>
          </div>

          <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            :ellipsis="false"
            router
            class="top-menu"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon> 首页
            </el-menu-item>

            <el-menu-item index="/competition">
              <el-icon><Trophy /></el-icon> 赛事
            </el-menu-item>

            <el-sub-menu index="/results">
              <template #title>
                <el-icon><TrendCharts /></el-icon> 成绩
              </template>
              <el-menu-item index="/results/competition">赛事</el-menu-item>
              <el-menu-item index="/results/person">选手</el-menu-item>
              <el-menu-item index="/results/rank">排名</el-menu-item>
            </el-sub-menu>

            <div style="flex-grow: 1"></div>

            <el-sub-menu index="/user" v-if="userStore.userInfo.id">
              <template #title>
                <el-icon><User /></el-icon>
                {{ userStore.userInfo.name }}
              </template>

              <el-menu-item index="/user/profile">我的资料</el-menu-item>
              <el-menu-item index="/user/my-competitions"
                >赛事中心</el-menu-item
              >

              <el-menu-item
                :index="`/results/person/${userStore.userInfo.displayId}`"
              >
                我的主页
              </el-menu-item>

              <el-menu-item
                v-if="userStore.userInfo.role === 'admin'"
                index="/admin/dashboard"
                style="color: #f56c6c"
              >
                <el-icon><Monitor /></el-icon> 管理员控制台
              </el-menu-item>

              <el-menu-item
                v-if="userStore.userInfo.role === 'admin'"
                index="/admin/users"
                style="color: #f56c6c"
              >
                <el-icon><UserFilled /></el-icon> 用户权限管理
              </el-menu-item>

              <el-divider style="margin: 5px 0" />

              <el-menu-item index="/competition/apply">
                <el-icon><Edit /></el-icon> 申请公示比赛
              </el-menu-item>

              <el-divider style="margin: 5px 0" />

              <el-menu-item @click="handleLogout" index=""
                >退出登录</el-menu-item
              >
            </el-sub-menu>

            <el-menu-item index="/login" v-else>
              <el-icon><User /></el-icon> 登录 / 注册
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>

      <el-main class="site-main">
        <RouterView />
      </el-main>

      <el-footer class="site-footer">
        <div class="footer-content">
          <el-text type="info">Copyright © 2025 王海东</el-text>
        </div>
      </el-footer>
    </el-container>
    <AiChat />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { useEventStore } from "@/stores/event";
import AiChat from "@/components/AiChat.vue";
import {
  Grid,
  HomeFilled,
  Trophy,
  TrendCharts,
  User,
  Edit,
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const eventStore = useEventStore();
const activeIndex = ref("/");

// 监听路由变化，高亮对应的菜单
watch(
  () => route.path,
  (newPath) => {
    activeIndex.value = newPath;
  }
);

// 退出登录逻辑
const handleLogout = () => {
  userStore.logout();
  router.push("/");
};

// 4. 应用挂载时，立即拉取项目字典
onMounted(() => {
  eventStore.loadEvents();
});
</script>

<style scoped>
/* 保持原有样式 */
.site-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #409eff;
  font-size: 20px;
  font-weight: bold;
  margin-right: 40px;
}
.top-menu {
  border-bottom: none !important;
  flex-grow: 1;
}
.site-main {
  padding: 20px;
  min-height: calc(100vh - 140px);
  background-color: #f4f6f8;
}
.site-footer {
  background-color: #303133;
  color: #909399;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
