<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 15px">
    <el-row :gutter="20">
      <el-col :span="17" :xs="24">
        <h3 style="margin-bottom: 20px; color: #409eff">
          <el-icon style="vertical-align: middle"><Bell /></el-icon> 最新公告
        </h3>

        <el-card
          v-for="news in newsList"
          :key="news.id"
          shadow="never"
          style="margin-bottom: 15px"
        >
          <template #header>
            <span style="font-weight: bold">{{ news.title }}</span>
          </template>
          <el-space direction="vertical" alignment="start">
            <el-text type="info" size="small">
              发布于：{{ formatDateTime(news.publishTime) }} | 作者：管理员
            </el-text>
            
            <div style="color: #606266; font-size: 14px; max-height: 60px; overflow: hidden;">
               (点击下方按钮查看公告详情...)
            </div>

            <el-link type="primary" @click="openArticle(news)">阅读全文</el-link>
          </el-space>
        </el-card>
      </el-col>

      <el-col :span="7" :xs="24">
         <h3 style="margin-bottom: 20px; color: #409eff">
          <el-icon style="vertical-align: middle"><Trophy /></el-icon> 近期赛事
        </h3>
        <el-card shadow="never">
          <div
            v-for="comp in compList"
            :key="comp.id"
            style="margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #f0f0f0;"
          >
            <el-row align="middle" style="cursor: pointer" @click="goDetail(comp.slug)">
              <el-col :span="8">
                <el-tag size="large" effect="plain">{{ formatDate(comp.startDate) }}</el-tag>
              </el-col>
              <el-col :span="16">
                <div style="font-weight: bold; color: #303133; margin-bottom: 5px">{{ comp.name }}</div>
                <div style="font-size: 12px; color: #909399"><el-icon><Location /></el-icon> {{ comp.city }}</div>
              </el-col>
            </el-row>
          </div>
          <el-empty v-if="compList.length === 0" description="暂无赛事" :image-size="60" />
          <el-button style="width: 100%; margin-top: 10px" @click="$router.push('/competition')">查看更多</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="currentArticle.title" width="600px">
      <div style="margin-bottom: 10px; color: #999; font-size: 12px; border-bottom: 1px solid #eee; padding-bottom: 10px;">
        发布时间：{{ formatDateTime(currentArticle.publishTime) }}
      </div>
      <div v-html="currentArticle.content" class="article-content"></div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { Bell, Trophy, Location } from "@element-plus/icons-vue";
import { getArticleList } from "@/api/article";
import { getUpcomingCompetitions } from "@/api/competition";

const router = useRouter();
const newsList = ref([]);
const compList = ref([]);

// 弹窗相关变量
const dialogVisible = ref(false);
const currentArticle = ref({});

const formatDate = (str) => {
  if (!str) return "";
  return str.split("T")[0];
};
// 增加一个显示详细时间的方法
const formatDateTime = (str) => {
  if (!str) return "";
  return str.replace("T", " ");
};

const goDetail = (slug) => {
  router.push(`/competition/${slug}`);
};

// 打开弹窗方法
const openArticle = (news) => {
  currentArticle.value = news;
  dialogVisible.value = true;
};

const loadData = async () => {
  try {
    const newsRes = await getArticleList();
    if (newsRes.data.code === 200) {
      newsList.value = newsRes.data.data;
    }
    const compRes = await getUpcomingCompetitions();
    if (compRes.data.code === 200) {
      compList.value = compRes.data.data;
    }
  } catch (e) {
    console.error("加载数据失败:", e);
  }
};

onMounted(() => loadData());
</script>

<style scoped>
/* 防止图片溢出弹窗 */
.article-content :deep(img) {
  max-width: 100%;
}
</style>