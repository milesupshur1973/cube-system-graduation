<template>
  <div class="registration-container">
    <div
        v-if="!userStore.userInfo.id"
        class="empty-state"
    >
      <el-empty description="请先登录后报名">
        <el-button type="primary" @click="$router.push('/login')"
        >去登录</el-button
        >
      </el-empty>
    </div>

    <div v-else-if="compInfo.status !== 1" class="empty-state">
      <el-empty :description="getStatusText(compInfo.status)" />
    </div>

    <div v-else class="main-content">
      <div class="header-text">
        <h2 class="title-text">
          {{ isUpdate ? "修改报名信息" : "在线报名" }}
        </h2>
        <p class="time-text">
          报名时间：{{ formatDate(compInfo.regStartTime) }} ~
          {{ formatDate(compInfo.regEndTime) }}
        </p>
      </div>

      <el-card shadow="never">
        <template #header>
          <div class="card-header">请选择参赛项目</div>
        </template>

        <el-form label-position="top">
          <el-form-item>
            <el-checkbox-group v-model="selectedEvents" style="width: 100%">
              <el-row :gutter="20">
                <el-col
                    :span="6"
                    v-for="event in allEvents"
                    :key="event.eventId"
                    class="checkbox-col"
                >
                  <el-checkbox
                      :label="event.eventId"
                      border
                      class="event-checkbox"
                      :title="getEventName(event.eventId)"
                  >
                    {{ getEventName(event.eventId) }}
                  </el-checkbox>
                </el-col>
              </el-row>
            </el-checkbox-group>
          </el-form-item>

          <el-divider />

          <div class="action-bar">
            <div class="count-info">
              <span class="count-label">已选项目数：</span>
              <span class="count-number">
                {{ selectedEvents.length }}
              </span>
            </div>

            <el-button
                v-if="isUpdate"
                type="danger"
                size="large"
                @click="handleCancelRegistration"
            >
              取消报名
            </el-button>

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

      <div class="tips-area">
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
import { getMyRegistration, submitRegistration, cancelRegistration } from "@/api/registration";
import { useEventStore } from "@/stores/event";

const props = defineProps(["compInfo"]);
const userStore = useUserStore();
const eventStore = useEventStore();

const allEvents = ref([]); // 比赛开设的所有项目
const selectedEvents = ref([]); // 用户勾选的项目ID
const submitting = ref(false);
const isUpdate = ref(false); // 标记是“新增”还是“修改”

const getEventName = (id) => eventStore.getEventName(id);

// 根据比赛状态获取提示文本
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return "比赛正在审核中，暂未开启报名";
    case 2:
      return "比赛正在进行中，报名已截止";
    case 3:
      return "比赛已结束，报名已截止";
    case 4:
      return "比赛未通过审核，无法报名";
    default:
      return "当前比赛未开启报名通道";
  }
};

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
  // 如果是修改报名且未选择项目，询问是否取消报名
  if (isUpdate.value && selectedEvents.value.length === 0) {
    return handleCancelRegistration();
  }

  // 原有的验证逻辑，仅对首次报名生效
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

// 4. 取消报名
const handleCancelRegistration = async () => {
  try {
    await ElMessageBox.confirm(
        "确定要取消报名吗？此操作不可恢复。",
        "取消报名",
        { confirmButtonText: "确定", cancelButtonText: "取消", type: "warning" }
    );
  } catch {
    return; // 用户点了取消
  }

  submitting.value = true;
  try {
    // 调用取消报名接口
    const res = await cancelRegistration(props.compInfo.id, userStore.userInfo.id);

    if (res.data.code === 200) {
      ElMessage.success("取消报名成功！");
      isUpdate.value = false;
      selectedEvents.value = [];
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

<style scoped>
/* 最外层容器 */
.registration-container {
  padding: 20px;
}

/* 空状态（未登录/未开放报名） */
.empty-state {
  text-align: center;
  margin-top: 50px;
}

/* 主内容区限制宽度居中 */
.main-content {
  max-width: 800px;
  margin: 0 auto;
}

/* 顶部标题区域 */
.header-text {
  text-align: center;
  margin-bottom: 30px;
}

.title-text {
  color: #409eff;
}

.time-text {
  color: #666;
  font-size: 14px;
}

/* 卡片标题 */
.card-header {
  font-weight: bold;
}

/* 多选框列间距 */
.checkbox-col {
  margin-bottom: 15px;
}

/* 修复多选框文本溢出问题：
  1. 将 checkbox 设为 flex 布局并限制 100% 宽度
  2. 穿透修改内部 label 的样式，使其支持单行省略号截断
*/
.event-checkbox {
  width: 100%;
  display: flex;
  align-items: center;
}

.event-checkbox :deep(.el-checkbox__label) {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-left: 8px; /* 根据 Element Plus 默认样式微调，防止贴太紧 */
}

/* 底部操作栏 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
}

/* 已选项目信息（靠左） */
.count-info {
  margin-right: auto;
}

.count-label {
  color: #666;
}

.count-number {
  font-weight: bold;
  font-size: 18px;
  color: #f56c6c;
  margin-right: 10px;
}

/* 底部说明提示区 */
.tips-area {
  margin-top: 20px;
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
}
</style>