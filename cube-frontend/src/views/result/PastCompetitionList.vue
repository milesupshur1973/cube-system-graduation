<template>
  <div class="page-container">
    <div class="header-wrapper">
      <el-icon :size="24" color="#409eff" class="header-icon"><DataAnalysis /></el-icon>
      <h2 class="header-title">历史赛事成绩</h2>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="年份">
          <el-select v-model="filterYear" placeholder="所有" class="year-select" clearable @change="loadData">
            <el-option v-for="year in yearOptions" :key="year" :label="year + '年'" :value="year.toString()" />
          </el-select>
        </el-form-item>

        <el-form-item label="省份">
          <el-select v-model="filterProvince" placeholder="所有地区" class="province-select" clearable filterable @change="loadData">
            <el-option v-for="item in provinceOptions" :key="item.value" :label="item.label" :value="item.label" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe class="data-table">
        <el-table-column prop="startDate" label="日期" width="150" sortable />

        <el-table-column label="比赛名称" min-width="300">
          <template #default="scope">
            <el-link
                type="primary"
                class="competition-link"
                @click="$router.push(`/results/competition/${scope.row.slug}`)"
            >
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="province" label="地区" width="150">
          <template #default="scope">
            {{ scope.row.province }}
          </template>
        </el-table-column>

        <el-table-column prop="city" label="城市" width="150" />
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadData"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { DataAnalysis } from "@element-plus/icons-vue";
import { getCompetitionList, getCompetitionYears } from "@/api/competition";
import { provinceAndCityData } from "element-china-area-data";

const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(20);

const filterYear = ref("");
const filterProvince = ref("");
const yearOptions = ref([]);
const provinceOptions = provinceAndCityData.map((item) => ({ value: item.value, label: item.label }));

const loadYears = async () => {
  const res = await getCompetitionYears();
  if (res.data.code === 200) yearOptions.value = res.data.data;
};

const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      year: filterYear.value,
      province: filterProvince.value,
      status: 3
    };
    const res = await getCompetitionList(params);
    if (res.data.code === 200) {
      tableData.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadYears();
  loadData();
});
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}

.header-wrapper {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header-icon {
  margin-right: 10px;
}

.header-title {
  margin: 0;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: -18px;
}

.year-select {
  width: 120px;
}

.province-select {
  width: 150px;
}

.data-table {
  width: 100%;
}

.competition-link {
  font-weight: bold;
  font-size: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>