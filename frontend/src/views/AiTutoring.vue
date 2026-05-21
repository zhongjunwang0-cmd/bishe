<template>
  <div class="ai-container">
    <el-card shadow="hover" class="chat-card">
      <template #header>
        <div class="card-header">
          <span>AI 辅导学习助手</span>
          <el-tag type="success">在线互动</el-tag>
        </div>
      </template>
      <div class="chat-box" ref="chatBox">
        <div v-for="(msg, index) in messages" :key="index" :class="['chat-bubble', msg.role]">
          <div class="bubble-content">{{ msg.content }}</div>
        </div>
      </div>
      <div class="input-area">
        <el-input v-model="inputText" placeholder="向AI提问有关英语学习的问题..." @keyup.enter="sendMessage" clearable>
          <template #append>
            <el-button icon="Position" @click="sendMessage">发送</el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import axios from 'axios'

const chatBox = ref<HTMLElement | null>(null)
const inputText = ref('')

const messages = ref([
  { role: 'ai', content: '您好！我是您的专属 AI 英语辅导老师。请问有什么我可以帮您的吗？您可以问我单词意思、语法解析，或者让我帮您修改作文。' }
])

const sendMessage = async () => {
  if (!inputText.value.trim()) return
  
  const query = inputText.value
  messages.value.push({ role: 'user', content: query })
  inputText.value = ''
  
  scrollToBottom()
  
  try {
    const res = await axios.post('/api/ai/advice', { content: query })
    if (res.data.code === 200) {
      messages.value.push({ role: 'ai', content: res.data.data.advice })
    }
  } catch (error) {
    messages.value.push({ role: 'ai', content: '抱歉，AI 助手暂时无法响应，请稍后再试。' })
  }
  scrollToBottom()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBox.value) {
      chatBox.value.scrollTop = chatBox.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-container { padding: 10px; height: 100%; }
.chat-card { height: calc(100vh - 120px); display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.chat-box { flex: 1; overflow-y: auto; padding: 20px; background: #f9f9f9; border-radius: 8px; margin-bottom: 20px; height: 400px; display: flex; flex-direction: column; gap: 15px; }
.chat-bubble { max-width: 80%; padding: 12px 16px; border-radius: 8px; font-size: 14px; line-height: 1.5; }
.chat-bubble.ai { background: #fff; align-self: flex-start; border: 1px solid #ebeef5; }
.chat-bubble.user { background: #409eff; color: white; align-self: flex-end; }
.input-area { margin-top: auto; }
</style>
