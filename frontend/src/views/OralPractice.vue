<template>
  <div class="oral-container">
    <WhisperModelBadge variant="banner" module="oral" />
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            口语练习
            <WhisperModelBadge variant="tag" />
          </span>
          <el-button type="primary" icon="Microphone" @click="handleStart">开始录音评测</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="topic" label="练习主题" />
        <el-table-column prop="score" label="历史最高分" width="150" />
        <el-table-column prop="attempts" label="练习次数" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePractice(row)">去练习</el-button>
            <el-button v-if="currentRole !== 'User'" link type="danger" @click="handleDelete(row)">删除主题</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="recordingDialogVisible" :title="currentTest?.topic || '录音评测'" width="560px">
      <div v-if="currentTest" class="recording-content">
        <el-alert title="请先朗读下方英文参考句，系统将通过 Whisper 转写与 WER 计算发音得分。" type="info" show-icon :closable="false" style="margin-bottom: 16px;" />
        <p class="topic-label">练习主题</p>
        <h3 class="topic-title">{{ currentTest.topic }}</h3>
        <p class="topic-label">请朗读</p>
        <el-alert :title="referenceText" type="success" :closable="false" style="margin-bottom: 20px;" />

        <div class="mic-status">
          <el-avatar :size="100" :style="{ backgroundColor: isRecording ? '#F56C6C' : '#909399' }">
            <el-icon :size="50" color="white"><Microphone /></el-icon>
          </el-avatar>
          <p class="mic-label" :style="{ color: isRecording ? '#F56C6C' : '#909399' }">
            {{ isRecording ? '正在录音...' : '准备就绪' }}
          </p>
        </div>

        <el-alert v-if="lastResult" :title="resultTitle" :type="resultAlertType" :closable="false" style="margin-top: 16px;">
          <template #default>
            <p class="result-summary">{{ lastResult.feedback }}</p>
            <p v-if="lastResult.transcript" class="result-transcript">
              识别结果：{{ lastResult.transcript }}
            </p>
            <p v-if="readingTip" class="result-tip">{{ readingTip }}</p>
          </template>
        </el-alert>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!isRecording && !evaluating" type="primary" size="large" @click="startRecording">开始录音</el-button>
          <el-button v-else-if="isRecording" type="danger" size="large" @click="stopAndEvaluate" :loading="evaluating">
            {{ evaluating ? 'Whisper 评测中…' : '结束并评测' }}
          </el-button>
          <el-button v-else type="info" size="large" loading disabled>Whisper 评测中，请稍候…</el-button>
          <el-button @click="closeDialog" :disabled="evaluating">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted, computed, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import axios from 'axios'
import WhisperModelBadge from '../components/WhisperModelBadge.vue'

interface OralItem {
  id: number
  topic: string
  referenceText?: string
  score?: number
  attempts?: number
}

interface PronunciationResult {
  score: number
  wer: number
  transcript: string
  feedback: string
  suggestions?: string[]
  source?: string
}

const currentRole = inject<any>('currentRole', ref('User'))
const tableData = ref<OralItem[]>([])
const loading = ref(false)

const recordingDialogVisible = ref(false)
const isRecording = ref(false)
const evaluating = ref(false)
const currentTest = ref<OralItem | null>(null)
const lastResult = ref<PronunciationResult | null>(null)

let mediaRecorder: MediaRecorder | null = null
let mediaStream: MediaStream | null = null
let audioChunks: Blob[] = []

const referenceText = computed(() => currentTest.value?.referenceText || '')

const resultTitle = computed(() => {
  if (!lastResult.value) return ''
  if (lastResult.value.score <= 0) return '未能完成自动评测'
  return `得分 ${lastResult.value.score} · WER ${lastResult.value.wer}`
})

const resultAlertType = computed(() => {
  if (!lastResult.value) return 'info'
  if (lastResult.value.score <= 0) return 'error'
  if (lastResult.value.score >= 85) return 'success'
  if (lastResult.value.score >= 70) return 'warning'
  return 'error'
})

const readingTip = computed(() => {
  const tips = (lastResult.value?.suggestions ?? [])
    .filter(Boolean)
    .filter(t => !t.includes('pip install'))
    .filter(t => !t.includes('install openai-whisper'))
    .filter(t => !t.includes('语音引擎处理失败'))
    .filter(t => !t.includes('RuntimeError'))
    .slice(0, 2)
  return tips.join(' ')
})

const fetchOral = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/oral/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch {
    ElMessage.error('无法加载口语练习主题')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOral()
})

onBeforeUnmount(() => {
  stopMediaTracks()
})

const stopMediaTracks = () => {
  mediaStream?.getTracks().forEach(track => track.stop())
  mediaStream = null
}

