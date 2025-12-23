<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 20px">
    <div style="margin-bottom: 20px">
      <h2 style="color: #409eff; display: inline-block; margin-right: 20px">
        <el-icon><Tickets /></el-icon> 我的赛事中心
      </h2>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我报名的比赛" name="joined">
          <el-empty
            v-if="!loading && joinedList.length === 0"
            description="您还没有报名任何比赛"
          />

          <el-table
            v-else
            :data="joinedList"
            v-loading="loading"
            stripe
            style="width: 100%"
          >
            <el-table-column label="比赛名称" min-width="250">
              <template #default="scope">
                <div style="font-weight: bold; font-size: 16px">
                  {{ scope.row.competitionName }}
                </div>
                <div style="font-size: 12px; color: #999; margin-top: 5px">
                  <el-icon><Location /></el-icon> {{ scope.row.location }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="比赛日期" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.startDate) }}
              </template>
            </el-table-column>

            <el-table-column label="报名项目" min-width="200">
              <template #default="scope">
                <el-tag
                  type="info"
                  effect="plain"
                  style="white-space: normal; height: auto; padding: 5px"
                >
                  {{ scope.row.eventNames || "无项目" }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 1" type="success"
                  >已通过</el-tag
                >
                <el-tag v-else-if="scope.row.status === 0" type="warning"
                  >审核中</el-tag
                >
                <el-tag v-else type="danger">已取消</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button link type="primary" @click="goDetail(scope.row.slug)"
                  >详情</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我主办的比赛" name="organized">
          <div style="text-align: right; margin-bottom: 10px">
            <el-button
              type="primary"
              size="small"
              @click="$router.push('/competition/apply')"
            >
              <el-icon><Plus /></el-icon> 申请新比赛
            </el-button>
          </div>

          <el-empty
            v-if="!loading && organizedList.length === 0"
            description="您还没有举办过比赛"
          />

          <el-table
            v-else
            :data="organizedList"
            v-loading="loading"
            stripe
            style="width: 100%"
          >
            <el-table-column label="赛事名称" min-width="200">
              <template #default="scope">
                <span style="font-weight: bold">{{ scope.row.name }}</span>
                <div style="font-size: 12px; color: #999">
                  Slug: {{ scope.row.slug }}
                </div>
              </template>
            </el-table-column>

            <el-table-column label="日期" width="150">
              <template #default="scope">{{
                formatDate(scope.row.startDate)
              }}</template>
            </el-table-column>

            <el-table-column label="审核状态" width="150">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 0" type="warning"
                  >待审核</el-tag
                >
                <el-tag v-else-if="scope.row.status === 4" type="danger"
                  >已驳回</el-tag
                >
                <el-tag v-else type="success">已发布</el-tag>
              </template>
            </el-table-column>

            <el-table-column
              label="审核意见"
              min-width="150"
              show-overflow-tooltip
            >
              <template #default="scope">
                <span v-if="scope.row.status === 4" style="color: #f56c6c">{{
                  scope.row.auditMsg
                }}</span>
                <span v-else>{{ scope.row.auditMsg || "-" }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="280" align="center">
              <template #default="scope">
                <el-button
                  v-if="[0, 4].includes(scope.row.status)"
                  link
                  type="danger"
                  @click="handleEdit(scope.row)"
                >
                  <el-icon><Edit /></el-icon> 修改
                </el-button>

                <el-button
                  v-if="[0, 4].includes(scope.row.status)"
                  link
                  type="danger"
                  @click="handleDelete(scope.row)"
                >
                  <el-icon><Delete /></el-icon> 删除
                </el-button>

                <el-button
                  v-else
                  link
                  type="primary"
                  @click="goDetail(scope.row.slug)"
                >
                  预览
                </el-button>

                <el-button
                  v-if="[1, 2, 3].includes(scope.row.status)"
                  link
                  type="warning"
                  @click="$router.push(`/competition/${scope.row.slug}/score`)"
                >
                  管理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getMyRegistrationList } from "@/api/registration";
import { getMyOrganizedCompetitions, deleteCompetition } from "@/api/competition";
import { Tickets, Location, Plus, Edit, Delete } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const activeTab = ref("joined");
const loading = ref(false);
const joinedList = ref([]); // 报名的列表
const organizedList = ref([]); // 主办的列表

const formatDate = (str) => {
  if (!str) return "";
  return str.toString().split("T")[0];
};

const goDetail = (slug) => {
  router.push(`/competition/${slug}`);
};

// 加载“我报名的”
const loadJoined = async () => {
  loading.value = true;
  try {
    const res = await getMyRegistrationList();
    if (res.data.code === 200) joinedList.value = res.data.data;
  } finally {
    loading.value = false;
  }
};

// 加载“我主办的”
const loadOrganized = async () => {
  loading.value = true;
  try {
    const res = await getMyOrganizedCompetitions();
    if (res.data.code === 200) organizedList.value = res.data.data;
  } finally {
    loading.value = false;
  }
};

// 切换 Tab 时加载不同数据
const handleTabChange = (tabName) => {
  if (tabName === "joined") {
    loadJoined();
  } else if (tabName === "organized") {
    loadOrganized();
  }
};

const handleEdit = (row) => {
  // 跳转到申请页，并带上 slug 参数，表明是编辑模式
  router.push(`/competition/apply?slug=${row.slug}`);
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除比赛 "${row.name}" 吗？`,
      "删除确认",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning"
      }
    );
    
    const res = await deleteCompetition(row.id);
    if (res.data.code === 200) {
      ElMessage.success("比赛删除成功");
      loadOrganized(); // 重新加载列表
    } else {
      ElMessage.error(res.data.msg || "删除失败");
    }
  } catch (error) {
    // 取消删除操作，不做任何处理
    if (error !== "cancel") {
      ElMessage.error("删除失败");
    }
  }
};

onMounted(() => {
  loadJoined();
});
</script>
