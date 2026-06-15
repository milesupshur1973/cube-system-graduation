<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="17" :xs="24">
        <h3 class="section-title">
          <el-icon class="title-icon"><Bell /></el-icon> 最新公告
        </h3>

        <el-card
            v-for="news in newsList"
            :key="news.id"
            shadow="never"
            class="news-card"
        >
          <template #header>
            <span class="news-title">{{ news.title }}</span>
          </template>
          <el-space direction="vertical" alignment="start">
            <el-text type="info" size="small">
              发布于：{{ formatDateTime(news.publishTime) }} | 作者：管理员
            </el-text>

            <div class="news-desc">
              (点击下方按钮查看公告详情...)
            </div>

            <el-link type="primary" @click="openArticle(news)">阅读全文</el-link>
          </el-space>
        </el-card>

        <div class="pagination-wrapper">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 15, 20]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background></el-pagination>
        </div>
      </el-col>

      <el-col :span="7" :xs="24">
        <h3 class="section-title">
          <el-icon class="title-icon"><Location /></el-icon> 附近赛事
        </h3>
        <el-card shadow="never">
          <div
              v-for="comp in compList"
              :key="comp.id"
              class="comp-item"
          >
            <el-row align="middle" class="comp-row" @click="goDetail(comp.slug)">
              <el-col :span="8">
                <el-tag size="large" effect="plain" :type="comp.distanceText ? 'success' : 'info'">
                  {{ comp.distanceText ? comp.distanceText : formatDate(comp.startDate) }}
                </el-tag>
              </el-col>
              <el-col :span="16">
                <div class="comp-title">{{ comp.name }}</div>
                <div class="comp-location"><el-icon><Location /></el-icon> {{ comp.city }}</div>
              </el-col>
            </el-row>
          </div>
          <el-empty v-if="compList.length === 0" description="暂无赛事" :image-size="60" />
          <el-button class="more-btn" @click="$router.push('/competition')">查看全部赛事</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="currentArticle.title" width="600px">
      <div class="dialog-meta">
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
import { initAMap, getUserLocation, calculateDistancesAndSort } from "@/utils/map";

const router = useRouter();
const newsList = ref([]);
const compList = ref([]);

// 分页相关变量
const currentPage = ref(1);
const pageSize = ref(5);
const total = ref(0);

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
    // 1. 加载新闻（原有逻辑）
    const newsRes = await getArticleList({ page: currentPage.value, size: pageSize.value });
    if (newsRes.data.code === 200) {
      newsList.value = newsRes.data.data.records;
      total.value = newsRes.data.data.total;
    }

    // 2. 加载所有近期赛事
    const compRes = await getUpcomingCompetitions();
    let rawCompList = [];
    if (compRes.data.code === 200) {
      rawCompList = compRes.data.data;
    }

    // --- 开始地图智能推荐逻辑 ---
    try {
      // a. 初始化地图工具
      await initAMap();
      // b. 获取用户位置 (浏览器可能会在此刻弹窗询问是否允许定位)
      const userPos = await getUserLocation();
      // c. 计算距离并排序
      const sortedList = await calculateDistancesAndSort(userPos, rawCompList);
      // d. 截取离得最近的 5 个展示
      compList.value = sortedList.slice(0, 5);

    } catch (mapError) {
      console.warn("地理位置获取失败，已降级为按时间显示", mapError);
      // 兜底机制：如果定位失败（如用户拒绝、无网络等），就按原来的逻辑只展示前5个
      compList.value = rawCompList.slice(0, 5);
    }

  } catch (e) {
    console.error("加载数据失败:", e);
    newsList.value = [];
    compList.value = [];
    total.value = 0;
  }
};

// 分页事件处理
const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadData();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1; // 切换每页条数时重置到第一页
  loadData();
};

onMounted(() => loadData());
</script>

<style scoped>
/* 页面主容器 */
.page-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 15px;
}

/* 模块标题 */
.section-title {
  margin-bottom: 20px;
  color: #409eff;
}

.title-icon {
  vertical-align: middle;
}

/* 公告列表样式 */
.news-card {
  margin-bottom: 15px;
}

.news-title {
  font-weight: bold;
}

.news-desc {
  color: #606266;
  font-size: 14px;
  max-height: 60px;
  overflow: hidden;
}

.pagination-wrapper {
  text-align: center;
  margin-top: 20px;
}

/* 赛事列表样式 */
.comp-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.comp-row {
  cursor: pointer;
}

.comp-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.comp-location {
  font-size: 12px;
  color: #909399;
}

.more-btn {
  width: 100%;
  margin-top: 10px;
}

/* 弹窗样式 */
.dialog-meta {
  margin-bottom: 10px;
  color: #999;
  font-size: 12px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

/* 防止图片溢出弹窗 */
.article-content :deep(img) {
  max-width: 100%;
}
</style>