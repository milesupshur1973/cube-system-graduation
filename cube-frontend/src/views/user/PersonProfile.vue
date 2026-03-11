<template>
  <div class="page-container">
    <el-card shadow="hover" class="user-card">
      <div v-if="userInfo">
        <el-avatar
            :size="100"
            :src="userInfo.avatarUrl"
            class="user-avatar"
        />
        <h1 class="user-name">{{ userInfo.name }}</h1>

        <el-descriptions
            :column="3"
            border
            class="user-descriptions"
        >
          <el-descriptions-item label="WCA ID">
            <el-tag effect="dark">{{ userInfo.displayId }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="地区"
          >{{ userInfo.province }} - {{ userInfo.city }}</el-descriptions-item
          >
          <el-descriptions-item label="性别">
            <span v-if="userInfo.gender === 'M'">男</span>
            <span v-else-if="userInfo.gender === 'F'">女</span>
            <span v-else>保密</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-skeleton v-else :rows="3" animated />
    </el-card>

    <h3 class="section-title">
      个人最好成绩
    </h3>

    <el-card shadow="never">
      <el-table :data="pbList" stripe class="full-width-table" v-loading="loading">
        <el-table-column prop="eventName" label="项目" min-width="150">
          <template #default="scope">
            <span class="event-name-bold">{{ scope.row.eventName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="单次 (Single)" width="220">
          <template #default="scope">
            <div v-if="scope.row.best">
              <div class="pb-time-single">
                {{ formatTime(scope.row.best) }}
              </div>
              <div class="pb-comp-detail">
                @ {{ scope.row.bestCompName }}
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="平均 (Average)" width="220">
          <template #default="scope">
            <div v-if="scope.row.average">
              <div class="pb-time-average">
                {{ formatTime(scope.row.average) }}
              </div>
              <div class="pb-comp-detail">
                @ {{ scope.row.averageCompName }}
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="详情" min-width="100"></el-table-column>
      </el-table>
    </el-card>

    <div class="history-section">
      <h3 class="history-title">
        历史战绩
      </h3>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="historyTab">

        <el-tab-pane label="按比赛查看" name="byCompetition">
          <div v-for="(group, index) in historyByCompetition" :key="index" class="group-container">

            <div class="group-header-comp">
              <el-icon :size="18" color="var(--el-text-color-regular)"><Trophy /></el-icon>
              <el-text size="large" tag="b" class="group-title-comp">
                {{ group.competitionName }}
              </el-text>
              <el-tag type="info" effect="plain" round>{{ group.date }}</el-tag>
            </div>

            <el-table :data="group.records" border stripe class="full-width-table">
              <el-table-column prop="eventName" label="项目" width="120">
                <template #default="scope">
                  <span class="event-name-bold">{{ scope.row.eventName }}</span>
                </template>
              </el-table-column>

              <el-table-column label="最佳" width="110" align="right">
                <template #default="scope">
                  <el-text tag="b">{{ formatTime(scope.row.best) }}</el-text>
                </template>
              </el-table-column>

              <el-table-column label="平均" width="110" align="right">
                <template #default="scope">
                  <el-text tag="b" :type="scope.row.average > 0 ? 'primary' : ''">
                    {{ formatTime(scope.row.average) }}
                  </el-text>
                </template>
              </el-table-column>

              <el-table-column label="详情" min-width="200">
                <template #default="scope">
                  <el-text type="info" class="detail-text">
                    {{ formatDetails(scope.row) }}
                  </el-text>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="按项目查看" name="byEvent">
          <div v-for="(group, index) in historyByEvent" :key="index" class="group-container">

            <div class="group-header-event">
              <el-icon :size="18" color="var(--el-color-primary)"><Grid /></el-icon>
              <el-text size="large" tag="b" type="primary">
                {{ group.eventName }}
              </el-text>
            </div>

            <el-table :data="group.records" border stripe class="full-width-table">
              <el-table-column label="比赛名称" min-width="200">
                <template #default="scope">
                  <div>
                    <el-text tag="b">{{ scope.row.competitionName }}</el-text>
                  </div>
                  <div>
                    <el-text size="small" type="info">{{ scope.row.competitionDate }}</el-text>
                  </div>
                </template>
              </el-table-column>

              <el-table-column label="最佳" width="110" align="right">
                <template #default="scope">
                  <el-text tag="b">{{ formatTime(scope.row.best) }}</el-text>
                </template>
              </el-table-column>

              <el-table-column label="平均" width="110" align="right">
                <template #default="scope">
                  <el-text tag="b" :type="scope.row.average > 0 ? 'primary' : ''">
                    {{ formatTime(scope.row.average) }}
                  </el-text>
                </template>
              </el-table-column>

              <el-table-column label="详情" min-width="200">
                <template #default="scope">
                  <el-text type="info" class="detail-text">
                    {{ formatDetails(scope.row) }}
                  </el-text>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { ElMessage } from "element-plus";
import { getPublicUser } from "@/api/user";
import { getPersonPBs,getPersonHistory } from "@/api/result";

const route = useRoute();
const userInfo = ref(null);
const pbList = ref([]);
const loading = ref(false);
const historyTab = ref("byCompetition"); // 默认选中的 Tab
const historyList = ref([]); // 存储后端返回的原始列表
const historyByCompetition = ref([]); // 分组后的数据
const historyByEvent = ref([]); // 分组后的数据

const formatTime = (ms) => {
  if (ms === -1) return "DNF";
  if (!ms || ms <= 0) return "";

  const minutes = Math.floor(ms / 60000);
  const seconds = Math.floor((ms % 60000) / 1000);
  const milliseconds = Math.floor((ms % 1000) / 10); // 保留两位小数

  // 秒数和毫秒数补零
  const sStr = seconds.toString().padStart(2, '0');
  const msStr = milliseconds.toString().padStart(2, '0');

  if (minutes > 0) {
    return `${minutes}:${sStr}.${msStr}`;
  } else {
    // 小于1分钟，秒数不补零 (例如 9.86)
    return `${seconds}.${msStr}`;
  }
};

const loadData = async (displayId) => {
  if (!displayId) return;
  loading.value = true;
  userInfo.value = null;
  pbList.value = [];
  historyList.value = []; // 清空

  try {
    // 并行请求：用户信息、PB、历史成绩
    const [userRes, pbRes, historyRes] = await Promise.all([
      getPublicUser(displayId),
      getPersonPBs(displayId),
      getPersonHistory(displayId) // 新增请求
    ]);

    if (userRes.data.code === 200) userInfo.value = userRes.data.data;
    if (pbRes.data.code === 200) pbList.value = pbRes.data.data;

    // 处理历史成绩
    if (historyRes.data.code === 200) {
      historyList.value = historyRes.data.data;
      processHistory(historyList.value);
    }

  } catch (e) {
    console.error(e);
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
};

// 增加一个格式化详情的函数 (复用之前的逻辑)
const formatDetails = (row) => {
  const times = [row.value1, row.value2, row.value3, row.value4, row.value5];
  return times.map(t => (t !== 0 ? formatTime(t) : "")).join("  ");
};

// 分组处理逻辑
const processHistory = (list) => {
  // 1. 按比赛分组
  const compMap = new Map();
  list.forEach(item => {
    // 这里的 Key 用 比赛名+日期 确保唯一，也可以用 competitionId 如果后端传了
    const key = item.competitionName;
    if (!compMap.has(key)) {
      compMap.set(key, {
        competitionName: item.competitionName,
        date: item.competitionDate,
        records: []
      });
    }
    compMap.get(key).records.push(item);
  });
  historyByCompetition.value = Array.from(compMap.values());

  // 2. 按项目分组
  const eventMap = new Map();
  list.forEach(item => {
    const key = item.eventName;
    if (!eventMap.has(key)) {
      eventMap.set(key, { eventName: item.eventName, records: [] });
    }
    eventMap.get(key).records.push(item);
  });
  historyByEvent.value = Array.from(eventMap.values());
};

onMounted(() => {
  loadData(route.params.displayId);
});

// 监听路由变化（比如从 A选手页面 跳转到 B选手页面）
watch(
    () => route.params.displayId,
    (newId) => {
      if (newId) loadData(newId);
    }
);
</script>

<style scoped>
.page-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
}

.user-card {
  margin-bottom: 25px;
  text-align: center;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-name {
  margin: 0 0 10px 0;
  color: #303133;
}

.user-descriptions {
  margin-top: 20px;
  max-width: 800px;
  margin: 20px auto;
}

.section-title {
  border-left: 4px solid #409eff;
  padding-left: 10px;
  margin-bottom: 20px;
  color: #303133;
}

.full-width-table {
  width: 100%;
}

.event-name-bold {
  font-weight: bold;
}

.pb-time-single {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.pb-comp-detail {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.pb-time-average {
  font-weight: bold;
  color: #67c23a;
  font-size: 16px;
}

.history-section {
  margin-top: 30px;
  margin-bottom: 20px;
}

.history-title {
  border-left: 4px solid #409eff;
  padding-left: 10px;
  color: #303133;
}

.group-container {
  margin-bottom: 30px;
}

.group-header-comp {
  background-color: var(--el-fill-color-light);
  padding: 12px 15px;
  border-radius: 4px;
  border-left: 4px solid var(--el-text-color-secondary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-title-comp {
  color: var(--el-text-color-primary);
}

.detail-text {
  font-family: monospace;
}

.group-header-event {
  background-color: var(--el-color-primary-light-9);
  padding: 12px 15px;
  border-radius: 4px;
  border-left: 4px solid var(--el-color-primary);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>