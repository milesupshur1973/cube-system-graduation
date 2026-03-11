<template>
  <div>
    <div class="header-wrapper">
      <div class="header-content">
        <h1 class="header-title">
          {{ compInfo.name || "加载中..." }}
        </h1>
        <div class="header-info">
          <el-icon><Location /></el-icon> {{ compInfo.province }} -
          {{ compInfo.city }}
          <span class="info-separator">|</span>
          <el-icon><Calendar /></el-icon> {{ compInfo.startDate }}
        </div>
      </div>
    </div>

    <div class="nav-wrapper">
      <div class="nav-content">
        <el-menu
            :default-active="route.path"
            mode="horizontal"
            router
            class="custom-menu"
        >
          <el-menu-item :index="`/competition/${slug}`">详情</el-menu-item>
          <el-menu-item :index="`/competition/${slug}/regulations`"
          >规则</el-menu-item
          >
          <el-menu-item :index="`/competition/${slug}/competitors`"
          >选手</el-menu-item
          >
          <el-menu-item
              v-if="compInfo.status === 1"
              :index="`/competition/${slug}/registration`"
          >
            报名</el-menu-item
          >
        </el-menu>
      </div>
    </div>

    <div class="main-content">
      <router-view :compInfo="compInfo" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { getCompetitionBySlug } from "@/api/competition";
import { Location, Calendar } from "@element-plus/icons-vue";

const route = useRoute();
const slug = route.params.slug;
const compInfo = ref({});

onMounted(async () => {
  try {
    const res = await getCompetitionBySlug(slug);
    if (res.data.code === 200) {
      compInfo.value = res.data.data;
    }
  } catch (e) {
    console.error(e);
  }
});
</script>

<style scoped>
/* 头部背景及外层容器 */
.header-wrapper {
  background-color: #f5f7fa;
  padding: 40px 0;
  border-bottom: 1px solid #ddd;
}

/* 头部内容居中容器 */
.header-content {
  max-width: 1100px;
  margin: 0 auto;
  text-align: center;
}

/* 标题样式 */
.header-title {
  font-size: 28px;
  color: #303133;
}

/* 地址与日期信息容器 */
.header-info {
  margin-top: 10px;
  color: #606266;
}

/* 分隔符样式 */
.info-separator {
  margin: 0 10px;
}

/* 导航栏外层阴影与背景 */
.nav-wrapper {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 导航栏内容居中及Flex布局 */
.nav-content {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  align-items: center;
}

/* 自定义 el-menu 样式 */
.custom-menu {
  border-bottom: none;
  flex-grow: 1;
}

/* 主体路由视图容器 */
.main-content {
  max-width: 1100px;
  margin: 20px auto;
  padding: 20px;
  background: #fff;
  min-height: 400px;
}
</style>