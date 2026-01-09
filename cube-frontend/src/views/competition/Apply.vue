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

        <el-form-item label="项目与轮次配置" required>
          <div style="width: 100%">
            <div
                v-for="(item, index) in form.events"
                :key="index"
                style="border: 1px solid #dcdfe6; border-radius: 4px; padding: 15px; margin-bottom: 15px; position: relative;"
            >
              <el-button
                  type="danger"
                  link
                  icon="Delete"
                  style="position: absolute; right: 10px; top: 10px;"
                  @click="removeEvent(index)"
              >删除项目</el-button>

              <el-form-item label="比赛项目" label-width="80px" style="margin-bottom: 15px">
                <el-select v-model="item.eventId" placeholder="请选择项目" style="width: 200px" filterable>
                  <el-option
                      v-for="evt in eventList"
                      :key="evt.id"
                      :label="evt.name"
                      :value="evt.id"
                      :disabled="isEventSelected(evt.id) && item.eventId !== evt.id"
                  />
                </el-select>
              </el-form-item>

              <div style="background-color: #f5f7fa; padding: 10px; border-radius: 4px;">
                <div style="font-size: 13px; color: #666; margin-bottom: 10px;">轮次设置</div>

                <el-row :gutter="10" v-for="(round, rIndex) in item.rounds" :key="rIndex" style="margin-bottom: 10px; align-items: center;">
                  <el-col :span="8">
                    <el-input v-model="round.roundName" placeholder="轮次名称 (如: 初赛)">
                      <template #prepend>第{{ rIndex + 1 }}轮</template>
                    </el-input>
                  </el-col>

                  <el-col :span="10">
                    <div v-if="rIndex !== item.rounds.length - 1">
                      <el-input
                          v-model="round.splitRule.value"
                          placeholder="数值"
                          type="number"
                          :min="1"
                      >
                        <template #prepend>
                          <el-select v-model="round.splitRule.type" placeholder="类型" style="width: 85px">
                            <el-option label="前 N 名" value="TOP" />
                            <el-option label="前 %" value="PCT" />
                          </el-select>
                        </template>
                        <template #append>
                          {{ round.splitRule.type === 'TOP' ? '人' : '%' }}
                        </template>
                      </el-input>
                    </div>
                    <div v-else style="line-height: 32px; color: #999; font-size: 13px; padding-left: 10px;">
                      冠军诞生
                    </div>
                  </el-col>

                  <el-col :span="6">
                    <el-button
                        v-if="item.rounds.length > 1 && rIndex === item.rounds.length - 1"
                        type="danger"
                        circle
                        icon="Minus"
                        size="small"
                        @click="removeRound(index)"
                    />
                    <el-tag v-if="rIndex === item.rounds.length - 1" type="info" style="margin-left: 5px">决赛</el-tag>
                    <el-tag v-else type="primary" style="margin-left: 5px">晋级下一轮</el-tag>
                  </el-col>
                </el-row>

                <el-button type="primary" link icon="Plus" @click="addRound(index)" style="margin-top: 5px">
                  增加一轮
                </el-button>
              </div>
            </div>

            <el-button type="primary" plain style="width: 100%; border-style: dashed" icon="Plus" @click="addEvent">
              添加比赛项目
            </el-button>
          </div>
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
import { Edit, Delete, Minus, Plus, Location } from "@element-plus/icons-vue";
import { provinceAndCityData } from "element-china-area-data";
import { applyCompetition, updateCompetition, getCompetitionBySlug, getCompetitionEvents, getCompetitionDetailForEdit } from "@/api/competition";
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
  events: [
    {
      eventId: '',
      rounds: [
        { roundOrder: 1, roundName: '决赛', advancementRule: '' }
      ]
    }
  ],
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
  events: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (form.events.length === 0) {
          callback(new Error("请至少添加一个比赛项目"));
        } else {
          // 检查是否所有项目都选了 ID
          const hasEmpty = form.events.some(e => !e.eventId);
          if (hasEmpty) callback(new Error("请完善项目选择"));
          else callback();
        }
      },
      trigger: "change"
    },
  ],
};

// 判断某个项目是否已经被选中过（用于下拉框禁用已选项）
const isEventSelected = (id) => {
  return form.events.some(item => item.eventId === id);
};

// 添加一个新项目
const addEvent = () => {
  form.events.push({
    eventId: "", // 等待用户选择
    rounds: [
      { roundOrder: 1, roundName: "决赛", advancementRule: "" } // 默认一轮
    ]
  });
};

// 删除一个项目
const removeEvent = (index) => {
  form.events.splice(index, 1);
};

