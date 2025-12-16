<template>
  <div>
    <el-descriptions border :column="1" size="large" class="comp-info-list">
      <el-descriptions-item label="赛事规则">
        <el-tag type="primary" effect="plain">WCA 规则标准</el-tag>
        <span style="margin-left: 10px; font-size: 13px; color: #666">
          本场比赛严格执行 WCA 竞赛规则
        </span>
      </el-descriptions-item>

      <el-descriptions-item label="日期">
        {{ compInfo.startDate }} 至 {{ compInfo.endDate }}
      </el-descriptions-item>

      <el-descriptions-item label="地点">
        {{ compInfo.province }} {{ compInfo.city }} {{ compInfo.location }}
      </el-descriptions-item>

      <el-descriptions-item label="主办方">
        <el-link type="primary" :underline="false">
          <el-icon style="margin-right: 4px"><User /></el-icon>
          {{ organizerName }}
        </el-link>
      </el-descriptions-item>

      <el-descriptions-item label="开设项目">
        <el-space wrap>
          <el-tag v-for="item in events" :key="item.id" effect="plain">{{
            item.name
          }}</el-tag>
          <el-tag v-if="events.length === 0" type="info">暂无项目数据</el-tag>
        </el-space>
      </el-descriptions-item>

      <el-descriptions-item label="报名时间">
        {{ formatTime(compInfo.regStartTime) }} 至
        {{ formatTime(compInfo.regEndTime) }}
      </el-descriptions-item>

      <el-descriptions-item label="人数限制">
        <span style="font-weight: bold; color: #f56c6c"
          >{{ compInfo.competitorLimit }} 人</span
        >
      </el-descriptions-item>

      <el-descriptions-item label="关于比赛">
        <div
          class="rich-text-content"
          v-html="
            compInfo.contentDescription ||
            '<p style=\'color:#999\'>暂无详细介绍</p>'
          "
        ></div>
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import axios from "axios";
import { User } from "@element-plus/icons-vue";
import { getCompetitionEvents } from "@/api/competition";
import { getUserById } from "@/api/user";
import { useEventStore } from "@/stores/event";

const props = defineProps(["compInfo"]);
const events = ref([]);
const organizerName = ref("加载中...");
const eventStore = useEventStore();

const formatTime = (str) => {
  if (!str) return "";
  return str.toString().split("T")[0];
};

const loadData = async () => {
  if (!props.compInfo.id) return;

  try {
    // 替换项目列表请求
    const eventRes = await getCompetitionEvents(props.compInfo.id);
    if (eventRes.data.code === 200) {
      events.value = eventRes.data.data.map((item) => ({
        ...item,
        name: eventStore.getEventName(item.eventId),
      }));
    }

    if (props.compInfo.organizerId) {
      // 替换用户详情请求
      const userRes = await getUserById(props.compInfo.organizerId);
      if (userRes.data.code === 200) {
        organizerName.value = userRes.data.data.name;
      }
    }
  } catch (e) {
    console.error(e);
    organizerName.value = "未知主办方";
  }
};

watch(
  () => props.compInfo,
  (newVal) => {
    if (newVal.id) loadData();
  },
  { immediate: true }
);
</script>

<style scoped>
/* 保持原有样式不变 */
.rich-text-content {
  line-height: 1.6;
  color: #333;
}
.rich-text-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 10px 0;
}
.rich-text-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
  font-size: 14px;
}
.rich-text-content :deep(th) {
  background-color: #f5f7fa;
  font-weight: bold;
  text-align: center;
}
.rich-text-content :deep(th),
.rich-text-content :deep(td) {
  border: 1px solid #ebeef5;
  padding: 12px 8px;
}
.rich-text-content :deep(p) {
  margin-bottom: 10px;
}
</style>
