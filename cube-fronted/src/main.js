import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 1. 引入 Element Plus 和 样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入中文语言包 (让日期选择器等显示中文)
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 2. 引入图标库
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 3. 使用 Element Plus，并设置语言为中文
app.use(ElementPlus, {
    locale: zhCn,
})

// 4. 循环注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.mount('#app')