const handleStart = async () => {
  ElMessage.success('正在为您随机生成日常会话练习主题...')
  loading.value = true
  try {
    const res = await axios.post('/api/oral/generate')
    if (res.data.code === 200) {
      ElMessage.success(res.data.message || res.data.data || '生成成功！')
      fetchOral()
    } else {
      ElMessage.warning(res.data.message || '生成失败')
    }
  } catch {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

const handlePractice = (row: OralItem) => {
  resetRecordingState()
  currentTest.value = row
  lastResult.value = null
  recordingDialogVisible.value = true
}

const resetRecordingState = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    try {
      mediaRecorder.stop()
    } catch {
      // ignore
    }
  }
  mediaRecorder = null
  stopMediaTracks()
  audioChunks = []
  isRecording.value = false
  evaluating.value = false
}

const closeDialog = () => {
  if (evaluating.value) return
  resetRecordingState()
  recordingDialogVisible.value = false
}

const pickMimeType = (): string | undefined => {
  const candidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4', 'audio/ogg;codecs=opus']
  return candidates.find(type => MediaRecorder.isTypeSupported(type))
}

const waitForRecorderStop = (recorder: MediaRecorder): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    const mimeType = recorder.mimeType || pickMimeType() || 'audio/webm'
    recorder.onstop = () => {
      resolve(new Blob(audioChunks, { type: mimeType }))
    }
    recorder.onerror = () => reject(new Error('录音失败'))
    if (recorder.state === 'recording') {
      recorder.stop()
    } else {
      resolve(new Blob(audioChunks, { type: mimeType }))
    }
  })
}

const startRecording = async () => {
  if (!referenceText.value) {
    ElMessage.warning('当前主题缺少英文参考句，无法评测')
    return
  }
  if (evaluating.value) return
  resetRecordingState()
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    audioChunks = []
    const mimeType = pickMimeType()
    mediaRecorder = mimeType ? new MediaRecorder(mediaStream, { mimeType }) : new MediaRecorder(mediaStream)
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }
    mediaRecorder.start(250)
    isRecording.value = true
  } catch {
    ElMessage.error('无法访问麦克风，请检查浏览器权限')
    resetRecordingState()
  }
}

const stopAndEvaluate = async () => {
  if (!mediaRecorder || !currentTest.value || evaluating.value) {
    return
  }
  evaluating.value = true
  const recorder = mediaRecorder
  mediaRecorder = null

  try {
    const blob = await waitForRecorderStop(recorder)
    stopMediaTracks()
    isRecording.value = false

    if (blob.size === 0) {
      ElMessage.warning('未录到有效音频，请重试')
      return
    }

    const formData = new FormData()
    formData.append('referenceText', referenceText.value)
    formData.append('audio', blob, blob.type.includes('mp4') ? 'recording.mp4' : 'recording.webm')

    ElMessage.info('Whisper 正在转写，首次评测可能需等待 10～30 秒…')

    const res = await axios.post(`/api/oral/${currentTest.value.id}/evaluate-audio`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180000,
    })

    if (res.data.code === 200) {
      const dto = res.data.data as PronunciationResult
      lastResult.value = dto
      if (dto.score > 0) {
        ElMessage.success(`评测完成！本次得分: ${dto.score} 分`)
      } else {
        ElMessage.warning('未能自动打分，请查看下方朗读建议')
      }
      fetchOral()

      if (dto.score > 0) {
        try {
          await axios.post('/api/record/add', {
            type: '口语练习',
            targetId: currentTest.value.id,
            duration: 60,
            score: dto.score,
          })
        } catch {
          // ignore record failure
        }
      }
    } else {
      ElMessage.error(res.data.message || '评测失败')
    }
    } catch (error: any) {
      const msg = error?.response?.data?.message
      if (error?.code === 'ECONNABORTED') {
        ElMessage.error('评测超时，Whisper 首次加载较慢，请稍后再试')
      } else if (msg) {
        ElMessage.error(msg)
        if (error?.response?.data?.data) {
          lastResult.value = error.response.data.data as PronunciationResult
        }
      } else {
        ElMessage.error('网络错误，无法完成发音评测')
      }
  } finally {
    evaluating.value = false
    audioChunks = []
  }
}

const handleDelete = (row: OralItem) => {
  ElMessageBox.confirm(`确定要删除口语主题【${row.topic}】吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/oral/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchOral()
      } else {
        ElMessage.error('删除失败')
      }
    } catch {
      ElMessage.error('无法连接到服务器')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.oral-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { display: inline-flex; align-items: center; gap: 8px; }
.topic-label { margin: 0 0 8px; color: #909399; font-size: 13px; }
.topic-title { text-align: center; color: #409EFF; margin: 0 0 20px; font-size: 18px; }
.mic-status { text-align: center; margin-bottom: 8px; }
.mic-label { margin-top: 15px; font-weight: bold; }
.dialog-footer { text-align: center; }
.result-summary { margin: 0 0 8px; font-weight: 500; color: #303133; }
.result-transcript { margin: 0 0 10px; color: #606266; font-size: 13px; }
.result-tip { margin: 0; color: #606266; font-size: 13px; line-height: 1.6; }
</style>
