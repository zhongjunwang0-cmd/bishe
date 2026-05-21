<template>
  <div class="test-management-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>题库管理（阅读 / 听力 / 选词填空）</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="筛选题型" clearable style="width: 140px; margin-right: 10px;" @change="fetchList">
              <el-option label="阅读理解" value="READING" />
              <el-option label="听力测试" value="LISTENING" />
              <el-option label="选词填空" value="CLOZE" />
            </el-select>
            <el-button type="primary" icon="Plus" @click="openDialog()">新增题库</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="题库名称" min-width="200" />
        <el-table-column prop="moduleType" label="题型" width="120">
          <template #default="{ row }">{{ moduleLabel(row.moduleType) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'Active' ? 'success' : 'info'">
              {{ row.status === 'Active' ? '使用中' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 'Active' ? '下线' : '上线' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题库' : '新增题库'"
      width="780px"
      top="4vh"
      destroy-on-close
    >
      <el-scrollbar max-height="68vh">
        <el-form :model="form" label-width="100px" class="edit-form">
          <el-form-item label="题库名称">
            <el-input v-model="form.title" placeholder="请输入题库名称" />
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="form.moduleType" style="width: 100%" :disabled="isEdit" @change="onModuleTypeChange">
              <el-option label="阅读理解" value="READING" />
              <el-option label="听力测试" value="LISTENING" />
              <el-option label="选词填空" value="CLOZE" />
            </el-select>
          </el-form-item>
          <el-form-item :label="form.moduleType === 'CLOZE' ? '填空文章' : '正文/原文'">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="4"
              :placeholder="form.moduleType === 'CLOZE'
                ? '文章中用 (1)_____、(2)_____ 标记空格，例如：Learning a language (1)_____ time.'
                : '阅读 passage 或听力 transcript'"
            />
          </el-form-item>
          <el-form-item v-if="form.moduleType === 'READING'" label="难度">
            <el-select v-model="form.difficulty" style="width: 100%">
              <el-option label="Easy" value="Easy" />
              <el-option label="Medium" value="Medium" />
              <el-option label="Hard" value="Hard" />
            </el-select>
          </el-form-item>
          <template v-if="form.moduleType === 'LISTENING'">
            <el-form-item label="分类">
              <el-input v-model="form.category" placeholder="如：日常口语 / IELTS模拟" />
            </el-form-item>
            <el-form-item label="时长">
              <el-input v-model="form.duration" placeholder="如：04:15" />
            </el-form-item>
            <el-form-item label="音频地址">
              <div class="audio-url-row">
                <el-input v-model="form.audioUrl" placeholder="MP3 URL 或上传本地音频" />
                <el-upload
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  accept=".mp3,.wav,.m4a,.ogg,audio/*"
                  :on-change="handleAudioUpload"
                >
                  <el-button type="primary" :loading="audioUploading">上传音频</el-button>
                </el-upload>
              </div>
              <audio v-if="form.audioUrl" controls :src="form.audioUrl" class="audio-preview" />
            </el-form-item>
          </template>

          <!-- 阅读 / 听力：选择题 -->
          <template v-if="form.moduleType !== 'CLOZE'">
            <div class="section-bar">
              <span class="section-title">题目设置</span>
              <el-button type="primary" link icon="Plus" @click="addMcqQuestion">添加题目</el-button>
            </div>
            <el-empty v-if="!mcqQuestions.length" description="暂无题目，请点击添加" :image-size="60" />
            <div v-for="(q, qi) in mcqQuestions" :key="qi" class="question-card">
              <div class="question-card-header">
                <span class="question-no">第 {{ qi + 1 }} 题</span>
                <el-button link type="danger" size="small" @click="removeMcqQuestion(qi)" :disabled="mcqQuestions.length <= 1">删除</el-button>
              </div>
              <el-form-item label="题干" label-width="80px">
                <el-input v-model="q.question" type="textarea" :rows="2" placeholder="请输入题目内容" />
              </el-form-item>
              <el-form-item
                v-for="opt in q.options"
                :key="opt.value"
                :label="'选项 ' + opt.value"
                label-width="80px"
              >
                <el-input v-model="opt.label" :placeholder="'选项 ' + opt.value + ' 内容'" />
              </el-form-item>
              <el-form-item label="正确答案" label-width="80px">
                <el-radio-group v-model="q.correct">
                  <el-radio v-for="opt in q.options" :key="opt.value" :value="opt.value">{{ opt.value }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="解析" label-width="80px">
                <el-input v-model="q.explanation" type="textarea" :rows="2" placeholder="请输入答案解析" />
              </el-form-item>
              <el-form-item v-if="form.moduleType === 'LISTENING'" label="原文片段" label-width="80px">
                <el-input v-model="q.transcript" type="textarea" :rows="2" placeholder="对应听力原文（选填）" />
              </el-form-item>
            </div>
          </template>

          <!-- 选词填空 -->
          <template v-else>
            <div class="section-bar">
              <span class="section-title">空格设置</span>
              <el-button type="primary" link icon="Plus" @click="addClozeBlank">添加空格</el-button>
            </div>
            <el-alert
              title="提示：文章中使用 (1)_____、(2)_____ 标记空格，空格编号需与下方设置一致"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 12px;"
            />
            <el-empty v-if="!clozeBlanks.length" description="暂无空格，请点击添加" :image-size="60" />
            <div v-for="(blank, bi) in clozeBlanks" :key="bi" class="question-card">
              <div class="question-card-header">
                <span class="question-no">空格 ({{ blank.blankIndex }})</span>
                <el-button link type="danger" size="small" @click="removeClozeBlank(bi)" :disabled="clozeBlanks.length <= 1">删除</el-button>
              </div>
              <el-form-item label="备选项" label-width="80px">
                <div class="option-list">
                  <div v-for="(opt, oi) in blank.options" :key="oi" class="option-row">
                    <el-input v-model="blank.options[oi]" placeholder="输入候选词" size="small" />
                    <el-button link type="danger" size="small" @click="removeClozeOption(bi, oi)" :disabled="blank.options.length <= 2">删</el-button>
                  </div>
                  <el-button size="small" @click="addClozeOption(bi)">+ 添加选项</el-button>
                </div>
              </el-form-item>
              <el-form-item label="正确答案" label-width="80px">
                <el-select v-model="blank.correct" placeholder="选择正确答案" style="width: 100%">
                  <el-option v-for="opt in blank.options.filter(Boolean)" :key="opt" :label="opt" :value="opt" />
                </el-select>
              </el-form-item>
              <el-form-item label="解析" label-width="80px">
                <el-input v-model="blank.explanation" type="textarea" :rows="2" placeholder="请输入答案解析" />
              </el-form-item>
            </div>
          </template>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

interface McqOption { label: string; value: string }
interface McqQuestion {
  question: string
  options: McqOption[]
  correct: string
  explanation: string
  transcript?: string
}
interface ClozeBlank {
  blankIndex: number
  options: string[]
  correct: string
  explanation: string
}

const loading = ref(false)
const saving = ref(false)
const audioUploading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const filterType = ref('')
const tableData = ref<any[]>([])
const mcqQuestions = ref<McqQuestion[]>([])
const clozeBlanks = ref<ClozeBlank[]>([])

const form = reactive({
  title: '',
  moduleType: 'READING',
  content: '',
  difficulty: 'Medium',
  category: '',
  duration: '',
  audioUrl: '',
  status: 'Active'
})

const moduleLabel = (type: string) => ({
  READING: '阅读理解',
  LISTENING: '听力测试',
  CLOZE: '选词填空'
}[type] || type)

const createMcqQuestion = (): McqQuestion => ({
  question: '',
  options: [
    { label: '', value: 'A' },
    { label: '', value: 'B' },
    { label: '', value: 'C' },
    { label: '', value: 'D' }
  ],
  correct: 'A',
  explanation: '',
  transcript: ''
})

const createClozeBlank = (index: number): ClozeBlank => ({
  blankIndex: index,
  options: ['', ''],
  correct: '',
  explanation: ''
})

const addMcqQuestion = () => {
  mcqQuestions.value.push(createMcqQuestion())
}

const removeMcqQuestion = (index: number) => {
  mcqQuestions.value.splice(index, 1)
}

const addClozeBlank = () => {
  clozeBlanks.value.push(createClozeBlank(clozeBlanks.value.length + 1))
}

const removeClozeBlank = (index: number) => {
  clozeBlanks.value.splice(index, 1)
  clozeBlanks.value.forEach((b, i) => { b.blankIndex = i + 1 })
}

const addClozeOption = (blankIndex: number) => {
  clozeBlanks.value[blankIndex].options.push('')
}

const removeClozeOption = (blankIndex: number, optionIndex: number) => {
  const blank = clozeBlanks.value[blankIndex]
  const removed = blank.options[optionIndex]
  blank.options.splice(optionIndex, 1)
  if (blank.correct === removed) blank.correct = ''
}

const onModuleTypeChange = () => {
  mcqQuestions.value = [createMcqQuestion()]
  clozeBlanks.value = [createClozeBlank(1)]
}

const handleAudioUpload = async (file: any) => {
  if (!file?.raw) return
  audioUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await axios.post('/api/listening/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200) {
      form.audioUrl = res.data.data
      ElMessage.success('音频上传成功')
    } else {
      ElMessage.error(res.data.message || res.data.msg || '音频上传失败')
    }
  } catch {
    ElMessage.error('音频上传失败')
  } finally {
    audioUploading.value = false
  }
}

const parseMcqFromJson = (questionsJson: string, answersJson: string) => {
  const questions = JSON.parse(questionsJson)
  const answers = JSON.parse(answersJson)
  return questions.map((q: any, i: number) => ({
    question: q.question || '',
    options: (q.options || []).map((o: any) => ({
      label: o.label || '',
      value: o.value || 'A'
    })),
    correct: answers[i]?.correct || 'A',
    explanation: answers[i]?.explanation || '',
    transcript: answers[i]?.transcript || ''
  }))
}

const parseClozeFromJson = (questionsJson: string, answersJson: string) => {
  const questions = JSON.parse(questionsJson)
  const answers = JSON.parse(answersJson)
  return questions.map((q: any, i: number) => ({
    blankIndex: q.blankIndex ?? i + 1,
    options: [...(q.options || ['', ''])],
    correct: answers[i]?.correct || '',
    explanation: answers[i]?.explanation || ''
  }))
}

const buildMcqJson = () => {
  const questions = mcqQuestions.value.map(q => ({
    question: q.question,
    options: q.options.map(o => ({ label: o.label, value: o.value }))
  }))
  const answers = mcqQuestions.value.map(q => {
    const item: Record<string, string> = { correct: q.correct, explanation: q.explanation }
    if (form.moduleType === 'LISTENING' && q.transcript) item.transcript = q.transcript
    return item
  })
  return { questionsJson: JSON.stringify(questions), answersJson: JSON.stringify(answers) }
}

const buildClozeJson = () => {
  const questions = clozeBlanks.value.map(b => ({
    blankIndex: b.blankIndex,
    options: b.options.filter(Boolean)
  }))
  const answers = clozeBlanks.value.map(b => ({
    correct: b.correct,
    explanation: b.explanation
  }))
  return { questionsJson: JSON.stringify(questions), answersJson: JSON.stringify(answers) }
}

const validateForm = () => {
  if (!form.title.trim()) {
    ElMessage.warning('请填写题库名称')
    return false
  }
  if (form.moduleType === 'CLOZE') {
    if (!form.content.trim()) {
      ElMessage.warning('请填写填空文章')
      return false
    }
    for (const b of clozeBlanks.value) {
      const opts = b.options.filter(Boolean)
      if (opts.length < 2) {
        ElMessage.warning(`空格 (${b.blankIndex}) 至少需要 2 个备选项`)
        return false
      }
      if (!b.correct) {
        ElMessage.warning(`请选择空格 (${b.blankIndex}) 的正确答案`)
        return false
      }
    }
  } else {
    for (let i = 0; i < mcqQuestions.value.length; i++) {
      const q = mcqQuestions.value[i]
      if (!q.question.trim()) {
        ElMessage.warning(`请填写第 ${i + 1} 题的题干`)
        return false
      }
      if (q.options.some(o => !o.label.trim())) {
        ElMessage.warning(`请完善第 ${i + 1} 题的所有选项`)
        return false
      }
      if (!q.correct) {
        ElMessage.warning(`请选择第 ${i + 1} 题的正确答案`)
        return false
      }
    }
  }
  return true
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = filterType.value ? { moduleType: filterType.value } : {}
    const res = await axios.get('/api/admin/question-bank/list', { params })
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch {
    ElMessage.error('加载题库失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.moduleType = 'READING'
  form.content = ''
  form.difficulty = 'Medium'
  form.category = ''
  form.duration = ''
  form.audioUrl = ''
  form.status = 'Active'
  mcqQuestions.value = [createMcqQuestion()]
  clozeBlanks.value = [createClozeBlank(1)]
}

const openDialog = (row?: any) => {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.title = row.title
    form.moduleType = row.moduleType
    form.content = row.content || ''
    form.difficulty = row.difficulty || 'Medium'
    form.category = row.category || ''
    form.duration = row.duration || ''
    form.audioUrl = row.audioUrl || ''
    form.status = row.status
    const qJson = typeof row.questionsJson === 'string' ? row.questionsJson : JSON.stringify(row.questionsJson)
    const aJson = typeof row.answersJson === 'string' ? row.answersJson : JSON.stringify(row.answersJson)
    try {
      if (row.moduleType === 'CLOZE') {
        clozeBlanks.value = parseClozeFromJson(qJson, aJson)
        mcqQuestions.value = [createMcqQuestion()]
      } else {
        mcqQuestions.value = parseMcqFromJson(qJson, aJson)
        clozeBlanks.value = [createClozeBlank(1)]
      }
    } catch {
      mcqQuestions.value = [createMcqQuestion()]
      clozeBlanks.value = [createClozeBlank(1)]
    }
  } else {
    isEdit.value = false
    editId.value = null
    resetForm()
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!validateForm()) return

  const { questionsJson, answersJson } = form.moduleType === 'CLOZE'
    ? buildClozeJson()
    : buildMcqJson()

  saving.value = true
  try {
    const payload = {
      title: form.title,
      moduleType: form.moduleType,
      content: form.content,
      difficulty: form.moduleType === 'READING' ? form.difficulty : null,
      category: form.moduleType === 'LISTENING' ? form.category : null,
      duration: form.moduleType === 'LISTENING' ? form.duration : null,
      audioUrl: form.moduleType === 'LISTENING' ? form.audioUrl : null,
      questionsJson,
      answersJson,
      status: form.status
    }
    const res = isEdit.value
      ? await axios.put(`/api/admin/question-bank/${editId.value}`, payload)
      : await axios.post('/api/admin/question-bank', payload)
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row: any) => {
  const next = row.status === 'Active' ? 'Inactive' : 'Active'
  try {
    const res = await axios.put(`/api/admin/question-bank/${row.id}/status`, { status: next })
    if (res.data.code === 200) {
      ElMessage.success(next === 'Active' ? '已上线' : '已下线')
      fetchList()
    }
  } catch {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除题库「${row.title}」？`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await axios.delete(`/api/admin/question-bank/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchList()
      }
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>

<style scoped>
.test-management-container { padding: 10px; }
.audio-url-row { display: flex; gap: 10px; width: 100%; }
.audio-url-row .el-input { flex: 1; }
.audio-preview { width: 100%; margin-top: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
.edit-form { padding-right: 12px; }
.section-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.section-title { font-weight: 600; color: #303133; font-size: 15px; }
.question-card {
  background: #f9fafc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 16px 4px;
  margin-bottom: 14px;
}
.question-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.question-no { font-weight: 600; color: #409eff; }
.option-list { width: 100%; }
.option-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.option-row .el-input { flex: 1; }
</style>
