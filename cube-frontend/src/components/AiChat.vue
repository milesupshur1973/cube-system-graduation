<template>
  <div>
    <div class="ai-float-btn" @click="dialogVisible = true">
      <el-icon size="24"><ChatDotRound /></el-icon>
      <span style="font-size: 12px; margin-left: 4px;">AI助手</span>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="魔方赛事智能助手"
      width="400px"
      :close-on-click-modal="false"
      class="ai-dialog"
    >
      <div class="chat-box" ref="chatBoxRef">
        <div v-for="(msg, index) in messages" :key="index" 
             :class="['message-row', msg.role === 'user' ? 'msg-right' : 'msg-left']">
          
          <el-avatar 
            v-if="msg.role === 'ai'" 
            :size="30" 
            icon="Service" 
            style="background-color: #409EFF; color: white;" 
          />
          
          <div class="message-content">
            {{ msg.content }}
          </div>

          <el-avatar v-if="msg.role === 'user'" :size="30" icon="UserFilled" />
        </div>
        
        <div v-if="loading" class="message-row msg-left">
          <el-avatar 
            :size="30" 
            icon="Service" 
            style="background-color: #409EFF; color: white;" 
          />
          <div class="message-content">
            <span class="dot">...</span>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputMsg"
          placeholder="问问我关于魔方的问题..."
          @keyup.enter="sendMessage"
          :disabled="loading"
        >
          <template #append>
            <el-button @click="sendMessage" :loading="loading">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, UserFilled, Service } from '@element-plus/icons-vue'
import request from '@/utils/request' // 引入你的 axios 封装

const dialogVisible = ref(false)
const inputMsg = ref('')
const loading = ref(false)
const chatBoxRef = ref(null)

// 初始消息
const messages = ref([
  { role: 'ai', content: '你好！我是你的魔方赛事小助手。关于比赛规则、WCA项目或者还原技巧，都可以问我哦！' }
])

// 发送消息
const sendMessage = async () => {
  if (!inputMsg.value.trim()) return

  // 1. 添加用户消息到列表
  const question = inputMsg.value
  messages.value.push({ role: 'user', content: question })
  inputMsg.value = ''
  scrollToBottom()

  // 2. 调用后端 API
  loading.value = true
  try {
    const res = await request.post('/ai/chat', question, {
        headers: { 'Content-Type': 'text/plain' }
    })
    
    // ✅ 修复点：先解包！
    // axios 返回的 res.data 才是我们的 Result 对象
    const result = res.data; 

    // ✅ 修复点：使用解包后的 result 变量进行判断
    if (result.code === 200) {
       messages.value.push({ role: 'ai', content: result.data })
    } else {
       // 如果出错，显示后端返回的具体 msg
       messages.value.push({ role: 'ai', content: result.msg || '抱歉，我好像出故障了。' })
    }
  } catch (error) {
    messages.value.push({ role: 'ai', content: '网络连接失败，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatBoxRef.value) {
      chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
/* 悬浮按钮样式 */
.ai-float-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background-color: #409EFF;
  border-radius: 50%;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(0,0,0,0.3);
  cursor: pointer;
  z-index: 9999;
  transition: all 0.3s;
}
.ai-float-btn:hover {
  transform: scale(1.1);
  background-color: #66b1ff;
}

/* 聊天框样式 */
.chat-box {
  height: 400px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
  border: 1px solid #e4e7ed;
}

.message-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.msg-left {
  flex-direction: row;
}

.msg-right {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  margin: 0 10px;
  word-break: break-all;
}

.msg-left .message-content {
  background-color: white;
  color: #333;
  border-top-left-radius: 0;
}

.msg-right .message-content {
  background-color: #95d475;
  color: #333;
  border-top-right-radius: 0;
}

.input-area {
  margin-top: 10px;
}
</style>