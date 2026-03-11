<template>
  <div class="page-container">
    <div class="filter-container">
      <h2 class="page-title">
        <el-icon><User /></el-icon> 选手列表
      </h2>

      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="省份">
          <el-select
              v-model="queryParams.province"
              placeholder="所有省份"
              clearable
              class="province-select"
              @change="handleSearch"
          >
            <el-option
                v-for="item in provinceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.label"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="性别">
          <el-select
              v-model="queryParams.gender"
              placeholder="所有性别"
              clearable
              class="gender-select"
              @change="handleSearch"
          >
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input
              v-model="queryParams.name"
              placeholder="姓名 或 WCAID"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
          >
            <template #append>
              <el-button @click="handleSearch"
              ><el-icon><Search /></el-icon
              ></el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="never">
      <el-table
          :data="tableData"
          v-loading="loading"
          stripe
          class="full-width-table"
      >
        <el-table-column prop="displayId" label="WCA ID" width="150" sortable />

        <el-table-column prop="name" label="姓名" min-width="150">
          <template #default="scope">
            <el-link
                :underline="false"
                type="primary"
                class="name-link"
                @click="$router.push(`/results/person/${scope.row.displayId}`)"
            >
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="gender" label="性别" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.gender === 'M'" class="gender-male"
            >男</span
            >
            <span v-else-if="scope.row.gender === 'F'" class="gender-female"
            >女</span
            >
            <span v-else>保密</span>
          </template>
        </el-table-column>

        <el-table-column prop="province" label="省份" width="150" />
        <el-table-column prop="city" label="城市" width="150" />

        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button
                size="small"
                @click="$router.push(`/results/person/${scope.row.displayId}`)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSearch"
            @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import axios from "axios";
import { User, Search } from "@element-plus/icons-vue";
import { provinceAndCityData } from "element-china-area-data";
import { getUserList } from "@/api/user";

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);

// 省份数据简化
const provinceOptions = provinceAndCityData.map((item) => ({
  value: item.value,
  label: item.label,
}));

const queryParams = reactive({
  page: 1,
  size: 10,
  name: "",
  gender: "",
  province: "",
});

const formatTime = (str) => {
  return str ? str.toString().split("T")[0] : "";
};

const handleSearch = () => {
  queryParams.page = 1; // 搜索时重置回第一页
  loadData();
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getUserList(queryParams);
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
  loadData();
});
</script>

<style scoped>
/* 页面外层容器 */
.page-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}

/* 顶部搜索过滤区域容器 */
.filter-container {
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

/* 页面大标题 */
.page-title {
  margin-top: 0;
  color: #409eff;
  margin-bottom: 20px;
}

/* 省份选择框宽度 */
.province-select {
  width: 150px;
}

/* 性别选择框宽度 */
.gender-select {
  width: 120px;
}

/* 搜索输入框宽度 */
.search-input {
  width: 200px;
}

/* 表格占满全宽 */
.full-width-table {
  width: 100%;
}

/* 姓名链接加粗 */
.name-link {
  font-weight: bold;
}

/* 男性性别颜色 */
.gender-male {
  color: #409eff;
}

/* 女性性别颜色 */
.gender-female {
  color: #f56c6c;
}

/* 分页器靠右对齐容器 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>