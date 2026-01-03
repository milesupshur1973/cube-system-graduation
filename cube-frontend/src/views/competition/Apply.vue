<template>
  <div style="max-width: 800px; margin: 30px auto">
    <el-card v-loading="loading" element-loading-text="正在加载比赛详情...">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 18px">
            <el-icon><Edit /></el-icon> 申请举办公示比赛
          </span>
        </div>
      </template>

      <el-form
        :model="form"
        ref="formRef"
        :rules="rules"
        label-width="120px"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="赛事名称" prop="name">
              <el-input
                v-model="form.name"
                placeholder="例如：2025年WCA西安公开赛"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赛事代号 (Slug)" prop="slug">
              <el-input
                v-model="form.slug"
                placeholder="英文与数字，如：Xian-Open-2025"
                :disabled="isEditMode" 
              >
                <template #prepend>cubing.com/</template>
              </el-input>
              
              <small style="color: #999">
                 {{ isEditMode ? '赛事代号创建后不可修改' : '这将作为比赛的专属链接，不可重复' }}
              </small>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所在地区" prop="selectedLocation">
              <el-cascader
                v-model="form.selectedLocation"
                :options="cityData"
                placeholder="请选择省份 / 城市"
                style="width: 100%"
                separator=" / "
                @change="handleLocationChange"
              />
              <div v-if="isEditMode && form.selectedLocation.length === 0 && form.province" 
                   style="font-size: 12px; color: #E6A23C; line-height: 1.5; margin-top: 5px;">
                <el-icon style="vertical-align: middle"><Location /></el-icon>
                当前已存地区：<strong>{{ form.province }} / {{ form.city }}</strong>
                <div style="color: #999">如需修改，请在上方重新选择；不操作则保持原样。</div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="具体场馆" prop="location">
              <el-input
                v-model="form.location"
                placeholder="区/县 + 街道 + 建筑名"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="比赛日期" prop="dateRange">
              <el-date-picker
                v-model="form.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="disabledDate"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名人数限制" prop="competitorLimit">
              <el-input-number
                v-model="form.competitorLimit"
                :min="1"
                :max="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="开设项目" prop="eventIds">
          <el-checkbox-group v-model="form.eventIds">
            <el-checkbox
              v-for="evt in eventList"
              :key="evt.id"
              :label="evt.id"
              border
              style="margin-right: 10px; margin-bottom: 10px"
            >
              {{ evt.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="赛事详情" prop="contentDescription">
          <MyEditor v-model="form.contentDescription" />
        </el-form-item>

        <el-form-item label="比赛规则" prop="contentRule">
          <MyEditor v-model="form.contentRule" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="submitForm"
            :loading="loading"
            >提交申请</el-button
          >
          <el-button size="large" @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import { Edit } from "@element-plus/icons-vue";
import { provinceAndCityData } from "element-china-area-data";
import { applyCompetition, updateCompetition, getCompetitionBySlug, getCompetitionEvents } from "@/api/competition";
import MyEditor from "@/components/MyEditor.vue";
import { useEventStore } from "@/stores/event";
import { storeToRefs } from "pinia";
import { onMounted } from 'vue';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);
const loading = ref(false);
const cityData = provinceAndCityData;
const eventStore = useEventStore();
const { eventList } = storeToRefs(eventStore);
const route = useRoute(); // 获取路由参数
const isEditMode = ref(false); // 是否为编辑模式
const editId = ref(null); // 编辑的比赛ID

const form = reactive({
  name: "",
  slug: "",
  selectedLocation: [],
  province: "",
  city: "",
  location: "",
  dateRange: [],
  competitorLimit: 100,
  contentDescription: "",
  contentRule: "",
  eventIds: [],
});

const rules = {
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
  slug: [{ required: true, message: "请输入代号", trigger: "blur" }],
  selectedLocation: [
    { 
      required: true, 
      message: "请选择省市", 
      trigger: "change",
      validator: (rule, value, callback) => {
        // 编辑模式下，如果已有province和city，则跳过验证
        if (isEditMode.value && form.province && form.city) {
          callback();
        } else if (value && value.length === 2) {
          callback();
        } else {
          callback(new Error("请选择省市"));
        }
      }
    },
  ],
  location: [{ required: true, message: "请输入地址", trigger: "blur" }],
  dateRange: [{ required: true, message: "请选择日期", trigger: "change" }],
  eventIds: [
    { required: true, message: "请至少选择一个项目", trigger: "change" },
  ],
};

// 定义日期禁用逻辑：当前时间往后推7天之前的日期都禁用
const disabledDate = (time) => {
  const today = new Date();
  // 计算7天后的时间戳 (7天 * 24小时 * 60分钟 * 60秒 * 1000毫秒)
  const sevenDaysLater = today.getTime() + 7 * 24 * 60 * 60 * 1000;

  // 如果日历上的时间小于7天后的时间，则禁用（返回true）
  // 比如今天是20号，20+7=27号。我们希望27号开始才是可选的。
  return time.getTime() < sevenDaysLater;
};

const handleLocationChange = (value) => {
  if (value && value.length === 2) {
    const provinceCode = value[0];
    const cityCode = value[1];
    const provinceItem = cityData.find((item) => item.value === provinceCode);
    const cityItem = provinceItem
      ? provinceItem.children.find((item) => item.value === cityCode)
      : null;
    if (provinceItem && cityItem) {
      form.province = provinceItem.label;
      form.city = cityItem.label;
    }
  }
};

// 1. 获取详情并回显数据的方法
const loadDetail = async (slug) => {
  try {
    loading.value = true;
    
    // 修复点1：并行请求比赛详情 + 比赛项目
    const compRes = await getCompetitionBySlug(slug);
    
    if (compRes.data.code === 200) {
      const data = compRes.data.data;
      
      // 检查权限：只有自己或驳回状态才能改 (前端简单防一下，后端有兜底)
      if (data.status !== 4 && data.status !== 0) {
        ElMessage.warning("当前状态不可修改");
        // 注意：这里不return，让用户可以查看详情，只是提示一下
      }

      // 开启编辑模式
      isEditMode.value = true;
      editId.value = data.id;

      // === 回显表单数据 ===
      form.name = data.name;
      form.slug = data.slug;
      form.province = data.province;
      form.city = data.city;
      form.location = data.location;
      form.competitorLimit = data.competitorLimit;
      form.contentDescription = data.contentDescription;
      form.contentRule = data.contentRule;

      // 回显日期 (后端是 startDate/endDate 分开的，前端是 dateRange 数组)
      if (data.startDate && data.endDate) {
        form.dateRange = [data.startDate, data.endDate];
      }

      // 回显省市区：保留原始值，Cascader显示为空但不影响提交
      // 如果用户不重新选择，提交时会用 form.province 和 form.city
      // 如果用户重新选择了，handleLocationChange 会覆盖这两个值
      form.selectedLocation = []; // Cascader置空，但province/city已赋值
      
      // 修复点2：立即加载项目列表（在同一个 try 块内）
      const eventRes = await getCompetitionEvents(data.id);
      if (eventRes.data.code === 200) {
        // 提取 eventId 放入数组
        form.eventIds = eventRes.data.data.map(e => e.eventId);
        console.log("✅ 已回显项目:", form.eventIds); // 调试用
      } else {
        ElMessage.warning("项目列表加载失败，请重新选择");
        form.eventIds = []; // 安全兜底
      }
    }

  } catch (e) {
    console.error(e);
    ElMessage.error("加载比赛详情失败");
    // 修复点3：加载失败时清空表单，避免脏数据
    form.eventIds = [];
  } finally {
    loading.value = false;
  }
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!userStore.userInfo.id) return ElMessage.error("请先登录");

      loading.value = true;
      try {
        const now = new Date();
        
        const localNow = new Date(now.getTime() - (now.getTimezoneOffset() * 60000)).toISOString().slice(0, 19);

        // 构造提交数据
        const payload = {
          ...form,
          startDate: form.dateRange[0],
          endDate: form.dateRange[1],
          
          // 如果是编辑模式(isEditMode=true)且原数据有regStartTime，则保持原样
          // 如果是新增模式，或者原数据为空，则使用当前时间(localNow)作为报名开始时间
          regStartTime: form.regStartTime || localNow, 
          
          // 报名截止时间默认为比赛开始当天的 00:00:00
          regEndTime: form.regEndTime || (form.dateRange[0] + "T23:59:59"),
        };
        
        // 删除多余字段
        delete payload.selectedLocation;
        delete payload.dateRange;

        let res;
        if (isEditMode.value) {
          // ★★★ 编辑模式：调用 update 接口，并传 ID ★★★
          payload.id = editId.value;
          res = await updateCompetition(payload);
        } else {
          // ★★★ 新增模式：调用 apply 接口 ★★★
          res = await applyCompetition(payload);
        }

        if (res.data.code === 200) {
          ElMessage.success(isEditMode.value ? "修改成功，已重新提交审核" : "申请提交成功！请等待审核");
          router.push("/user/my-competitions"); // 跳回列表页
        } else {
          ElMessage.error(res.data.msg || "提交失败");
        }
      } catch (e) {
        console.error(e);
        ElMessage.error("网络异常");
      } finally {
        loading.value = false;
      }
    }
  });
};

onMounted(() => {
    // 检查是否有 slug 参数
    const slug = route.query.slug;
    if (slug) {
        loadDetail(slug);
    }
});
</script>
