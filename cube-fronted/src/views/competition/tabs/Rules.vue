<template>
  <div>
    <div v-if="compInfo.contentRule" class="rule-content">
      <div
        style="
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 1px solid #eee;
        "
      >
        <h2 style="margin: 0; font-size: 24px; color: #333">
          {{ compInfo.name }} - 比赛规则
        </h2>
      </div>

      <div v-html="compInfo.contentRule"></div>

      <el-divider />

      <p style="color: #909399; font-size: 14px">
        中国魔方赛事网是信息发布平台。我们不是赛事主办方，因此不对比赛具体实施负责。
      </p>
    </div>

    <el-empty v-else description="暂无比赛规则说明" />
  </div>
</template>

<script setup>
const props = defineProps(["compInfo"]);
</script>

<style scoped>
/* 虽然你说尽量不改样式，但为了让 v-html 渲染出来的文章（h3, ol, li）不至于太丑，
  加上这些极简的样式是必要的。这不属于“魔改”，而是“基础排版”。
*/
.rule-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

/* 针对 v-html 内部标签的样式 */
.rule-content :deep(h3) {
  margin-top: 25px;
  margin-bottom: 15px;
  font-size: 18px;
  color: #409eff; /* 用 Element Plus 的主题蓝 */
  font-weight: bold;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.rule-content :deep(ol),
.rule-content :deep(ul) {
  padding-left: 20px;
  margin-bottom: 15px;
}

.rule-content :deep(li) {
  margin-bottom: 8px;
}

.rule-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}
.rule-content :deep(a):hover {
  text-decoration: underline;
}
</style>
