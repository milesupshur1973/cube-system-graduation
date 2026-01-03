<template>
  <div style="max-width: 1100px; margin: 20px auto; padding: 0 15px">
    <div
      style="
        margin-bottom: 20px;
        background: #fff;
        padding: 20px;
        border-radius: 4px;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
      "
    >
      <h2 style="margin-top: 0; color: #409eff; margin-bottom: 20px">
        <el-icon><Trophy /></el-icon> 纪录排名
      </h2>

      <el-form :inline="true">
        <el-form-item label="项目">
          <el-select
            v-model="queryParams.eventId"
            placeholder="请选择项目"
            style="width: 130px"
          >
            <el-option
              v-for="evt in eventList"
              :key="evt.id"
              :label="evt.name"
              :value="evt.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="地区">
          <el-select
            v-model="queryParams.region"
            placeholder="全国"
            @change="handleFilter"
            clearable
            style="width: 130px"
          >
            <el-option label="全国" value="" />
            <el-option
              v-for="item in provinceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.label"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="类型">
          <el-radio-group v-model="queryParams.type" @change="handleFilter">
            <el-radio-button label="best">单次</el-radio-button>
            <el-radio-button label="average">平均</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading"
            >刷新</el-button
          >
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table :data="rankList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="rank" label="排名" width="80" align="center">
          <template #default="scope">
            <span
              v-if="scope.row.rank <= 3"
              style="font-weight: bold; color: #f56c6c; font-size: 16px"
            >
              #{{ scope.row.rank }}
            </span>
            <span v-else>{{ scope.row.rank }}</span>
          </template>
        </el-table-column>

        <el-table-column label="姓名" min-width="120">
          <template #default="scope">
            <el-link
              :underline="false"
              type="primary"
              @click="
                $router.push(`/results/person/${scope.row.userDisplayId}`)
              "
            >
              {{ scope.row.userName }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="userDisplayId" label="WCA ID" width="120" />
        <el-table-column prop="userProvince" label="地区" width="100" />

        <el-table-column label="成绩" width="120" align="right">
          <template #default="scope">
            <span
              style="font-weight: bold; font-family: monospace; font-size: 15px"
            >
              {{ formatTime(scope.row.bestScore) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="赛事" min-width="200" align="right">
          <template #default="scope">
            <el-link
              :underline="false"
              type="info"
              style="font-size: 12px"
              @click="$router.push(`/competition/${scope.row.competitionSlug}`)"
            >
              {{ scope.row.competitionName }}
            </el-link>
            <div style="font-size: 12px; color: #ccc">
              {{ scope.row.competitionDate }}
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="padding: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleFilter"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import axios from "axios";
import { Trophy } from "@element-plus/icons-vue";
import { getRankings } from "@/api/result";
import { useEventStore } from "@/stores/event";
import { storeToRefs } from "pinia";
// 引入省份数据
import { provinceAndCityData } from "element-china-area-data";

const loading = ref(false);
const rankList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);
const eventStore = useEventStore();
const { eventList } = storeToRefs(eventStore);

// 提取省份列表
const provinceOptions = provinceAndCityData.map((item) => ({
  value: item.value,
  label: item.label,
}));

const queryParams = reactive({
  eventId: "333",
  type: "average",
  region: "", // 默认空，表示全国
});

const formatTime = (ms) => {
  if (ms === -1) return "DNF";
  if (!ms || ms <= 0) return "";

  // 1. 计算分钟、秒、毫秒
  const minutes = Math.floor(ms / 60000);
  const seconds = Math.floor((ms % 60000) / 1000);
  const milliseconds = Math.floor((ms % 1000) / 10); // WCA只显示两位小数，所以除以10

  // 2. 补零逻辑 (例如 5秒 变成 05)
  const sStr = seconds.toString().padStart(2, '0');
  const msStr = milliseconds.toString().padStart(2, '0');

  // 3. 根据是否超过1分钟返回不同格式
  if (minutes > 0) {
    return `${minutes}:${sStr}.${msStr}`; // 显示 1:15.45
  } else {
    return `${seconds}.${msStr}`;         // 显示 15.45 (秒数不补零，符合习惯)
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      page: currentPage.value,
      size: pageSize.value,
    };
    const res = await getRankings(params);
    if (res.data.code === 200) {
      rankList.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 翻页事件
const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

// 2. 添加监听逻辑
watch(
  () => queryParams.eventId,
  (newId) => {
    // 从 store 的列表中找到当前选中的项目对象
    const targetEvent = eventList.value.find((e) => e.id === newId);

    if (targetEvent) {
      if (targetEvent.format === "bo3") {
        queryParams.type = "best";
      }
    }
    currentPage.value = 1;
    loadData();
  }
);

const handleFilter = () => {
  currentPage.value = 1;
  loadData();
};

onMounted(() => {
  loadData();
});
</script>
