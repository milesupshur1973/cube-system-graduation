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

        <el-tab-pane label="成绩趋势图" name="trendChart">
          <div v-if="historyByEvent.length > 0" class="chart-container">
            <div class="chart-controls">
              <el-text tag="b">选择要查看的项目：</el-text>
              <el-select
                  v-model="selectedEvent"
                  placeholder="请选择项目"
                  style="width: 200px; margin-left: 10px;"
                  @change="drawChart"
              >
                <el-option
                    v-for="group in historyByEvent"
                    :key="group.eventName"
                    :label="group.eventName"
                    :value="group.eventName"
                />
              </el-select>
            </div>
            <div ref="chartDom" style="width: 100%; height: 400px; margin-top: 20px;"></div>
          </div>
          <el-empty v-else description="暂无历史成绩数据" />
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import * as echarts from 'echarts';
import { nextTick } from 'vue';
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
const selectedEvent = ref(''); // 当前选中的项目
const chartDom = ref(null); // 图表的 DOM 引用
let myChart = null; // ECharts 实例

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

// 画图方法
const drawChart = () => {
  if (!chartDom.value || !selectedEvent.value) return;

  // 找到当前选中项目的所有记录
  const eventData = historyByEvent.value.find(e => e.eventName === selectedEvent.value);
  if (!eventData || !eventData.records) return;

  // 按照比赛日期从早到晚排序 (保证 X 轴时间是正向的)
  const sortedRecords = [...eventData.records].sort((a, b) => {
    return new Date(a.competitionDate) - new Date(b.competitionDate);
  });

  // 准备 X 轴 (比赛名称/日期) 和 Y 轴 (成绩) 数据
  const xAxisData = [];
  const bestData = [];
  const averageData = [];

  sortedRecords.forEach(item => {
    // X轴显示比赛短名称即可
    xAxisData.push(item.competitionName);

    // Y轴需要数字，我们将毫秒转换为秒 (例如 9500 -> 9.5)
    // 注意：如果是 -1 (DNF) 我们就填 null，让图表断开不显示 DNF
    bestData.push(item.best > 0 ? (item.best / 1000).toFixed(2) : null);
    averageData.push(item.average > 0 ? (item.average / 1000).toFixed(2) : null);
  });

  // 初始化或更新 ECharts
  if (myChart != null && myChart !== "" && myChart !== undefined) {
    myChart.dispose(); // 销毁旧图表
  }
  myChart = echarts.init(chartDom.value);

  // ECharts 配置项 (保持 Element Plus 的清爽风格)
  // ECharts 配置项 (保持 Element Plus 的清爽风格)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        let res = `<b>${params[0].axisValue}</b><br/>`;
        params.forEach(p => {
          if (p.data !== null) {
            res += `${p.marker} ${p.seriesName}: ${p.data} 秒<br/>`;
          }
        });
        return res;
      }
    },
    legend: {
      data: ['单次最佳 (Best)', '平均成绩 (Average)'],
      top: '0%' // 🌟 关键修改 1：将图例明确移到顶部，远离 X 轴文字
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '25%', // 🌟 关键修改 2：把原本的 3% 增大到 25% (或者写绝对数值如 80)，给底部倾斜长文字留足空间
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisLabel: {
        rotate: 35, // 稍微再倾斜一点点
        interval: 0 // 🌟 关键修改 3：强制显示所有的 X 轴标签，防止如果比赛太多 ECharts 自动隐藏部分名字
      }
    },
    yAxis: {
      type: 'value',
      name: '时间 (秒)',
      scale: true // Y轴不强制从0开始，放大成绩波动的视觉效果
    },
    series: [
      {
        name: '单次最佳 (Best)',
        type: 'line',
        data: bestData,
        itemStyle: { color: '#409eff' },
        smooth: true
      },
      {
        name: '平均成绩 (Average)',
        type: 'line',
        data: averageData,
        itemStyle: { color: '#67c23a' },
        smooth: true
      }
    ]
  };

  myChart.setOption(option);
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

// 监听 Tab 切换，解决 ECharts 在隐藏 Tab 中宽度变为 0 的经典 Bug
watch(historyTab, (newTab) => {
  if (newTab === 'trendChart') {
    // 如果用户是第一次点开这个 Tab，且还没选中项目，默认选中他的第一个项目
    if (!selectedEvent.value && historyByEvent.value.length > 0) {
      selectedEvent.value = historyByEvent.value[0].eventName;
    }
    // 等待 DOM 渲染完毕后画图
    nextTick(() => {
      drawChart();
    });
  }
});
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

.chart-container {
  padding: 10px;
}
.chart-controls {
  display: flex;
  align-items: center;
  background-color: var(--el-fill-color-light);
  padding: 15px;
  border-radius: 4px;
}
</style>