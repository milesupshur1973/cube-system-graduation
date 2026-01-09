<template>
  <el-container class="main-layout">

    <el-header class="layout-header">
      <el-page-header @back="$router.back()" class="w-full">
        <template #content>
          <el-space>
            <el-text tag="b" size="large">成绩录入</el-text>
            <el-divider direction="vertical"/>
            <el-text type="info">{{ competitionName }}</el-text>
            <el-tag v-if="compStatus === 3" type="info" effect="dark" round>已归档</el-tag>
            <el-tag v-else type="success" effect="dark" round>进行中</el-tag>
          </el-space>
        </template>
        <template #extra>
          <el-button v-if="compStatus !== 3" type="danger" plain @click="handleEndCompetition">
            结束比赛
          </el-button>
        </template>
      </el-page-header>
    </el-header>

    <el-container class="no-scroll">
      <el-aside width="220px" class="layout-aside">
        <el-scrollbar>
          <el-menu
              :default-active="activeEventId"
              @select="handleEventSelect"
              class="no-border-menu"
          >
            <el-menu-item-group title="比赛项目">
              <el-menu-item v-for="evt in eventList" :key="evt.eventId" :index="evt.eventId">
                <el-icon>
                  <Menu/>
                </el-icon>
                <span>{{ evt.eventName }}</span>
              </el-menu-item>
            </el-menu-item-group>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-main class="layout-main">
        <el-empty v-if="!activeEventId" description="请在左侧选择一个项目" class="h-full"/>

        <el-card v-else shadow="never" class="content-card">
          <template #header>
            <el-tabs v-model="activeRoundId" @tab-change="handleRoundChange" class="card-tabs">
              <el-tab-pane
                  v-for="round in roundList"
                  :key="round.id"
                  :label="round.roundName"
                  :name="round.id"
              />
            </el-tabs>
          </template>

          <div class="toolbar-section">
            <el-row justify="space-between" align="middle">
              <el-col :span="8">
                <el-input
                    v-model="searchKeyword"
                    placeholder="搜索姓名 / ID"
                    prefix-icon="Search"
                    clearable
                />
              </el-col>
              <el-col :span="16" class="text-right">
                <el-space>
                  <el-text type="info" size="small">人数: {{ tableData.length }}</el-text>
                  <el-divider direction="vertical"/>
                  <el-button
                      v-if="isFirstRound && tableData.length === 0 && compStatus !== 3"
                      type="primary" plain icon="Download"
                      @click="handleInitRound1"
                  >
                    加载报名选手
                  </el-button>
                  <span v-if="!isFinalRound && tableData.length > 0 && compStatus !== 3">
                    <el-button
                        v-if="!nextRoundHasData"
                        type="warning"
                        plain
                        icon="Trophy"
                        @click="openPromoteDialog"
                    >
                      晋级下一轮
                    </el-button>

                    <el-tooltip v-else content="下一轮已有选手" placement="top">
                      <el-button type="success" plain disabled icon="CircleCheck">
                        已完成晋级
                      </el-button>
                    </el-tooltip>
                  </span>
                </el-space>
              </el-col>
            </el-row>
          </div>

          <el-table
              :data="filteredTableData"
              stripe
              v-loading="loading"
              class="flex-table"
          >
            <el-table-column type="index" label="排名" width="60" align="center">
              <template #default="{ row, $index }">
                <strong v-if="row.best > 0">{{ $index + 1 }}</strong>
                <span v-else>-</span>
              </template>
            </el-table-column>

            <el-table-column prop="userName" label="姓名" width="140" show-overflow-tooltip/>
            <el-table-column prop="userDisplayId" label="参赛ID" width="120"/>

            <el-table-column label="五次成绩 (分:秒.毫秒)" min-width="320">
              <template #default="{ row }">
                <el-space size="large" class="mono-font">
                  <span
                      v-for="i in 5"
                      :key="i"
                      :style="{ fontWeight: isBest(row, i) ? 'bold' : 'normal', color: isBest(row, i) ? 'var(--el-color-success)' : 'inherit' }"
                  >
                     {{ formatSingleTime(row['value' + i]) }}
                  </span>
                </el-space>
              </template>
            </el-table-column>

            <el-table-column label="最佳 (Best)" width="120" align="center">
              <template #default="{ row }">
                <el-text tag="b">{{ formatTime(row.best) }}</el-text>
              </template>
            </el-table-column>

            <el-table-column label="平均 (Avg)" width="120" align="center">
              <template #default="{ row }">
                <el-text tag="b">{{ formatTime(row.average) }}</el-text>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">

                <el-button
                    v-if="compStatus !== 3 && !nextRoundHasData"
                    type="primary"
                    link
                    icon="EditPen"
                    @click="openScoreDialog(row)"
                >
                  录入
                </el-button>

                <el-tag v-else-if="compStatus === 3" type="info" size="small">已归档</el-tag>

                <el-tooltip v-else content="下一轮已开始，本轮成绩锁定" placement="top">
                  <el-tag type="warning" size="small" effect="plain">
                    <el-icon><Lock /></el-icon> 已锁定
                  </el-tag>
                </el-tooltip>

              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>

    <el-dialog v-model="scoreDialogVisible" title="录入成绩" width="550px" destroy-on-close
               :close-on-click-modal="false">
      <el-alert type="success" :closable="false" show-icon class="mb-20">
        <template #title>
          <b>{{ currentCompetitor.userName }}</b>
          <span class="small-subtitle">{{ currentEventName }} / {{ currentRoundName }}</span>
        </template>
      </el-alert>

      <el-form label-width="70px">
        <el-form-item v-for="i in 5" :key="i" :label="`第 ${i} 次`">
          <el-row :gutter="10" class="w-full">
            <el-col :span="8">
              <el-input-number v-model="inputMap['value'+i].min" :min="0" :max="60" controls-position="right"
                               :disabled="dnfFlags['value'+i]" class="w-full">
                <template #suffix>分</template>
              </el-input-number>
            </el-col>
            <el-col :span="10">
              <el-input-number v-model="inputMap['value'+i].sec" :min="0" :max="59.99" :precision="2" :step="0.01"
                               controls-position="right" :disabled="dnfFlags['value'+i]" class="w-full">
                <template #suffix>秒</template>
              </el-input-number>
            </el-col>
            <el-col :span="6">
              <el-checkbox v-model="dnfFlags['value'+i]" label="DNF" @change="(val) => handleDnfChange(val, i)"/>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="scoreDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitScore" :loading="submitting">保存提交</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="promoteDialogVisible" title="晋级设置" width="400px">
      <el-descriptions :column="1" border class="mb-20">
        <el-descriptions-item label="当前轮次">{{ currentRoundName }}</el-descriptions-item>
        <el-descriptions-item label="晋级至">
          <el-tag>{{ nextRoundName }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-position="top">
        <el-form-item label="晋级前多少名 (Top N)">
          <el-input-number v-model="promoteTopN" :min="1" :max="1000" class="w-full"/>
          <el-text type="info" size="small" style="margin-top: 5px;">
            <el-icon>
              <InfoFilled/>
            </el-icon>
            默认值已根据规则自动计算
          </el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="promoteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePromote" :loading="promoting">确认晋级</el-button>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
// ... 这里保持你的 Script 代码完全不变 ...
// ... 请直接复制之前的 script setup 部分 ...
import {ref, reactive, computed, onMounted} from "vue";
import {useRoute} from "vue-router";
import {ElMessage, ElMessageBox} from "element-plus";
import {EditPen, Menu, Download, Search, Trophy, InfoFilled} from "@element-plus/icons-vue";
import {
  getCompetitionBySlug,
  getCompetitionEvents,
  getCompetitionRounds,
  updateCompetitionStatus
} from "@/api/competition";
import {getResultList, saveScore, initFirstRound, promoteCompetitors} from "@/api/result";
import {useEventStore} from "@/stores/event";

const route = useRoute();
const slug = route.params.slug;
const eventStore = useEventStore();
const nextRoundHasData = ref(false);

// --- 状态数据 ---
const competitionId = ref(null);
const competitionName = ref("");
const compStatus = ref(0);
const endDate = ref("");

const eventList = ref([]); // 左侧菜单
const activeEventId = ref("");

const roundList = ref([]); // 顶部 Tab
const activeRoundId = ref(null); // 当前选中的 Tab 值 (ID)

const tableData = ref([]); // 成绩列表
const loading = ref(false);
const searchKeyword = ref("");

// --- 计算属性 ---
const currentEventName = computed(() => {
  const evt = eventList.value.find(e => e.eventId === activeEventId.value);
  return evt ? evt.eventName : "";
});

const currentRoundIndex = computed(() => {
  return roundList.value.findIndex(r => r.id === activeRoundId.value);
});

const currentRoundName = computed(() => {
  const r = roundList.value.find(r => r.id === activeRoundId.value);
  return r ? r.roundName : "";
});

const isFirstRound = computed(() => currentRoundIndex.value === 0);
const isFinalRound = computed(() => currentRoundIndex.value === roundList.value.length - 1);

const nextRoundName = computed(() => {
  if (isFinalRound.value) return "";
  return roundList.value[currentRoundIndex.value + 1].roundName;
});

// 过滤搜索
const filteredTableData = computed(() => {
  if (!searchKeyword.value) return tableData.value;
  const key = searchKeyword.value.toLowerCase();
  return tableData.value.filter(item =>
      (item.userName && item.userName.toLowerCase().includes(key)) ||
      (item.userDisplayId && item.userDisplayId.toLowerCase().includes(key))
  );
});

// --- 初始化 ---
const init = async () => {
  try {
    const compRes = await getCompetitionBySlug(slug);
    if (compRes.data.code === 200) {
      const data = compRes.data.data;
      competitionId.value = data.id;
      competitionName.value = data.name;
      compStatus.value = data.status;
      endDate.value = data.endDate;
      loadEvents();
    }
  } catch (e) {
    console.error(e);
  }
};

const loadEvents = async () => {
  const res = await getCompetitionEvents(competitionId.value);
  if (res.data.code === 200) {
    eventList.value = res.data.data.map(e => ({
      eventId: e.eventId,
      eventName: eventStore.getEventName(e.eventId)
    }));
    if (eventList.value.length > 0) {
      handleEventSelect(eventList.value[0].eventId);
    }
  }
};

// --- 左侧项目切换 ---
const handleEventSelect = async (eventId) => {
  activeEventId.value = eventId;
  const res = await getCompetitionRounds(competitionId.value, eventId);
  if (res.data.code === 200) {
    roundList.value = res.data.data;
    if (roundList.value.length > 0) {
      activeRoundId.value = roundList.value[0].id;
      loadResults();
    } else {
      tableData.value = [];
    }
  }
};

const handleRoundChange = (val) => {
  loadResults();
};

const loadResults = async () => {
  if (!activeRoundId.value) return;
  loading.value = true;
  try {
    // 2.1 加载当前轮次数据 (原有逻辑)
    const res = await getResultList(competitionId.value, activeEventId.value, activeRoundId.value);
    if (res.data.code === 200) {
      tableData.value = res.data.data;
    } else {
      tableData.value = [];
    }

    // ===========================================
    // ★★★ 新增：检测下一轮是否有数据 ★★★
    // ===========================================
    nextRoundHasData.value = false; // 先重置

    // 如果不是决赛，说明还有下一轮，我们需要去查一下下一轮的情况
    if (!isFinalRound.value && roundList.value.length > 0) {
      const nextRoundIndex = currentRoundIndex.value + 1;
      if (nextRoundIndex < roundList.value.length) {
        const nextRound = roundList.value[nextRoundIndex];
        // 调用接口查下一轮的数据 (静默查询，不显示loading)
        const nextRes = await getResultList(competitionId.value, activeEventId.value, nextRound.id);
        if (nextRes.data.code === 200 && nextRes.data.data.length > 0) {
          nextRoundHasData.value = true; // 下一轮有人了！
        }
      }
    }
    // ===========================================

  } finally {
    loading.value = false;
  }
};

// --- 功能 A：一键初始化第一轮 ---
const handleInitRound1 = async () => {
  try {
    await ElMessageBox.confirm('确定导入所有已报名选手到【' + currentRoundName.value + '】吗？', '确认操作', {type: 'info'});
    loading.value = true;
    const res = await initFirstRound({
      competitionId: competitionId.value,
      eventId: activeEventId.value,
      roundId: activeRoundId.value
    });
    if (res.data.code === 200) {
      ElMessage.success("导入成功");
      loadResults();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
  } finally {
    loading.value = false;
  }
};

// --- 功能 B：晋级 ---
const promoteDialogVisible = ref(false);
const promoteTopN = ref(12);
const promoting = ref(false);

const openPromoteDialog = () => {
  // 1. 获取基础数据
  const currentRound = roundList.value.find(r => r.id === activeRoundId.value);
  const totalCompetitors = tableData.value.length; // 当前这一轮实际有多少人
  let calculatedLimit = 0;

  // 2. 解析规则 (WCA规则的简化版)
  if (currentRound && currentRound.advancementRule) {
    const parts = currentRound.advancementRule.split('-');
    const ruleType = parts[0]; // TOP 或 PCT
    const ruleValue = parseInt(parts[1]);

    if (ruleType === 'TOP') {
      // 规则：前 N 名
      calculatedLimit = ruleValue;
    } else if (ruleType === 'PCT') {
      // 规则：前百分之 N
      // 逻辑：总人数 * 百分比，向下取整
      calculatedLimit = Math.floor(totalCompetitors * (ruleValue / 100));
    }
  } else {
    // 如果没设规则（脏数据），默认取前 75%
    calculatedLimit = Math.floor(totalCompetitors * 0.75);
  }

  // ==========================================
  // ★★★ 核心修改：毕设救命逻辑 ★★★
  // ==========================================

  // 1. 【保底逻辑】防止出现只晋级 1 个人的尴尬情况
  // 如果算出来少于 3 人，且总人数够多，强制提升到 3 人
  // 这样 10人参赛，规则10%，原计算为1人，现强制改为3人，比赛就能进行了
  if (calculatedLimit < 3 && totalCompetitors >= 3) {
    calculatedLimit = 3;
    ElMessage.info("根据规则计算晋级人数过少，系统已自动调整为最少 3 人");
  }

  // 2. 【封顶逻辑】防止出现 8人参赛，晋级12人的情况
  if (calculatedLimit >= totalCompetitors) {
    calculatedLimit = totalCompetitors;
    // 这里可以不用报错，或者给个轻提示
    // 这种情况在WCA里叫“全员晋级”，是允许的，不需要恐慌
  }

  // 3. 【极值处理】如果算出来是0 (比如总人数极少)，至少晋级1个冠军出来
  if (calculatedLimit === 0 && totalCompetitors > 0) {
    calculatedLimit = 1;
  }

  // ==========================================

  // 3. 赋值给弹窗，让用户拥有最终决定权
  promoteTopN.value = calculatedLimit;
  promoteDialogVisible.value = true;
};

const handlePromote = async () => {
  promoting.value = true;
  try {
    const nextRound = roundList.value[currentRoundIndex.value + 1];
    const res = await promoteCompetitors({
      competitionId: competitionId.value,
      eventId: activeEventId.value,
      currentRoundId: activeRoundId.value,
      nextRoundId: nextRound.id,
      topN: promoteTopN.value
    });
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg);
      promoteDialogVisible.value = false;
      loadResults();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
  } finally {
    promoting.value = false;
  }
};

// --- 录入弹窗 ---
const scoreDialogVisible = ref(false);
const submitting = ref(false);
const currentCompetitor = ref({});
const dnfFlags = reactive({value1: false, value2: false, value3: false, value4: false, value5: false});
const inputMap = reactive({
  value1: {min: 0, sec: 0},
  value2: {min: 0, sec: 0},
  value3: {min: 0, sec: 0},
  value4: {min: 0, sec: 0},
  value5: {min: 0, sec: 0}
});

const openScoreDialog = (row) => {
  currentCompetitor.value = row;
  for (let i = 1; i <= 5; i++) {
    dnfFlags['value' + i] = false;
    inputMap['value' + i] = {min: 0, sec: 0};
    const ms = row['value' + i];
    if (ms === -1) {
      dnfFlags['value' + i] = true;
    } else if (ms > 0) {
      inputMap['value' + i].min = Math.floor(ms / 60000);
      inputMap['value' + i].sec = (ms % 60000) / 1000;
    }
  }
  scoreDialogVisible.value = true;
};

const handleDnfChange = (val, i) => {
  if (val) inputMap['value' + i] = {min: 0, sec: 0};
};

const submitScore = async () => {
  submitting.value = true;
  try {
    const payload = {
      competitionId: competitionId.value,
      eventId: activeEventId.value,
      roundId: activeRoundId.value,
      userId: currentCompetitor.value.userId,
    };
    for (let i = 1; i <= 5; i++) {
      if (dnfFlags['value' + i]) payload['value' + i] = -1;
      else payload['value' + i] = Math.round(inputMap['value' + i].min * 60000 + inputMap['value' + i].sec * 1000);
    }
    const res = await saveScore(payload);
    if (res.data.code === 200) {
      ElMessage.success("录入成功");
      scoreDialogVisible.value = false;
      loadResults();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    console.error(e);
  } finally {
    submitting.value = false;
  }
};

const formatTime = (ms) => {
  if (ms === -1) return "DNF";
  if (!ms || ms <= 0) return "";
  const min = Math.floor(ms / 60000);
  const sec = Math.floor((ms % 60000) / 1000);
  const msec = Math.floor((ms % 1000) / 10);
  const sStr = sec.toString().padStart(2, '0');
  const msStr = msec.toString().padStart(2, '0');
  return min > 0 ? `${min}:${sStr}.${msStr}` : `${sec}.${msStr}`;
};

const formatSingleTime = (ms) => {
  if (ms === 0) return "";
  return formatTime(ms);
};

const isBest = (row, i) => {
  const currentVal = row['value' + i];

  // 1. 如果当前值无效 (<=0)，直接返回 false
  if (!currentVal || currentVal <= 0) return false;

  // 2. 找出这一行的真正最小值
  let min = Infinity;
  for (let k = 1; k <= 5; k++) {
    const v = row['value' + k];
    if (v > 0 && v < min) min = v;
  }

  // 3. 如果当前值不是最小值，返回 false
  if (currentVal !== min) return false;

  // 4. ★★★ 核心修改：防重复高亮 ★★★
  // 检查在当前位置 (i) 之前，是否已经出现过这个最小值了？
  // 如果前面已经有了，说明当前这个是重复的，就不高亮了。
  for (let k = 1; k < i; k++) {
    if (row['value' + k] === min) {
      return false; // 前面已经有个一模一样的最好成绩了，让它亮就行
    }
  }

  return true;
};

const handleEndCompetition = async () => {
  try {
    await ElMessageBox.confirm("确定结束比赛？结束后将无法录入成绩。", "警告", {type: 'warning'});
    await updateCompetitionStatus({id: competitionId.value, status: 3});
    ElMessage.success("比赛已结束");
    compStatus.value = 3;
  } catch (e) {
  }
};

onMounted(() => init());
</script>

<style scoped>
/* 1. 全局布局类 */
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.layout-header {
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid var(--el-border-color-light);
}

.layout-main {
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 2. 组件微调 */
.no-border-menu {
  border-right: none;
}

.card-tabs {
  margin-bottom: -15px; /* 让 Tab 贴紧分割线 */
}

.toolbar-section {
  padding: 15px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.flex-table {
  width: 100%;
  flex: 1; /* 撑开剩余空间 */
}

/* 3. 核心功能：卡片自适应高度 */
.content-card {
  display: flex;
  flex-direction: column;
  flex: 1; /* 占据 Main 的所有剩余高度 */
  overflow: hidden;
}

/* 强制覆盖 Element Card Body 样式，使其支持 Flex 布局 */
.content-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 0 !important; /* 表格贴边 */
  overflow: hidden; /* 关键：防止双重滚动条 */
}

/* 4. 通用工具类 */
.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.no-scroll {
  overflow: hidden;
}

.text-right {
  text-align: right;
}

.mono-font {
  font-family: monospace;
}

.mb-20 {
  margin-bottom: 20px;
}

.small-subtitle {
  font-size: 12px;
  margin-left: 10px;
}
</style>