<template>
  <div style="padding: 20px">
    <div
      v-if="!userStore.userInfo.id"
      style="text-align: center; margin-top: 50px"
    >
      <el-empty description="请先登录后报名">
        <el-button type="primary" @click="$router.push('/login')"
          >去登录</el-button
        >
      </el-empty>
    </div>

    <div v-else style="max-width: 800px; margin: 0 auto">
      <div style="text-align: center; margin-bottom: 30px">
        <h2 style="color: #409eff">
          {{ isUpdate ? "修改报名信息" : "在线报名" }}
        </h2>
        <p style="color: #666; font-size: 14px">
          报名时间：{{ formatDate(compInfo.regStartTime) }} ~
          {{ formatDate(compInfo.regEndTime) }}
        </p>
      </div>

      <el-card shadow="never">
        <template #header>
          <div style="font-weight: bold">请选择参赛项目</div>
        </template>

        <el-form label-position="top">
          <el-form-item>
            <el-checkbox-group v-model="selectedEvents">
              <el-row :gutter="20">
                <el-col
                  :span="6"
                  v-for="event in allEvents"
                  :key="event.eventId"
                  style="margin-bottom: 15px"
                >
                  <el-checkbox
                    :label="event.eventId"
                    border
                    style="width: 100%"
                  >
                    {{ getEventName(event.eventId) }}
                  </el-checkbox>
                </el-col>
              </el-row>
            </el-checkbox-group>
          </el-form-item>

          <el-divider />

          <div
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <div>
              <span style="color: #666">已选项目数：</span>
              <span
                style="
                  font-weight: bold;
                  font-size: 18px;
                  color: #f56c6c;
                  margin-right: 10px;
                "
              >
                {{ selectedEvents.length }}
              </span>
            </div>

            <el-button
              type="primary"
              size="large"
              @click="handleSubmit"
              :loading="submitting"
            >
              {{ isUpdate ? "更新报名" : "提交报名" }}
            </el-button>
          </div>
        </el-form>
      </el-card>

      <div
        style="
          margin-top: 20px;
          color: #909399;
          font-size: 13px;
          line-height: 1.6;
        "
      >
        <p>
          <el-icon><InfoFilled /></el-icon> 说明：
        </p>
        <p>1. 请务必确认您有能力完成所选项目。</p>
        <p>2. 在报名截止时间前，您可以随时修改报名项目。</p>
        <p>3. 报名成功后，您的名字将立即显示在“选手列表”中。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { useUserStore } from "@/stores/user";
import axios from "axios";
import { ElMessage, ElMessageBox } from "element-plus";
import { InfoFilled } from "@element-plus/icons-vue";
import { getCompetitionEvents } from "@/api/competition";
import { getMyRegistration, submitRegistration } from "@/api/registration";
import { useEventStore } from "@/stores/event";

const props = defineProps(["compInfo"]);
const userStore = useUserStore();
const eventStore = useEventStore();

const allEvents = ref([]); // 比赛开设的所有项目
const selectedEvents = ref([]); // 用户勾选的项目ID
const submitting = ref(false);
const isUpdate = ref(false); // 标记是“新增”还是“修改”

const getEventName = (id) => eventStore.getEventName(id);

// 格式化时间
const formatDate = (str) => (str ? str.replace("T", " ") : "");

// 1. 加载比赛开设的项目
const loadEvents = async () => {
  if (!props.compInfo.id) return;
  try {
    const res = await getCompetitionEvents(props.compInfo.id);
    if (res.data.code === 200) {
      allEvents.value = res.data.data;
    }
  } catch (e) {
    console.error(e);
  }
};

// 2. 检查我是否已经报过名（如果报过，回显数据）
const checkMyRegistration = async () => {
  if (!userStore.userInfo.id || !props.compInfo.id) return;
  try {
    const res = await getMyRegistration(
      props.compInfo.id,
      userStore.userInfo.id
    );
    if (res.data.code === 200 && res.data.data) {
      selectedEvents.value = res.data.data.eventIds || [];
      isUpdate.value = true;
    } else {
      selectedEvents.value = [];
      isUpdate.value = false;
    }
  } catch (e) {
    console.error(e);
  }
};

// 3. 提交报名
const handleSubmit = async () => {
  if (selectedEvents.value.length === 0) {
    return ElMessage.warning("请至少选择一个项目！");
  }

  // 简单的二次确认
  try {
    await ElMessageBox.confirm(
      `您已选择 ${selectedEvents.value.length} 个项目，确定提交吗？`,
      "确认报名",
      { confirmButtonText: "确定", cancelButtonText: "取消", type: "info" }
    );
  } catch {
    return; // 用户点了取消
  }

  submitting.value = true;
  try {
    const payload = {
      competitionId: props.compInfo.id,
      eventIds: selectedEvents.value,
    };
    // 替换 axios.post
    const res = await submitRegistration(payload, userStore.userInfo.id);

    if (res.data.code === 200) {
      ElMessage.success(isUpdate.value ? "修改成功！" : "报名成功！");
      isUpdate.value = true;
    } else {
      ElMessage.error(res.data.msg || "操作失败");
    }
  } catch (e) {
    ElMessage.error("网络异常，请稍后重试");
  } finally {
    submitting.value = false;
  }
};

// 监听 compInfo 变化，一旦有 ID 了就加载数据
watch(
  () => props.compInfo,
  (newVal) => {
    if (newVal.id) {
      loadEvents();
      checkMyRegistration();
    }
  },
  { immediate: true }
);
</script>
