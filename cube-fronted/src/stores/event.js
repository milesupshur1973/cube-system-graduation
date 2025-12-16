import { ref, computed } from "vue";
import { defineStore } from "pinia";
import request from "@/utils/request";

export const useEventStore = defineStore("event", () => {
  // 1. 状态：存储所有项目列表
  const eventList = ref([]);

  // 2. 计算属性：生成 ID -> Name 的映射字典 (替代原来的 EVENT_NAME_MAP)
  // 结果示例：{ '333': '三阶魔方', '333oh': '三阶单手' ... }
  const eventMap = computed(() => {
    const map = {};
    eventList.value.forEach((item) => {
      map[item.id] = item.name;
    });
    return map;
  });

  // 3. 动作：从后端加载数据
  const loadEvents = async () => {
    // 如果已经有数据了，就别浪费请求了
    if (eventList.value.length > 0) return;

    try {
      const res = await request.get("/event/list");
      if (res.data.code === 200) {
        eventList.value = res.data.data;
      }
    } catch (e) {
      console.error("加载项目列表失败", e);
    }
  };

  // 辅助函数：根据ID获取名字 (防崩：如果没加载完，先显示ID)
  const getEventName = (id) => {
    return eventMap.value[id] || id;
  };

  return { eventList, eventMap, loadEvents, getEventName };
});
