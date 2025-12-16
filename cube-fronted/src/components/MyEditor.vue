<template>
  <div style="border: 1px solid #ccc; z-index: 100">
    <Toolbar
      style="border-bottom: 1px solid #ccc"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
    />
    <Editor
      style="height: 300px; overflow-y: hidden"
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      mode="default"
      @onCreated="handleCreated"
    />
  </div>
</template>

<script setup>
import "@wangeditor/editor/dist/css/style.css"; // 引入 css
import { onBeforeUnmount, ref, shallowRef, watch } from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";

// 接收父组件传来的 v-model 值
const props = defineProps(["modelValue"]);
const emit = defineEmits(["update:modelValue"]);

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef();

// 内容 HTML
const valueHtml = ref("");

// 监听父组件值的变化（回显数据）
watch(
  () => props.modelValue,
  (newVal) => {
    valueHtml.value = newVal;
  },
  { immediate: true }
);

// 监听内容变化，同步给父组件
watch(valueHtml, (newHtml) => {
  emit("update:modelValue", newHtml);
});

// 组件销毁时，及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor == null) return;
  editor.destroy();
});

const handleCreated = (editor) => {
  editorRef.value = editor; // 记录 editor 实例
};

// 工具栏配置
const toolbarConfig = {
  excludeKeys: ["group-video"], // 毕设不搞视频上传，屏蔽掉
};

// 编辑器配置
const editorConfig = {
  placeholder: "请输入内容...",
  MENU_CONF: {
    uploadImage: {
      // 允许 5M 以内的图片转 base64 存数据库（最省事的毕设图片方案）
      base64LimitSize: 5 * 1024 * 1024,
    },
  },
};
</script>
