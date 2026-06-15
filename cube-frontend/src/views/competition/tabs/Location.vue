<template>
  <div class="map-tab-container">
    <div class="address-info">
      <el-icon color="#409eff"><Location /></el-icon>
      <span class="address-text">
        <strong>详细地址：</strong>
        {{ compInfo.province }}{{ compInfo.city }}{{ compInfo.location }}
      </span>
    </div>

    <div id="comp-map" class="map-container" v-loading="loading"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { Location } from "@element-plus/icons-vue";
// 引入我们之前写好的地图初始化方法
import { initAMap } from "@/utils/map";

// 接收父组件 DetailLayout 传过来的比赛信息
const props = defineProps({
  compInfo: {
    type: Object,
    default: () => ({})
  }
});

const loading = ref(true);
let mapInstance = null; // 存放地图实例

const renderMap = async () => {
  // 确保有地址信息再开始渲染
  if (!props.compInfo.province) return;

  loading.value = true;
  try {
    // 1. 获取 AMap 对象
    const AMap = await initAMap();

    // 2. 初始化地图，挂载到 id="comp-map" 的 div 上
    if (!mapInstance) {
      mapInstance = new AMap.Map("comp-map", {
        zoom: 15, // 缩放级别
        center: [116.397428, 39.90923] // 默认先放个北京天安门兜底
      });
    }

    // 3. 将比赛的文字地址转为经纬度
    const address = `${props.compInfo.province}${props.compInfo.city}${props.compInfo.location}`;
    const geocoder = new AMap.Geocoder();

    geocoder.getLocation(address, (status, result) => {
      if (status === 'complete' && result.geocodes.length) {
        const lnglat = result.geocodes[0].location; // 拿到经纬度

        // 4. 把地图中心平移到该位置
        mapInstance.setCenter(lnglat);

        // 5. 在该位置添加一个红色标记点
        new AMap.Marker({
          map: mapInstance,
          position: lnglat,
          title: props.compInfo.name // 鼠标悬浮显示比赛名称
        });
      } else {
        console.warn("地址解析失败，可能地址太模糊");
      }
      loading.value = false;
    });

  } catch (e) {
    console.error("地图加载失败", e);
    loading.value = false;
  }
};

// 监听 compInfo 的变化。因为父组件的数据是异步请求的，刚进来时可能是个空对象
watch(
    () => props.compInfo,
    (newVal) => {
      if (newVal && newVal.province) {
        renderMap();
      }
    },
    { immediate: true } // 立即执行一次
);
</script>

<style scoped>
.map-tab-container {
  padding: 10px;
}

.address-info {
  margin-bottom: 20px;
  font-size: 16px;
  background-color: #f0f9eb;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #67c23a;
  display: flex;
  align-items: center;
}

.address-text {
  margin-left: 8px;
  color: #303133;
}

.map-container {
  width: 100%;
  height: 500px; /* 控制地图的高度 */
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>