<template>
  <div class="ai-container">
    <T5ModelBadge variant="banner" module="ai" />

    <el-card shadow="hover" class="chat-card">
      <template #header>
        <div class="card-header">
          <div class="title-block">
            <span>AI 辅导学习助手</span>
            <T5ModelBadge variant="tag" />
          </div>
          <div class="header-tags">
            <el-tag v-if="lastSource === 'external'" type="success" size="small">外部 AI</el-tag>
            <el-tag v-else-if="lastSource === 't5_gec'" type="warning" size="small">T5-GEC 响应中</el-tag>
            <el-tag v-else-if="lastSource === 'rule_engine'" type="info" size="small">规则引擎</el-tag>
            <el-tag type="success">在线互动</el-tag>
          </div>
        </div>
      </template>
      <div class="chat-box" ref="chatBox">
        <div v-for="(msg, index) in messages" :key="index" :class="['chat-bubble', msg.role]">
          <div
            v-if="msg.role === 'ai'"
            class="bubble-content ai-content"
            v-html="formatAiMessage(msg.content)"
          ></div>
          <div v-else class="bubble-content user-content">{{ msg.content }}</div>
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
import T5ModelBadge from '../components/T5ModelBadge.vue'

const chatBox = ref<HTMLElement | null>(null)
const inputText = ref('')
const lastSource = ref('')

const messages = ref([
  { role: 'ai', content: '您好！我是您的专属 AI 英语辅导老师。本模块已接入 T5-GEC 语法纠错模型（JFLEG 微调），您可以直接发送英文句子让我批改，也可以提问单词、语法等问题。' }
])

const escapeHtml = (text: string) =>
  text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')

const formatAiMessage = (content: string) => {
  const lines = content.replace(/\*\*/g, '').split('\n')
  return lines
    .map((line) => {
      const trimmed = line.trim()
      if (!trimmed) {
        return '<div class="ai-gap"></div>'
      }
      if (trimmed.startsWith('【') && trimmed.endsWith('】')) {
        return `<div class="ai-title">${escapeHtml(trimmed)}</div>`
      }
      if (/^模型：/.test(trimmed)) {
        return `<div class="ai-model-tag">${escapeHtml(trimmed)}</div>`
      }
      if (trimmed === '────────────────────────') {
        return '<div class="ai-divider"></div>'
      }
      if (/^(原句|修改后|修改后全文|语法讲解.*|补充说明|共分析 \d+ 个句子)$/.test(trimmed)) {
        return `<div class="ai-label">${escapeHtml(trimmed)}</div>`
      }
      if (/^第 \d+ 句$/.test(trimmed)) {
        return `<div class="ai-section">${escapeHtml(trimmed)}</div>`
      }
      if (/^\d+）/.test(trimmed)) {
        return `<div class="ai-issue-title">${escapeHtml(trimmed)}</div>`
      }
      if (/^\s{2,}/.test(line)) {
        return `<div class="ai-indent">${escapeHtml(line.trim())}</div>`
      }
      if (/^(问题|修改|讲解)：/.test(trimmed)) {
        return `<div class="ai-detail">${escapeHtml(trimmed)}</div>`
      }
      return `<div class="ai-line">${escapeHtml(trimmed)}</div>`
    })
    .join('')
}

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
      lastSource.value = res.data.data.source || ''
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
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.title-block { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.header-tags { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
  height: 400px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.chat-bubble {
  max-width: 85%;
  padding: 14px 18px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
}
.chat-bubble.ai {
  background: #fff;
  align-self: flex-start;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}
.chat-bubble.user {
  background: #409eff;
  color: white;
  align-self: flex-end;
}
.user-content {
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-content :deep(.ai-title) {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.ai-content :deep(.ai-model-tag) {
  font-size: 12px;
  color: #e6a23c;
  margin-bottom: 8px;
}
.ai-content :deep(.ai-section) {
  font-weight: 600;
  color: #409eff;
  margin: 8px 0 6px;
}
.ai-content :deep(.ai-label) {
  font-weight: 600;
  color: #606266;
  margin-top: 8px;
  margin-bottom: 4px;
}
.ai-content :deep(.ai-indent) {
  color: #303133;
  background: #f5f7fa;
  border-left: 3px solid #409eff;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  word-break: break-word;
}
.ai-content :deep(.ai-issue-title) {
  font-weight: 600;
  color: #e6a23c;
  margin-top: 6px;
}
.ai-content :deep(.ai-detail) {
  color: #606266;
  padding-left: 12px;
  margin-bottom: 2px;
}
.ai-content :deep(.ai-line) {
  color: #303133;
  margin-bottom: 2px;
}
.ai-content :deep(.ai-gap) {
  height: 8px;
}
.ai-content :deep(.ai-divider) {
  height: 1px;
  background: #e4e7ed;
  margin: 12px 0;
}
.input-area { margin-top: auto; }
</style>
