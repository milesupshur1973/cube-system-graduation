<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 20px">
    <div
      style="
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        justify-content: space-between;
      "
    >
      <div style="display: flex; align-items: center; gap: 10px">
        <h2 style="color: #409eff; margin: 0">
          <el-icon><EditPen /></el-icon> 成绩录入
        </h2>
        <el-tag v-if="compStatus === 3" type="info">已结束/归档</el-tag>
        <el-tag v-else type="success">进行中</el-tag>
      </div>
      
      <div>
        <el-button 
          v-if="compStatus !== 3" 
          type="danger" 
          @click="handleEndCompetition"
        >
          结束比赛并发布成绩
        </el-button>
        <el-button @click="$router.back()">返回详情页</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-tabs
        v-model="activeEventId"
        type="card"
        @tab-change="handleEventChange"
      >
        <el-tab-pane
          v-for="evt in eventList"
          :key="evt.eventId"
          :label="evt.eventName"
          :name="evt.eventId"
        />
      </el-tabs>

      <div style="margin-top: 10px">
        <el-table :data="competitors" v-loading="loading" border stripe>
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="displayId" label="WCA ID" width="120" />

          <el-table-column label="成绩状态" min-width="200">
            <template #default="scope">
              <div v-if="scope.row.rawResult">
                <el-tag type="success" effect="dark">已录入</el-tag>
                <span style="margin-left: 10px; font-weight: bold">
                  最佳: {{ formatTime(scope.row.rawResult.best) }}
                </span>
              </div>
              <div v-else>
                <el-tag type="info" effect="plain">未录入</el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="openDialog(scope.row)"
              >
                录入成绩
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="录入成绩" width="550px">
      <div style="margin-bottom: 20px">
        <p>
          <strong>选手：</strong> {{ currentCompetitor.name }} ({{
            currentCompetitor.displayId
          }})
        </p>
        <p><strong>项目：</strong> {{ getEventName(activeEventId) }}</p>
        <el-alert
          title="录入格式：分钟 : 秒.毫秒 (例如 1分 12.34秒，无分钟填0)"
          type="success"
          :closable="false"
          show-icon
        />
      </div>

      <el-form label-width="60px">
        <el-form-item v-for="i in 5" :key="i" :label="`第${i}次`">
          <el-row :gutter="5" align="middle">
            <el-col :span="6">
              <el-input-number
                v-model="inputMap['value' + i].min"
                :min="0"
                :max="60"
                controls-position="right"
                placeholder="0"
                :disabled="dnfFlags['value' + i]"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="1" style="text-align: center; color: #606266"
              >分</el-col
            >

            <el-col :span="1" style="text-align: center; font-weight: bold"
              >:</el-col
            >

            <el-col :span="9">
              <el-input-number
                v-model="inputMap['value' + i].sec"
                :min="0"
                :max="59.99"
                :precision="2"
                :step="0.01"
                controls-position="right"
                placeholder="12.34"
                :disabled="dnfFlags['value' + i]"
                style="width: 100%"
              />
            </el-col>
            <el-col :span="1" style="text-align: center; color: #606266"
              >秒</el-col
            >

            <el-col :span="6" style="padding-left: 10px">
              <el-checkbox
                v-model="dnfFlags['value' + i]"
                label="DNF"
                style="color: #f56c6c; font-weight: bold"
                border
                size="small"
                @change="(val) => handleDnfChange(val, i)"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitScore" :loading="submitting">
            保存成绩
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { EditPen } from "@element-plus/icons-vue";
import { getCompetitionBySlug, getCompetitionEvents, updateCompetitionStatus } from "@/api/competition";
import { getCompetitors } from "@/api/registration";
import request from "@/utils/request"; // 直接引入request发简单的save请求，或者你可以封装到 api/result.js
import { useEventStore } from "@/stores/event";
import { getRankings, getPersonPBs, getResultList } from "@/api/result";

const route = useRoute();
const slug = route.params.slug;
const eventStore = useEventStore();
const endDate = ref("");

