<template>
  <div class="cloze-container">
    <T5ModelBadge variant="banner" module="cloze" />

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="title-block">
            <span>选词填空</span>
            <T5ModelBadge variant="tag" />
          </div>
          <el-button type="primary" icon="EditPen" @click="handleNewCloze">开始新填空</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="练习标题" />
        <el-table-column prop="blanksCount" label="填空数量" width="120" />
        <el-table-column prop="completionStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.completionStatus === 'Completed' ? 'success' : 'info'">
              {{ row.completionStatus === 'Completed' ? '已完成' : '未开始' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="历史得分" width="120">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined">{{ row.score }} 分</span>
            <span v-else>未作答</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleStart(row)">做题</el-button>
            <el-button link type="success" @click="handleViewKey(row)" :disabled="row.completionStatus !== 'Completed'">查看解析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="testDialogVisible" :title="currentTest?.title || '填空测试'" width="60%" top="5vh">
      <div v-if="currentTest" class="test-content" v-loading="detailLoading">
        <el-alert title="请通读全文并为每个空格选择最恰当的词汇。" type="info" show-icon :closable="false" style="margin-bottom: 15px;" />
        <div class="passage-box" v-if="questions.length">
          <p class="passage-text">
            <template v-for="(part, idx) in passageParts" :key="idx">
              <span v-if="part.type === 'text'">{{ part.value }}</span>
              <span v-else class="blank-slot">({{ part.index }})
                <el-select v-model="answers[part.index - 1]" placeholder="选择" size="small" style="width: 120px;">
                  <el-option v-for="opt in questions[part.index - 1]?.options || []" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </span>
            </template>
          </p>
        </div>
        <el-empty v-else description="该测试暂无结构化题目，请使用「开始新填空」从题库生成" />
        <el-divider v-if="questions.length">造句巩固 · T5 语法纠错</el-divider>
        <div v-if="questions.length" class="sentence-box">
          <p class="sentence-hint">用本篇文章关键词造句，检验语法是否正确：</p>
          <el-input v-model="sentenceInput" type="textarea" :rows="2" placeholder="例如：The research shows that vocabulary learning is important." />
          <div class="sentence-actions">
            <el-button type="warning" :loading="sentenceLoading" @click="checkSentence">T5 纠错</el-button>
          </div>
          <div v-if="sentenceResult" class="sentence-result">
            <p><strong>修改后：</strong>{{ sentenceResult.corrected }}</p>
            <ul v-if="sentenceResult.issues?.length">
              <li v-for="(issue, idx) in sentenceResult.issues" :key="idx">{{ issue.message }}</li>
            </ul>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTest" :loading="submitting" :disabled="!questions.length">提交答案</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="keyDialogVisible" :title="(currentTest?.title || '') + ' - 答案解析'" width="50%" top="5vh">
      <div v-if="currentTest" class="key-content" v-loading="detailLoading">
        <el-alert title="以下为标准答案及解析。" type="success" show-icon :closable="false" style="margin-bottom: 20px;" />
        <div class="passage-box" style="margin-bottom: 20px;" v-if="filledPassage">
          <p class="passage-text">{{ filledPassage }}</p>
        </div>
        <div v-for="(ans, index) in answerKeys" :key="index" class="key-item">
          <p><strong>空格 ({{ index + 1 }}):</strong></p>
          <p><span class="label">标准答案:</span> <el-tag type="success">{{ ans.correct }}</el-tag></p>
          <p><span class="label">解析:</span> <span class="explanation">{{ ans.explanation }}</span></p>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import T5ModelBadge from '../components/T5ModelBadge.vue'

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
const sentenceInput = ref('')
const sentenceLoading = ref(false)
const sentenceResult = ref<any>(null)

const parsePassage = (content: string) => {
  if (!content) return []
  const parts: Array<{ type: 'text' | 'blank'; value?: string; index?: number }> = []
  const regex = /(\(\d+\)_____)/g
  let lastIndex = 0
  let match
  while ((match = regex.exec(content)) !== null) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', value: content.slice(lastIndex, match.index) })
    }
    const index = parseInt(match[1].replace(/\D/g, ''), 10)
    parts.push({ type: 'blank', index })
    lastIndex = regex.lastIndex
  }
  if (lastIndex < content.length) {
    parts.push({ type: 'text', value: content.slice(lastIndex) })
  }
  return parts
}

