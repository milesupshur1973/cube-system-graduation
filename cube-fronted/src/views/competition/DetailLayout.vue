<template>
  <div>
    <div
      style="
        background-color: #f5f7fa;
        padding: 40px 0;
        border-bottom: 1px solid #ddd;
      "
    >
      <div style="max-width: 1100px; margin: 0 auto; text-align: center">
        <h1 style="font-size: 28px; color: #303133">
          {{ compInfo.name || "加载中..." }}
        </h1>
        <div style="margin-top: 10px; color: #606266">
          <el-icon><Location /></el-icon> {{ compInfo.province }} -
          {{ compInfo.city }}
          <span style="margin: 0 10px">|</span>
          <el-icon><Calendar /></el-icon> {{ compInfo.startDate }}
        </div>
      </div>
    </div>

    <div style="background: #fff; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05)">
      <div
        style="
          max-width: 1100px;
          margin: 0 auto;
          display: flex;
          align-items: center;
        "
      >
        <el-menu
          :default-active="route.path"
          mode="horizontal"
          router
          style="border-bottom: none; flex-grow: 1"
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

    <div
      style="
        max-width: 1100px;
        margin: 20px auto;
        padding: 20px;
        background: #fff;
        min-height: 400px;
      "
    >
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
