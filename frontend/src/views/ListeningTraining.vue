<template>
  <div class="listening-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>听力训练</span>
          <el-button type="primary" icon="Headset" @click="handleNewListen">开始新听力</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="听力材料" />
        <el-table-column prop="category" label="分类" width="150" />
        <el-table-column prop="duration" label="时长" width="120" />
        <el-table-column prop="score" label="历史得分" width="120">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</span>
            <span v-else>未作答</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePlay(row)">
              <el-icon><VideoPlay /></el-icon>播放
            </el-button>
            <el-button link type="primary" @click="handleDoTest(row)">做题</el-button>
            <el-button link type="success" @click="handleViewKey(row)">查看解析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Audio Player Dialog -->
    <el-dialog v-model="playDialogVisible" :title="currentTest?.title || '听力播放'" width="40%" top="15vh">
      <div v-if="currentTest" class="audio-content">
        <el-alert title="请仔细听录音，您可以反复播放。" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />
        <audio controls :src="currentTest.audioUrl" style="width: 100%;"></audio>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="playDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Test Dialog -->
    <el-dialog v-model="testDialogVisible" :title="currentTest?.title || '听力测试'" width="60%" top="5vh">
      <div v-if="currentTest" class="test-content">
        <el-alert title="注意：您可以边听边作答。" type="warning" show-icon :closable="false" style="margin-bottom: 15px;" />
        <audio controls :src="currentTest.audioUrl" style="width: 100%; margin-bottom: 20px;"></audio>
        
        <div class="questions-box">
          <h3>听力题目</h3>
          <div v-for="(q, index) in mockQuestions" :key="index" class="question-item">
            <p><strong>{{ index + 1 }}. {{ q.question }}</strong></p>
            <el-radio-group v-model="answers[index]" class="options-group">
              <el-radio v-for="(opt, oIndex) in q.options" :key="oIndex" :label="opt.value">
                {{ opt.label }}
              </el-radio>
            </el-radio-group>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="testDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTest" :loading="submitting">提交答案</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Key Dialog -->
    <el-dialog v-model="keyDialogVisible" :title="(currentTest?.title || '') + ' - 答案解析'" width="50%" top="5vh">
      <div v-if="currentTest" class="key-content">
        <el-alert title="注意" type="info" description="以下为标准答案及解析。" show-icon :closable="false" style="margin-bottom: 20px;" />
        
        <div v-for="(q, index) in mockQuestions" :key="index" class="key-item">
          <p><strong>题目 {{ index + 1 }}:</strong> {{ q.question }}</p>
          <p><span class="label">标准答案:</span> <el-tag type="success">{{ q.correct }}</el-tag></p>
          <p><span class="label">听力原文片段:</span> <span class="explanation">{{ q.transcript }}</span></p>
          <p><span class="label">解析:</span> <span class="explanation">{{ q.explanation }}</span></p>
          <el-divider />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="keyDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import axios from 'axios'

const tableData = ref<any[]>([])
const loading = ref(false)

const playDialogVisible = ref(false)
const testDialogVisible = ref(false)
const keyDialogVisible = ref(false)
const currentTest = ref<any>(null)
const answers = ref<string[]>([])
const submitting = ref(false)

const mockQuestions = [
  {
    question: "What are the speakers discussing?",
    options: [
      { label: "A. A weekend trip", value: "A" },
      { label: "B. A new software program", value: "B" },
      { label: "C. A job interview", value: "C" },
      { label: "D. Dinner plans", value: "D" }
    ],
    correct: "B",
    transcript: "M: Have you tried out the new software update they pushed yesterday?",
    explanation: "The male speaker explicitly mentions trying out a new software update."
  },
  {
    question: "How does the woman feel about the update?",
    options: [
      { label: "A. She thinks it's confusing", value: "A" },
      { label: "B. She loves the new interface", value: "B" },
      { label: "C. She hasn't downloaded it yet", value: "C" },
      { label: "D. She wants to revert to the old version", value: "D" }
    ],
    correct: "B",
    transcript: "W: Yes, I have! The interface is so much cleaner and easier to use.",
    explanation: "The woman praises the new interface, indicating she loves it."
  }
]

const fetchListening = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/listening/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载听力列表')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchListening()
})

const handleNewListen = async () => {
  ElMessage.success('正在为您生成新的听力训练材料...')
  try {
    const res = await axios.post('/api/listening/generate')
    if (res.data.code === 200) {
      ElMessage.success('生成成功！')
      fetchListening()
    } else {
      ElMessage.error(res.data.msg || '生成失败')
    }
  } catch (error) {
    // 隐藏无法连接报错，axios 拦截器如果返回401会自动跳登录
  }
}

const handlePlay = (row: any) => {
  currentTest.value = row
  playDialogVisible.value = true
}

const handleDoTest = (row: any) => {
  currentTest.value = row
  answers.value = Array(mockQuestions.length).fill('')
  testDialogVisible.value = true
}

const handleViewKey = (row: any) => {
  currentTest.value = row
  keyDialogVisible.value = true
}

const submitTest = async () => {
  if (answers.value.some(a => !a)) {
    ElMessage.warning('请回答所有问题后再提交')
    return
  }

  let correctCount = 0
  answers.value.forEach((ans, index) => {
    if (ans === mockQuestions[index].correct) {
      correctCount++
    }
  })
  
  const calculatedScore = Math.round((correctCount / mockQuestions.length) * 100)
  
  submitting.value = true
  try {
    const res = await axios.put(`/api/listening/${currentTest.value.id}/score`, { score: calculatedScore })
    if (res.data.code === 200) {
      ElMessage.success(`提交成功！您的得分是: ${calculatedScore} 分`)
      testDialogVisible.value = false
      fetchListening()

      try {
        await axios.post('/api/record/add', { type: '听力训练', targetId: currentTest.value.id, duration: 300 })
      } catch (e) {}

    } else {
      ElMessage.error('分数上传失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，无法提交得分')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.listening-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.audio-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.test-content {
  display: flex;
  flex-direction: column;
}
.questions-box {
  padding: 10px 0;
}
.question-item {
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #ebeef5;
}
.options-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  margin-top: 15px;
  padding-left: 10px;
}
.options-group .el-radio {
  margin-right: 0;
  white-space: normal;
  display: flex;
  align-items: flex-start;
}
.options-group :deep(.el-radio__label) {
  text-align: left;
  line-height: 1.5;
  padding-left: 10px;
}
.key-item {
  margin-bottom: 15px;
}
.label {
  font-weight: bold;
  color: #606266;
  margin-right: 8px;
}
.explanation {
  color: #666;
  line-height: 1.6;
}
</style>
