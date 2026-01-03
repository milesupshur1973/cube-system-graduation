<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 20px">
    <div style="margin-bottom: 20px">
      <h2 style="color: #f56c6c; display: inline-block; margin-right: 20px">
        <el-icon><Monitor /></el-icon> 管理员控制台
      </h2>
      <el-tag type="danger" effect="dark">待审核任务</el-tag>
    </div>

    <el-card shadow="never">
      <div style="text-align: right; margin-bottom: 15px">
        <el-button type="primary" @click="publishDialogVisible = true">
          <el-icon><Plus /></el-icon> 发布公告
        </el-button>
        <el-button @click="loadData" :icon="Refresh">刷新列表</el-button>
      </div>

      <el-table
        :data="list"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="赛事名称" min-width="200">
          <template #default="scope">
            <span style="font-weight: bold">{{ scope.row.name }}</span>
            <div style="font-size: 12px; color: #999">
              Slug: {{ scope.row.slug }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="申请人" width="150" align="center">
          <template #default="scope">
            <div style="font-weight: bold">{{ scope.row.organizerName }}</div>
            <div style="font-size: 12px; color: #999">
              (ID: {{ scope.row.organizerId }})
            </div>
          </template>
        </el-table-column>

        <el-table-column label="举办地" width="180">
          <template #default="scope">
            {{ scope.row.province }} - {{ scope.row.city }}
          </template>
        </el-table-column>

        <el-table-column label="计划日期" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="170" prop="createTime">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row.slug)"
              >详情</el-button
            >
            <el-button
              type="success"
              size="small"
              @click="handlePass(scope.row)"
              >通过</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleReject(scope.row)"
              >驳回</el-button
            >
          </template>
        </el-table-column>
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
      <el-dialog v-model="publishDialogVisible" title="发布新公告" width="800px">
      <el-form label-position="top">
        <el-form-item label="公告标题">
          <el-input v-model="articleForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="公告内容">
          <MyEditor v-model="articleForm.content" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishLoading">
          确认发布
        </el-button>
      </template>
    </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Monitor, Refresh } from "@element-plus/icons-vue";
import { getPendingCompetitions, auditCompetition } from "@/api/competition";
import { publishArticle } from "@/api/article";
import MyEditor from "@/components/MyEditor.vue";

const router = useRouter();
const list = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const publishDialogVisible = ref(false);
const publishLoading = ref(false);
const articleForm = ref({
  title: "",
  content: ""
});

const formatDate = (str) => {
  if (!str) return "";
  return str.split("T")[0];
};

const formatDateTime = (str) => {
  if (!str) return "";
  return str.replace("T", " ").substring(0, 16);
};

// 查看详情（新窗口打开，方便比对）
const viewDetail = (slug) => {
  const routeUrl = router.resolve({ path: `/competition/${slug}` });
  window.open(routeUrl.href, "_blank");
};

// 加载待审核列表
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    };
    const res = await getPendingCompetitions(params);
    if (res.data.code === 200) {
      list.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 翻页事件
const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

// 通过审核
const handlePass = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过 "${row.name}" 的申请吗？\n通过后比赛将立即发布。`,
      "通过审核",
      { type: "warning", confirmButtonText: "确定通过" }
    );

    const res = await auditCompetition(row.id, true, null);
    if (res.data.code === 200) {
      ElMessage.success("操作成功：已发布");
      if (list.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }
      loadData();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch {}
};

// 驳回申请
const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt("请输入驳回原因", "驳回申请", {
      confirmButtonText: "确认驳回",
      cancelButtonText: "取消",
      inputPlaceholder: "例如：信息不完整，请补充场馆具体位置",
      inputValidator: (val) => {
        if (!val) return "驳回原因不能为空";
        return true;
      },
    });

    const res = await auditCompetition(row.id, false, value);
    if (res.data.code === 200) {
      ElMessage.success("操作成功：已驳回");
      if (list.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }
      loadData();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch {}
};

const handlePublish = async () => {
  if (!articleForm.value.title || !articleForm.value.content) {
    return ElMessage.warning("标题和内容不能为空");
  }
  
  publishLoading.value = true;
  try {
    const res = await publishArticle(articleForm.value);
    if (res.data.code === 200) {
      ElMessage.success("发布成功");
      publishDialogVisible.value = false;
      // 清空表单
      articleForm.value.title = "";
      articleForm.value.content = "";
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch(e) {
    console.error(e);
  } finally {
    publishLoading.value = false;
  }
};

onMounted(() => {
  loadData();
});
</script>