const passageParts = computed(() => parsePassage(currentTest.value?.content || ''))

const filledPassage = computed(() => {
  let text = currentTest.value?.content || ''
  answerKeys.value.forEach((ans, i) => {
    text = text.replace(`(${i + 1})_____`, ans.correct)
  })
  return text.replace(/\n\n选项：[\s\S]*/g, '').trim()
})

const fetchClozes = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/cloze/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch {
    ElMessage.error('无法加载选词填空列表')
  } finally {
    loading.value = false
  }
}

const loadDetail = async (id: number, withAnswers = false) => {
  detailLoading.value = true
  questions.value = []
  answerKeys.value = []
  try {
    const res = await axios.get(`/api/cloze/${id}`)
    if (res.data.code === 200) {
      currentTest.value = res.data.data
      questions.value = res.data.data.questions || []
    }
    if (withAnswers) {
      const ansRes = await axios.get(`/api/cloze/${id}/answers`)
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

onMounted(fetchClozes)

const handleNewCloze = async () => {
  try {
    const res = await axios.post('/api/cloze/generate')
    if (res.data.code === 200) {
      ElMessage.success('已从题库随机生成选词填空')
      fetchClozes()
    } else {
      ElMessage.error(res.data.message || '生成失败')
    }
  } catch {}
}

const handleStart = async (row: any) => {
  currentTest.value = row
  testDialogVisible.value = true
  sentenceInput.value = ''
  sentenceResult.value = null
  await loadDetail(row.id)
  answers.value = Array(questions.value.length).fill('')
}

const checkSentence = async () => {
  if (!sentenceInput.value.trim()) {
    ElMessage.warning('请先输入英文句子')
    return
  }
  sentenceLoading.value = true
  sentenceResult.value = null
  try {
    const res = await axios.post('/api/grammar/correct', { text: sentenceInput.value.trim() })
    if (res.data.code === 200) {
      sentenceResult.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '纠错失败')
    }
  } catch {
    ElMessage.error('语法纠错服务不可用')
  } finally {
    sentenceLoading.value = false
  }
}

const handleViewKey = async (row: any) => {
  currentTest.value = row
  keyDialogVisible.value = true
  await loadDetail(row.id, true)
}

const submitTest = async () => {
  if (answers.value.some(a => !a)) {
    ElMessage.warning('请完成所有填空后再提交')
    return
  }
  submitting.value = true
  try {
    const res = await axios.post(`/api/cloze/${currentTest.value.id}/submit`, { answers: answers.value })
    if (res.data.code === 200) {
      const score = res.data.data.score
      ElMessage.success(`提交成功！您的得分是: ${score} 分`)
      testDialogVisible.value = false
      fetchClozes()
      try {
        await axios.post('/api/record/add', { type: '选词填空', targetId: currentTest.value.id, duration: 300, score })
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
.cloze-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.title-block { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.passage-box { background-color: #f9fafc; padding: 25px; border-radius: 8px; border: 1px solid #ebeef5; line-height: 2.2; }
.passage-text { font-size: 16px; color: #303133; white-space: pre-wrap; }
.blank-slot { display: inline-block; margin: 0 4px; font-weight: bold; color: #409EFF; }
.key-item { margin-bottom: 15px; }
.label { font-weight: bold; color: #606266; margin-right: 8px; }
.explanation { color: #666; line-height: 1.6; }
.sentence-box { margin-top: 8px; padding: 12px; background: #fff; border: 1px dashed #dcdfe6; border-radius: 8px; }
.sentence-hint { color: #606266; margin: 0 0 8px; font-size: 13px; }
.sentence-actions { margin-top: 8px; text-align: right; }
.sentence-result { margin-top: 10px; line-height: 1.6; font-size: 14px; }
.sentence-result ul { margin: 6px 0; padding-left: 18px; }
</style>
