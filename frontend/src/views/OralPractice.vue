<template>
  <div class="oral-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>口语练习</span>
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

    <!-- Recording Dialog -->
    <el-dialog v-model="recordingDialogVisible" :title="currentTest?.topic || '录音评测'" width="500px">
      <div v-if="currentTest" class="recording-content">
        <el-alert title="请大声朗读或就以下主题发表您的看法：" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />
        <h3 style="text-align: center; color: #409EFF; margin-bottom: 30px;">{{ currentTest.topic }}</h3>
        
        <div class="mic-status" style="text-align: center; margin-bottom: 20px;">
          <el-avatar :size="100" :style="{ backgroundColor: isRecording ? '#F56C6C' : '#909399' }">
            <el-icon :size="50" color="white"><Microphone /></el-icon>
          </el-avatar>
          <p style="margin-top: 15px; font-weight: bold;" :style="{ color: isRecording ? '#F56C6C' : '#909399' }">
            {{ isRecording ? '正在录音... (已开启麦克风)' : '准备就绪' }}
          </p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer" style="text-align: center;">
          <el-button v-if="!isRecording" type="primary" size="large" @click="startRecording">开始录音</el-button>
          <el-button v-else type="danger" size="large" @click="stopAndEvaluate" :loading="evaluating">结束并评测</el-button>
          <el-button @click="recordingDialogVisible = false" :disabled="isRecording">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const tableData = ref<any[]>([])
const loading = ref(false)

const recordingDialogVisible = ref(false)
const isRecording = ref(false)
const evaluating = ref(false)
const currentTest = ref<any>(null)

const fetchOral = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/oral/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载口语练习主题')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOral()
})

const handleStart = async () => {
  ElMessage.success('正在为您随机生成日常会话练习主题...')
  loading.value = true
  try {
    const res = await axios.post('/api/oral/generate')
    if (res.data.code === 200) {
      ElMessage.success('生成成功！')
      fetchOral()
    } else {
      ElMessage.error(res.data.msg || '生成失败')
    }
  } catch (error) {
    ElMessage.error('无法连接到服务器')
  } finally {
    loading.value = false
  }
}

const handlePractice = (row: any) => {
  currentTest.value = row
  isRecording.value = false
  recordingDialogVisible.value = true
}

const startRecording = () => {
  isRecording.value = true
}

const stopAndEvaluate = async () => {
  isRecording.value = false
  evaluating.value = true
  
  // 模拟调用第三方语音评测 API 花费的时间
  setTimeout(async () => {
    // 随机生成一个分数 60-100
    const mockScore = Math.floor(Math.random() * 41) + 60
    
    try {
      const res = await axios.put(`/api/oral/${currentTest.value.id}/evaluate`, { score: mockScore })
      if (res.data.code === 200) {
        ElMessage.success(`评测完成！本次得分: ${mockScore} 分`)
        recordingDialogVisible.value = false
        fetchOral()
        
        try {
          await axios.post('/api/record/add', { type: '口语练习', targetId: currentTest.value.id, duration: 60 })
        } catch (e) {}

      } else {
        ElMessage.error('评测分数保存失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，无法保存得分')
    } finally {
      evaluating.value = false
    }
  }, 1500)
}

const handleDelete = (row: any) => {
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
    } catch (error) {
      ElMessage.error('无法连接到服务器')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.oral-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
