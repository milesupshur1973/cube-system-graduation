<template>
  <div class="main-container">
    <el-breadcrumb separator="/" class="breadcrumb-nav">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>成绩</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/results/competition' }"
        >赛事</el-breadcrumb-item
      >
      <el-breadcrumb-item>{{ compInfo.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never" class="section-card">
      <el-descriptions :title="compInfo.name" :column="3" border>
        <template #extra>
          <el-button
            type="primary"
            link
            @click="$router.push(`/competition/${compInfo.slug}/competitors`)"
          >
            查看选手名单
          </el-button>
        </template>
        <el-descriptions-item label="日期">{{
          compInfo.startDate
        }}</el-descriptions-item>
        <el-descriptions-item label="地点"
          >{{ compInfo.province }} - {{ compInfo.city }}</el-descriptions-item
        >
        <el-descriptions-item label="链接">
          <el-link
            type="primary"
            :href="`/competition/${compInfo.slug}`"
            target="_blank"
          >
            赛事主页 <el-icon><TopRight /></el-icon>
          </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <div class="control-bar">
      <el-space wrap size="large">
        <el-radio-group v-model="viewMode" @change="handleViewChange">
          <el-radio-button label="winner">冠军</el-radio-button>
          <el-radio-button label="top3">前三</el-radio-button>
          <el-radio-button label="all">单项全览</el-radio-button>
        </el-radio-group>

        <el-select
          v-if="viewMode === 'all'"
          v-model="activeEventId"
          placeholder="选择项目"
          class="event-select"
          @change="handleViewChange"
        >
          <el-option
            v-for="evt in eventList"
            :key="evt.eventId"
            :label="evt.eventName"
            :value="evt.eventId"
          />
        </el-select>
      </el-space>
    </div>

    <div v-loading="loading">
      <el-card shadow="never" v-if="viewMode === 'all'">
        <template #header>
          <span class="card-header-text"
            >{{ eventStore.getEventName(activeEventId) }} - 完整排名</span
          >
        </template>
        <el-table :data="allResultsData" stripe style="width: 100%">
          <el-table-column label="排名" width="80" align="center">
            <template #default="scope">
              <span v-if="scope.row.rank <= 3" class="rank-highlight">{{
                scope.row.rank
              }}</span>
              <span v-else>{{ scope.row.rank }}</span>
            </template>
          </el-table-column>
          <el-table-column label="选手" min-width="160">
            <template #default="scope">
              <el-link
                type="primary"
                @click="
                  $router.push(`/results/person/${scope.row.userDisplayId}`)
                "
              >
                {{ scope.row.userName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="最佳 (Best)" width="120" align="right">
            <template #default="scope">
              <span class="time-bold">{{ formatTime(scope.row.best) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="平均 (Avg)" width="120" align="right">
            <template #default="scope">
              <span
                class="time-bold"
                :class="{ 'text-primary': scope.row.average > 0 }"
              >
                {{ formatTime(scope.row.average) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            prop="userProvince"
            label="地区"
            width="100"
            align="center"
          />
          <el-table-column label="详情" min-width="280">
            <template #default="scope">
              <span class="details-text">{{ formatDetails(scope.row) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" v-else-if="viewMode === 'winner'">
        <template #header>
          <span class="card-header-text">全项目冠军一览</span>
        </template>

        <el-table :data="championData" border stripe style="width: 100%">
          <el-table-column label="项目" width="140">
            <template #default="scope">
              <div
                style="display: flex; align-items: center; font-weight: bold"
              >
                <el-icon class="header-icon"><Grid /></el-icon>
                {{ scope.row.eventName }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="选手" min-width="150">
            <template #default="scope">
              <el-link
                type="primary"
                @click="
                  $router.push(`/results/person/${scope.row.userDisplayId}`)
                "
              >
                {{ scope.row.userName }}
              </el-link>
            </template>
          </el-table-column>

          <el-table-column label="最好" width="120" align="right">
            <template #default="scope">
              <span class="time-bold">{{ formatTime(scope.row.best) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="平均" width="120" align="right">
            <template #default="scope">
              <span class="time-bold text-primary">{{
                formatTime(scope.row.average)
              }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="userProvince"
            label="地区"
            width="100"
            align="center"
          />

          <el-table-column label="详情" min-width="260">
            <template #default="scope">
              <span class="details-text">{{ formatDetails(scope.row) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div v-else>
        <el-empty
          v-if="groupedResults.length === 0 && !loading"
          description="暂无数据"
        />

        <div
          v-for="group in groupedResults"
          :key="group.eventId"
          class="group-container"
        >
          <div class="group-header">
            <el-icon class="header-icon"><Grid /></el-icon>
            {{ group.eventName }}
          </div>

          <el-table :data="group.rows" border style="width: 100%">
            <el-table-column label="排名" width="80" align="center">
              <template #default="scope">
                <span v-if="scope.row.rank === 1" class="rank-1">1</span>
                <span v-else-if="scope.row.rank === 2" class="rank-2">2</span>
                <span v-else-if="scope.row.rank === 3" class="rank-3">3</span>
                <span v-else>{{ scope.row.rank }}</span>
              </template>
            </el-table-column>

            <el-table-column label="选手">
              <template #default="scope">
                <el-link
                  type="primary"
                  @click="
                    $router.push(`/results/person/${scope.row.userDisplayId}`)
                  "
                >
                  {{ scope.row.userName }}
                </el-link>
              </template>
            </el-table-column>

            <el-table-column label="最佳" width="120" align="right">
              <template #default="scope">
                <span class="time-bold">{{ formatTime(scope.row.best) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="平均" width="120" align="right">
              <template #default="scope">
                <span class="time-bold text-primary">{{
                  formatTime(scope.row.average)
                }}</span>
              </template>
            </el-table-column>

            <el-table-column
              prop="userProvince"
              label="地区"
              width="100"
              align="center"
            />

            <el-table-column label="详情" min-width="250">
              <template #default="scope">
                <span class="details-text">{{ formatDetails(scope.row) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { TopRight, Grid } from "@element-plus/icons-vue";
import { getCompetitionBySlug, getCompetitionEvents } from "@/api/competition";
import { getResultList } from "@/api/result";
import { useEventStore } from "@/stores/event";

const route = useRoute();
const slug = route.params.slug;
const eventStore = useEventStore();

const compInfo = ref({});
const eventList = ref([]);
const activeEventId = ref("");
const loading = ref(false);
const viewMode = ref("top3"); // 默认视图

// === 数据存储 ===
const allResultsData = ref([]); // 'all' 模式：单项所有数据
const championData = ref([]); // 'winner' 模式：所有项目的冠军聚合数组
const groupedResults = ref([]); // 'top3' 模式：按项目分组的数组

// === 格式化工具函数 ===
const formatTime = (ms) => {
  if (ms === -1) return "DNF";
  if (ms === -2) return "DNS";
  if (!ms || ms <= 0) return "";
  const minutes = Math.floor(ms / 60000);
  const seconds = Math.floor((ms % 60000) / 1000);
  const milliseconds = Math.floor((ms % 1000) / 10);
  const sStr = seconds.toString().padStart(2, "0");
  const msStr = milliseconds.toString().padStart(2, "0");
  return minutes > 0 ? `${minutes}:${sStr}.${msStr}` : `${seconds}.${msStr}`;
};

const formatDetails = (row) => {
  const times = [row.value1, row.value2, row.value3, row.value4, row.value5];
  return times.map((t) => (t !== 0 ? formatTime(t) : "")).join("   ");
};

// === 核心：修复后的排序逻辑 ===
const compareResults = (a, b) => {
  // 1. 判断是否有有效平均 (大于0)
  const validAvgA = a.average && a.average > 0;
  const validAvgB = b.average && b.average > 0;

  // 2. 如果两人都有有效平均，直接比平均
  if (validAvgA && validAvgB) {
    if (a.average !== b.average) return a.average - b.average;
    // 平均一样，比单次
    const bestA = a.best > 0 ? a.best : Infinity;
    const bestB = b.best > 0 ? b.best : Infinity;
    if (bestA === Infinity && bestB === Infinity) return 0; // 都是DNF，平级
    return bestA - bestB;
  }

  // 3. 如果只有一个有有效平均，有平均的排前面
  if (validAvgA && !validAvgB) return -1;
  if (!validAvgA && validAvgB) return 1;

  // 4. 如果都没有有效平均 (比如 Bo3 项目，或者 Ao5 爆掉了)，比单次
  // 注意：单次 -1 (DNF) 或 0 (未录入) 都要视为无穷大
  const bestA = a.best > 0 ? a.best : Infinity;
  const bestB = b.best > 0 ? b.best : Infinity;
  return bestA - bestB;
};

// === 加载逻辑 ===
const loadResults = async () => {
  if (!compInfo.value.id) return;
  loading.value = true;
  // 清空现有数据，防止混淆
  allResultsData.value = [];
  championData.value = [];
  groupedResults.value = [];

  try {
    // ------------------------------------------------
    // 情况 1: 单项全览模式 ('all')
    // ------------------------------------------------
    if (viewMode.value === "all") {
      if (!activeEventId.value) return;
      const res = await getResultList(compInfo.value.id, activeEventId.value);
      if (res.data.code === 200) {
        const data = res.data.data;

        // ★★★ 使用修复后的排序函数 ★★★
        data.sort(compareResults);

        // 添加排名索引和项目名称
        data.forEach((item, index) => {
          item.rank = index + 1;
          item.eventName = eventStore.getEventName(activeEventId.value);
        });
        allResultsData.value = data;
      }
    }
    // ------------------------------------------------
    // 情况 2 & 3: 冠军模式 ('winner') 和 前三模式 ('top3')
    // ------------------------------------------------
    else {
      // 1. 并发请求所有项目的成绩
      const promises = eventList.value.map((evt) =>
        getResultList(compInfo.value.id, evt.eventId).then((res) => ({
          eventId: evt.eventId,
          eventName: evt.eventName,
          data: res.data.code === 200 ? res.data.data : [],
        }))
      );
      const results = await Promise.all(promises);

      const winnersTemp = [];
      const groupsTemp = [];

      // 2. 遍历每个项目的结果
      results.forEach(({ eventId, eventName, data }) => {
        if (data.length === 0) return;

        // ★★★ 使用修复后的排序函数 ★★★
        data.sort(compareResults);

        // 4. 为原始数据打上排名标记
        data.forEach((item, idx) => (item.rank = idx + 1));

        // --- 分支处理：冠军模式 ---
        if (viewMode.value === "winner") {
          // 取第一名
          const winner = data[0];
          if (winner) {
            winner.eventName = eventName;
            winner.eventId = eventId;
            winnersTemp.push(winner);
          }
        }
        // --- 分支处理：前三模式 ---
        else {
          // 筛选前三名
          const top3 = data.filter((i) => i.rank <= 3);
          if (top3.length > 0) {
            groupsTemp.push({ eventId, eventName, rows: top3 });
          }
        }
      });
      championData.value = winnersTemp;
      groupedResults.value = groupsTemp;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const init = async () => {
  const compRes = await getCompetitionBySlug(slug);
  if (compRes.data.code === 200) {
    compInfo.value = compRes.data.data;
    const evtRes = await getCompetitionEvents(compInfo.value.id);
    if (evtRes.data.code === 200) {
      eventList.value = evtRes.data.data.map((e) => ({
        eventId: e.eventId,
        eventName: eventStore.getEventName(e.eventId),
      }));
      if (eventList.value.length > 0)
        activeEventId.value = eventList.value[0].eventId;
      loadResults();
    }
  }
};

const handleViewChange = () => {
  loadResults();
};

onMounted(() => {
  if (eventStore.eventList.length === 0) eventStore.loadEvents().then(init);
  else init();
});
</script>

<style scoped>
/* 布局容器 */
.main-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}

/* 导航 */
.breadcrumb-nav {
  margin-bottom: 20px;
  font-size: 14px;
}

/* 顶部卡片 & 控制栏 */
.section-card {
  margin-bottom: 20px;
}
.control-bar {
  margin-bottom: 20px;
}
.event-select {
  width: 180px;
}

/* 字体和颜色 - 通用 */
.card-header-text {
  font-weight: bold;
}
.text-primary {
  color: #409eff;
}
.time-bold {
  font-weight: bold;
}

/* 详情数据的等宽字体 */
.details-text {
  font-family: "Consolas", "Monaco", monospace;
  color: #909399;
}

/* 排名颜色 */
.rank-highlight {
  color: #f56c6c;
  font-weight: bold;
}
/* 分组模式下的排名颜色 */
.rank-1 {
  color: #f56c6c;
  font-weight: bold;
}
.rank-2 {
  color: #e6a23c;
  font-weight: bold;
}
.rank-3 {
  color: #409eff;
  font-weight: bold;
}

/* 分组标题 */
.group-container {
  margin-bottom: 20px;
}
.group-header {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #303133;
}
.header-icon {
  margin-right: 5px;
  vertical-align: middle;
}
</style>
