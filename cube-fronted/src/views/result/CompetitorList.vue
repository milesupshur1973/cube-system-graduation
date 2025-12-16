<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 15px">
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
        <el-icon><User /></el-icon> 选手列表
      </h2>

      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="省份">
          <el-select
            v-model="queryParams.province"
            placeholder="所有省份"
            clearable
            style="width: 150px"
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
            style="width: 120px"
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
            style="width: 200px"
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
        style="width: 100%"
      >
        <el-table-column prop="displayId" label="WCA ID" width="150" sortable />

        <el-table-column prop="name" label="姓名" min-width="150">
          <template #default="scope">
            <el-link
              :underline="false"
              type="primary"
              style="font-weight: bold"
              @click="$router.push(`/results/person/${scope.row.displayId}`)"
            >
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="gender" label="性别" width="100" align="center">
          <template #default="scope">
            <span v-if="scope.row.gender === 'M'" style="color: #409eff"
              >男</span
            >
            <span v-else-if="scope.row.gender === 'F'" style="color: #f56c6c"
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

      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
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