const competitionId = ref(null);
const eventList = ref([]);
const activeEventId = ref("");
const competitors = ref([]); // 当前项目下的选手列表
const loading = ref(false);
const compStatus = ref(0);

// 弹窗相关
const dialogVisible = ref(false);
const submitting = ref(false);
const currentCompetitor = ref({});
const scoreForm = reactive({
  value1: 0,
  value2: 0,
  value3: 0,
  value4: 0,
  value5: 0,
});

// DNF 标记状态
const dnfFlags = reactive({
  value1: false,
  value2: false,
  value3: false,
  value4: false,
  value5: false,
});

const getEventName = (id) => eventStore.getEventName(id);

// 1. 初始化数据
const init = async () => {
  try {
    // 1.1 先拿比赛ID
    const compRes = await getCompetitionBySlug(slug);
    if (compRes.data.code !== 200) return;
    competitionId.value = compRes.data.data.id;

    // 保存结束日期 判断是否提前归档
    endDate.value = compRes.data.data.endDate;

    // 获取当前比赛状态，否则按钮显示逻辑不生效
    compStatus.value = compRes.data.data.status;

    // 1.2 拿开设的项目
    const eventRes = await getCompetitionEvents(competitionId.value);
    if (eventRes.data.code === 200) {
      eventList.value = eventRes.data.data.map((e) => ({
        eventId: e.eventId,
        eventName: getEventName(e.eventId),
      }));
      // 默认选中第一个
      if (eventList.value.length > 0) {
        activeEventId.value = eventList.value[0].eventId;
        loadCompetitors();
      }
    }
  } catch (e) {
    console.error(e);
  }
};

