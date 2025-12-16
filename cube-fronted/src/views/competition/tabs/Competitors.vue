<template>
  <div>
    <h3 style="margin-bottom: 20px; color: #409eff">
      <el-icon><User /></el-icon> 参赛选手列表
    </h3>

    <div style="margin-bottom: 15px; color: #666; font-size: 14px">
      共 {{ total }} 位选手报名
    </div>

    <el-table
      :data="competitorList"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column 
        type="index" 
        :index="indexMethod" 
        label="#" 
        width="50" 
        align="center" 
      />
      
      <el-table-column
        prop="name"
        label="姓名"
        width="120"
        show-overflow-tooltip
      />
      <el-table-column prop="gender" label="性别" width="60" align="center">
        <template #default="scope">
          <span v-if="scope.row.gender === 'M'" style="color: #409eff">男</span>
          <span v-else-if="scope.row.gender === 'F'" style="color: #f56c6c"
            >女</span
          >
          <span v-else>保密</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="province"
        label="地区"
        width="100"
        show-overflow-tooltip
      />
      <el-table-column prop="displayId" label="WCAID" width="110" />

      <el-table-column
        v-for="event in eventList"
        :key="event.eventId"
        :label="event.eventName"
        align="center"
        min-width="80"
      >
        <template #header>
          <span style="font-size: 13px">{{ event.name }}</span>
        </template>

        <template #default="scope">
          <div v-if="hasEvent(scope.row, event.eventId)">
            <el-icon color="#67C23A" size="16"><Check /></el-icon>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handleCurrentChange"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import axios from "axios";
import { User, Check } from "@element-plus/icons-vue";
import { getCompetitionEvents } from "@/api/competition";
import { getCompetitors } from "@/api/registration";
import { useEventStore } from "@/stores/event";

const props = defineProps(["compInfo"]);
const competitorList = ref([]);
const eventList = ref([]); 
const eventStore = useEventStore();

const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const getEventName = (eventId) => {
  return eventStore.getEventName(eventId);
};

// 修改点3：新增序号计算方法
// index 是当前页的索引(0-9)，我们需要把它转换成全局索引
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1;
};

const loadData = async () => {
  if (!props.compInfo.id) return;
  loading.value = true;
  try {
    if (eventList.value.length === 0) {
      const eventRes = await getCompetitionEvents(props.compInfo.id);
      if (eventRes.data.code === 200) {
        eventList.value = eventRes.data.data.map((item) => ({
          eventId: item.eventId,
          name: getEventName(item.eventId),
        }));
      }
    }

    const params = {
      page: currentPage.value,
      size: pageSize.value,
    };
    const compRes = await getCompetitors(props.compInfo.id, params);

    if (compRes.data.code === 200) {
      competitorList.value = compRes.data.data.records;
      total.value = compRes.data.data.total; // 后端返回的总数赋值给了 total，模板里直接用 total 即可
    }
  } catch (e) {
    console.error("加载选手列表失败", e);
  } finally {
    loading.value = false;
  }
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

const hasEvent = (row, targetEventId) => {
  if (!row.eventIds) return false;
  return row.eventIds.includes(targetEventId);
};

watch(
  () => props.compInfo,
  (newVal) => {
    if (newVal.id) {
      loadData();
    }
  },
  { immediate: true }
);
</script>