// 1. 辅助函数：解析规则字符串 (用于回显)
// 输入 "TOP-12" -> 返回 { type: 'TOP', value: 12 }
// 输入 "PCT-75" -> 返回 { type: 'PCT', value: 75 }
const parseRule = (ruleStr) => {
  if (!ruleStr) return { type: 'TOP', value: null };
  const parts = ruleStr.split('-');
  if (parts.length === 2) {
    return { type: parts[0], value: parseInt(parts[1]) };
  }
  return { type: 'TOP', value: null };
};

// 给指定项目(eventIndex)增加一轮
const addRound = (eventIndex) => {
  const eventItem = form.events[eventIndex];
  const currentCount = eventItem.rounds.length;

  if (currentCount >= 3) {
    ElMessage.warning("根据规则，单项比赛最多只能开设 3 轮");
    return;
  }

  // 这里的 splitRule 是前端临时用的对象，不传给后端，提交时会拼成 advancementRule
  if (currentCount === 1) {
    // 只有1轮变2轮时：第1轮变初赛
    eventItem.rounds[0].roundName = "初赛";
    eventItem.rounds[0].splitRule = { type: 'PCT', value: 75 }; // 默认前75%
  } else {
    // 2轮变3轮时：第2轮变复赛
    eventItem.rounds[currentCount - 1].roundName = "复赛";
    eventItem.rounds[currentCount - 1].splitRule = { type: 'TOP', value: 12 }; // 默认前12人
  }

  // 压入新的一轮 (决赛)
  eventItem.rounds.push({
    roundOrder: currentCount + 1,
    roundName: "决赛",
    advancementRule: "",
    splitRule: { type: 'TOP', value: null } // 决赛不需要规则，占位即可
  });
};

// 删除最后一轮
const removeRound = (eventIndex) => {
  const eventItem = form.events[eventIndex];
  if (eventItem.rounds.length > 1) {
    eventItem.rounds.pop();
    // 修正现在的最后一轮为决赛
    const lastIndex = eventItem.rounds.length - 1;
    eventItem.rounds[lastIndex].roundName = "决赛";
    eventItem.rounds[lastIndex].advancementRule = "";
  }
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

    // 1. 先通过 slug 获取 ID
    const slugRes = await getCompetitionBySlug(slug);
    if (slugRes.data.code !== 200) {
      ElMessage.error("比赛不存在");
      return;
    }
    const simpleInfo = slugRes.data.data;

    // 2. 调用我们要的新接口 (传入 ID)
    const res = await getCompetitionDetailForEdit(simpleInfo.id);

    if (res.data.code === 200) {
      const data = res.data.data;

      isEditMode.value = true;
      editId.value = data.id;

      // === 直接回显基本信息 ===
      form.name = data.name;
      form.slug = data.slug;
      form.province = data.province;
      form.city = data.city;
      form.location = data.location;
      form.competitorLimit = data.competitorLimit;
      form.contentDescription = data.contentDescription;
      form.contentRule = data.contentRule;

      // 回显日期
      if (data.startDate && data.endDate) {
        form.dateRange = [data.startDate, data.endDate];
      }

      // ★★★ 核心修改点：这里替换了原来的直接赋值 ★★★
      if (data.events && data.events.length > 0) {
        // 遍历后端返回的数据，给每一轮加上 splitRule 对象
        form.events = data.events.map(evt => ({
          eventId: evt.eventId,
          rounds: evt.rounds.map(r => ({
            ...r,
            // 解析字符串 "TOP-12" -> { type: 'TOP', value: 12 }
            splitRule: parseRule(r.advancementRule)
          }))
        }));
      } else {
        form.events = [];
      }

      // Cascader 回显处理
      form.selectedLocation = [];

    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("加载详情失败");
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

        const processedEvents = form.events.map(evt => ({
          eventId: evt.eventId,
          rounds: evt.rounds.map(r => {
            // 如果是决赛，或者 splitRule 为空，规则就是空字符串
            // 否则拼接：TOP-12 或 PCT-75
            let ruleStr = "";
            if (r.splitRule && r.splitRule.value) {
              ruleStr = `${r.splitRule.type}-${r.splitRule.value}`;
            }

            return {
              roundOrder: r.roundOrder,
              roundName: r.roundName,
              advancementRule: ruleStr // 赋值给后端需要的字段
            };
          })
        }));

        // 构造提交数据
        const payload = {
          ...form,
          startDate: form.dateRange[0],
          endDate: form.dateRange[1],
          regStartTime: form.regStartTime || localNow,
          regEndTime: form.regEndTime || (form.dateRange[0] + "T23:59:59"),

          // ★★★ 核心：直接把 form.events 传给后端 ★★★
          // 后端 DTO 里的结构是 List<EventConfigDTO> events，跟我们前端构造的完全一致
          events: processedEvents
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
