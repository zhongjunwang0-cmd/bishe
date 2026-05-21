<template>
  <div class="reading-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>阅读理解</span>
          <el-button type="primary" icon="Document" @click="handleNewTest">开始新测试</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="文章标题" />
        <el-table-column prop="difficulty" label="难度" width="120">
          <template #default="{ row }">
            <el-tag :type="row.difficulty === 'Hard' ? 'danger' : (row.difficulty === 'Medium' ? 'warning' : 'success')">
              {{ row.difficulty || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="历史得分" width="150">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</span>
            <span v-else>未作答</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDoTest(row)">做题</el-button>
            <el-button link type="success" @click="handleViewKey(row)">查看解析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="testDialogVisible" :title="currentTest?.title || '阅读测试'" width="60%" top="5vh">
      <div v-if="currentTest" class="test-content" v-loading="detailLoading">
        <div class="passage-box">
          <h3>阅读材料</h3>
          <p class="passage-text">{{ currentTest.content }}</p>
        </div>
        <div class="questions-box" v-if="questions.length">
          <h3>测试题目</h3>
          <div v-for="(q, index) in questions" :key="index" class="question-item">
            <p><strong>{{ index + 1 }}. {{ q.question }}</strong></p>
            <el-radio-group v-model="answers[index]" class="options-group">
              <el-radio v-for="(opt, oIndex) in q.options" :key="oIndex" :label="opt.value">
                {{ opt.label }}
              </el-radio>
            </el-radio-group>
          </div>
        </div>
        <el-empty v-else description="该测试暂无结构化题目，请使用「开始新测试」从题库生成" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="testDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTest" :loading="submitting" :disabled="!questions.length">提交答案</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="keyDialogVisible" :title="(currentTest?.title || '') + ' - 答案解析'" width="50%" top="5vh">
      <div v-if="currentTest" class="key-content" v-loading="detailLoading">
        <el-alert title="注意" type="info" description="以下为标准答案及解析。" show-icon :closable="false" style="margin-bottom: 20px;" />
        <div v-for="(q, index) in questions" :key="index" class="key-item">
          <p><strong>题目 {{ index + 1 }}:</strong> {{ q.question }}</p>
          <p><span class="label">标准答案:</span> <el-tag type="success">{{ answerKeys[index]?.correct }}</el-tag></p>
          <p><span class="label">解析:</span> <span class="explanation">{{ answerKeys[index]?.explanation }}</span></p>
          <el-divider />
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="keyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const tableData = ref<any[]>([])
const loading = ref(false)
const detailLoading = ref(false)
const testDialogVisible = ref(false)
const keyDialogVisible = ref(false)
const currentTest = ref<any>(null)
const questions = ref<any[]>([])
const answerKeys = ref<any[]>([])
const answers = ref<string[]>([])
const submitting = ref(false)

const fetchReadings = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/reading/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch {
    ElMessage.error('无法加载阅读理解列表')
  } finally {
    loading.value = false
  }
}

const loadDetail = async (id: number, withAnswers = false) => {
  detailLoading.value = true
  questions.value = []
  answerKeys.value = []
  try {
    const res = await axios.get(`/api/reading/${id}`)
    if (res.data.code === 200) {
      currentTest.value = res.data.data
      questions.value = res.data.data.questions || []
    }
    if (withAnswers) {
      const ansRes = await axios.get(`/api/reading/${id}/answers`)
      if (ansRes.data.code === 200) {
        answerKeys.value = ansRes.data.data || []
      }
    }
  } catch {
    ElMessage.error('加载题目失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(fetchReadings)

const handleNewTest = async () => {
  try {
    const res = await axios.post('/api/reading/generate')
    if (res.data.code === 200) {
      ElMessage.success('已从题库随机生成阅读测试')
      fetchReadings()
    } else {
      ElMessage.error(res.data.message || '生成失败')
    }
  } catch {}
}

const handleDoTest = async (row: any) => {
  currentTest.value = row
  testDialogVisible.value = true
  await loadDetail(row.id)
  answers.value = Array(questions.value.length).fill('')
}

const handleViewKey = async (row: any) => {
  currentTest.value = row
  keyDialogVisible.value = true
  await loadDetail(row.id, true)
}

const submitTest = async () => {
  if (answers.value.some(a => !a)) {
    ElMessage.warning('请回答所有问题后再提交')
    return
  }
  submitting.value = true
  try {
    const res = await axios.post(`/api/reading/${currentTest.value.id}/submit`, { answers: answers.value })
    if (res.data.code === 200) {
      const score = res.data.data.score
      ElMessage.success(`提交成功！您的得分是: ${score} 分`)
      testDialogVisible.value = false
      fetchReadings()
      try {
        await axios.post('/api/record/add', { type: '阅读理解', targetId: currentTest.value.id, duration: 300, score })
      } catch {}
    } else {
      ElMessage.error(res.data.message || '提交失败')
    }
  } catch {
    ElMessage.error('网络错误，无法提交得分')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.reading-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.test-content { display: flex; flex-direction: column; gap: 20px; }
.passage-box { background-color: #f9fafc; padding: 15px; border-radius: 8px; border: 1px solid #ebeef5; }
.passage-text { line-height: 1.8; font-size: 15px; color: #303133; white-space: pre-wrap; }
.questions-box { padding: 10px 0; }
.question-item { margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px dashed #ebeef5; }
.options-group { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; margin-top: 15px; padding-left: 10px; }
.options-group .el-radio { margin-right: 0; white-space: normal; display: flex; align-items: flex-start; }
.options-group :deep(.el-radio__label) { text-align: left; line-height: 1.5; padding-left: 10px; }
.key-item { margin-bottom: 15px; }
.label { font-weight: bold; color: #606266; margin-right: 8px; }
.explanation { color: #666; line-height: 1.6; }
</style>
