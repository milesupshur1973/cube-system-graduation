<template>
  <div style="max-width: 1200px; margin: 20px auto">
    <h2 style="color: #409eff; margin-bottom: 20px">
      <el-icon><Trophy /></el-icon> 赛事列表
    </h2>

    <div style="margin-bottom: 20px">
      <el-space>
        <el-select
          v-model="filterYear"
          placeholder="年份"
          style="width: 120px"
          clearable
        >
          <el-option label="所有年份" value="" />
          <el-option
            v-for="year in yearOptions"
            :key="year"
            :label="year + '年'"
            :value="year.toString()"
          />
        </el-select>

        <el-select
          v-model="filterProvince"
          placeholder="省份"
          style="width: 150px"
          clearable
          filterable
        >
          <el-option label="所有省份" value="" />
          <el-option
            v-for="item in provinceOptions"
            :key="item.value"
            :label="item.label"
            :value="item.label"
          />
        </el-select>

        <el-button type="primary" @click="handleFilter">筛选</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-space>
    </div>

    <el-card shadow="never" :body-style="{ padding: '0' }">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
        @row-click="handleRowClick"
        row-class-name="clickable-row"
      >
        <el-table-column prop="startDate" label="日期" width="150" sortable />

        <el-table-column label="比赛名称" min-width="300">
          <template #default="scope">
            <div
              style="
                display: flex;
                flex-direction: column;
                justify-content: center;
              "
            >
              <div style="display: flex; align-items: center; gap: 10px">
                <span
                  style="font-weight: bold; font-size: 16px; color: #303133"
                >
                  {{ scope.row.name }}
                </span>
              </div>

              <div style="margin-top: 5px; font-size: 12px">
                <div v-if="isUpcoming(scope.row.startDate)">
                  <el-countdown
                    format="D 天 H 小时 m 分"
                    :value="new Date(scope.row.startDate).getTime()"
                    value-style="font-size: 12px; color: #E6A23C"
                  >
                    <template #prefix>
                      <span style="color: #909399; margin-right: 5px"
                        >距离比赛还有:</span
                      >
                    </template>
                  </el-countdown>
                </div>

                <div v-else>
                  <el-tag type="info" size="small" effect="plain"
                    >比赛已结束</el-tag
                  >
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="province" label="省份" width="120" />
        <el-table-column prop="city" label="城市" width="120" />
        <el-table-column prop="location" label="地点" show-overflow-tooltip />
      </el-table>

      <div style="padding: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Trophy } from "@element-plus/icons-vue";
// 引入两个接口：列表 和 年份
import { getCompetitionList, getCompetitionYears } from "@/api/competition";
import { provinceAndCityData } from "element-china-area-data";

const router = useRouter();

// === 变量定义 ===
const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 筛选条件
const filterYear = ref("");
const filterProvince = ref("");
const yearOptions = ref([]); // 存储从后端拿到的年份

// 省份下拉框数据
const provinceOptions = provinceAndCityData.map((item) => {
  return { value: item.value, label: item.label };
});

// === 1. 加载年份列表 ===
const loadYears = async () => {
  try {
    const res = await getCompetitionYears();
    if (res.data.code === 200) {
      // 假设后端返回的是 [2025, 2024, 2023]
      yearOptions.value = res.data.data;
    }
  } catch (e) {
    console.error("加载年份失败", e);
  }
};

// === 2. 加载比赛列表 (核心方法) ===
const loadData = async () => {
  loading.value = true;
  try {
    // 把分页参数和筛选参数一起传给后端
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      year: filterYear.value,
      province: filterProvince.value,
    };

    const res = await getCompetitionList(params);

    if (res.data.code === 200) {
      // MyBatis Plus 分页返回结构：records 是列表，total 是总数
      tableData.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch (e) {
    console.error("加载列表失败", e);
  } finally {
    loading.value = false;
  }
};

// === 事件处理 ===

// 点击筛选按钮
const handleFilter = () => {
  currentPage.value = 1; // 筛选时重置回第一页
  loadData();
};

// 点击重置按钮
const resetFilter = () => {
  filterYear.value = "";
  filterProvince.value = "";
  handleFilter();
};

// 翻页
const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

// 跳转详情
const handleRowClick = (row) => {
  router.push(`/competition/${row.slug}`);
};

// 判断是否近期
const isUpcoming = (dateStr) => {
  const now = new Date().getTime();
  const target = new Date(dateStr).getTime();
  return target > now;
};

// 初始化：先加载年份，再加载列表
onMounted(() => {
  loadYears();
  loadData();
});
</script>

<style>
.clickable-row {
  cursor: pointer;
}
.clickable-row:hover > td {
  background-color: #f5f7fa !important;
}
</style>