// 2. 加载该项目的选手
const loadCompetitors = async () => {
  loading.value = true;
  try {
    const params = { page: 1, size: 1000 };

    // 并行发送两个请求：拿人、拿成绩
    const [userRes, resultRes] = await Promise.all([
      getCompetitors(competitionId.value, params),
      getResultList(competitionId.value, activeEventId.value),
    ]);

    if (userRes.data.code === 200) {
      const allCompetitors = userRes.data.data.records;
      const allResults = resultRes.data.code === 200 ? resultRes.data.data : [];

      // 过滤 + 合并
      competitors.value = allCompetitors
        .filter(
          (user) => user.eventIds && user.eventIds.includes(activeEventId.value)
        )
        .map((user) => {
          // 在成绩列表里找这个人的成绩
          const match = allResults.find((r) => r.userId === user.id);
          return {
            ...user,
            rawResult: match || null, // 把成绩对象挂载到选手身上
          };
        });
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const inputMap = reactive({
  value1: { min: 0, sec: 0 },
  value2: { min: 0, sec: 0 },
  value3: { min: 0, sec: 0 },
  value4: { min: 0, sec: 0 },
  value5: { min: 0, sec: 0 },
});

// 切换项目
const handleEventChange = () => {
  loadCompetitors();
};

// 打开录入弹窗
// 打开录入弹窗
const openDialog = (row) => {
  currentCompetitor.value = row;
  // 1. 无论有没有成绩，先强制将所有输入框和 DNF 标记重置为“空/0”状态
  // 这样可以防止上一个人的数据残留
  for (let i = 1; i <= 5; i++) {
    dnfFlags["value" + i] = false;
    inputMap["value" + i].min = 0;
    inputMap["value" + i].sec = 0;
  }

  // 2. 如果该选手有成绩，再用成绩去覆盖上面的默认值
  if (row.rawResult) {
    for (let i = 1; i <= 5; i++) {
      const ms = row.rawResult["value" + i]; // 获取数据库里的毫秒数

      if (ms === -1) {
        // --- 情况A: 是 DNF ---
        dnfFlags["value" + i] = true; 
        // min 和 sec 保持为 0，不用动
      } else if (ms > 0) {
        // --- 情况B: 是有效成绩 ---
        inputMap["value" + i].min = Math.floor(ms / 60000);
        inputMap["value" + i].sec = (ms % 60000) / 1000;
      } 
      // --- 情况C: ms == 0 (未录入)，保持上面重置的 0 即可
    }
  }
  dialogVisible.value = true;
};

// 秒转毫秒
const toMs = (seconds) => {
  if (!seconds) return 0;
  return Math.round(seconds * 1000);
};

// 提交成绩
const submitScore = async () => {
  submitting.value = true;
  try {
    const payload = {
      competitionId: competitionId.value,
      eventId: activeEventId.value,
      userId: currentCompetitor.value.id,
      displayId: currentCompetitor.value.displayId,
    };

    for (let i = 1; i <= 5; i++) {
      if (dnfFlags["value" + i]) {
        payload["value" + i] = -1;
      } else {
        // 核心转换公式：分钟*60000 + 秒*1000
        const min = inputMap["value" + i].min || 0;
        const sec = inputMap["value" + i].sec || 0;
        payload["value" + i] = Math.round(min * 60000 + sec * 1000);
      }
    }

    // 发送请求 (后端接收到的就是整数毫秒了)
    const res = await request.post("/result/save", payload);

    if (res.data.code === 200) {
      ElMessage.success("成绩保存成功");
      dialogVisible.value = false;
      loadCompetitors();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("保存失败");
  } finally {
    submitting.value = false;
  }
};

const formatTime = (ms) => {
  if (ms === -1) return "DNF";
  if (!ms || ms <= 0) return "";

  // 1. 计算分钟、秒、毫秒
  const minutes = Math.floor(ms / 60000);
  const seconds = Math.floor((ms % 60000) / 1000);
  const milliseconds = Math.floor((ms % 1000) / 10); // WCA只显示两位小数

  // 2. 补零逻辑
  const sStr = seconds.toString().padStart(2, "0");
  const msStr = milliseconds.toString().padStart(2, "0");

  // 3. 根据是否超过1分钟返回不同格式
  if (minutes > 0) {
    return `${minutes}:${sStr}.${msStr}`;
  } else {
    // 小于1分钟，秒数不补零 (例如 9.86)
    return `${seconds}.${msStr}`;
  }
};

// 处理 DNF 勾选变化
const handleDnfChange = (checked, index) => {
  // 如果勾选了 DNF (checked 为 true)
  if (checked) {
    // 将对应行的分钟和秒数归零
    inputMap['value' + index].min = 0;
    inputMap['value' + index].sec = 0;
  }
};

//结束比赛的方法
const handleEndCompetition = async () => {
  // 1. 获取当前日期 (格式化为 YYYY-MM-DD 字符串，方便比较)
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const todayStr = `${year}-${month}-${day}`;

  // 2. 默认提示文案 (正常归档)
  let confirmMsg = "结束比赛后，成绩将向所有选手公开，且主办方无法再修改成绩。\n确认所有成绩都已录入无误吗？";
  let confirmTitle = "结束比赛确认";
  let confirmType = "warning";

  // 3. 判断：如果 今天 < 结束日期，说明是“提前归档”
  if (endDate.value && todayStr < endDate.value) {
    confirmMsg = '当前日期尚未到达比赛结束日期。是否确认提前结束比赛并发布成绩？';
    confirmTitle = '提前归档警告';
    // 可以换个颜色提示，比如 error 红色，或者保持 warning
    // confirmType = "warning";
  }

  try {
    await ElMessageBox.confirm(
        confirmMsg,
        confirmTitle,
        {
          confirmButtonText: "确认结束",
          cancelButtonText: "取消",
          type: confirmType,
        }
    );

    // 用户点击确认后，发送请求
    const res = await updateCompetitionStatus({
      id: competitionId.value,
      status: 3 // 设置为 3 (已结束)
    });

    if (res.data.code === 200) {
      ElMessage.success("比赛已结束，成绩已发布！");
      compStatus.value = 3;
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    // 用户点击取消，什么都不做
  }
}

onMounted(() => init());
</script>
