<template>

  <div class="listening-container">

    <el-card shadow="hover">

      <template #header>

        <div class="card-header">

          <span>听力训练</span>

          <div class="header-actions">

            <el-button v-if="currentRole !== 'User'" type="success" icon="Upload" @click="openUploadDialog">

              上传听力材料

            </el-button>

            <el-button type="primary" icon="Headset" @click="handleNewListen">开始新听力</el-button>

          </div>

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

        <el-table-column label="操作" :width="currentRole !== 'User' ? 340 : 220" fixed="right">

          <template #default="{ row }">

            <el-button link type="primary" @click="handlePlay(row)">

              <el-icon><VideoPlay /></el-icon>播放

            </el-button>

            <el-button link type="primary" @click="handleDoTest(row)">做题</el-button>

            <el-button link type="success" @click="handleViewKey(row)">查看解析</el-button>

            <el-button v-if="currentRole !== 'User'" link type="warning" @click="openReplaceDialog(row)">替换音频</el-button>

            <el-button v-if="currentRole !== 'User'" link type="danger" @click="handleDelete(row)">删除</el-button>

          </template>

        </el-table-column>

      </el-table>

    </el-card>



    <el-dialog v-model="playDialogVisible" :title="currentTest?.title || '听力播放'" width="40%" top="15vh" destroy-on-close @closed="stopPlayAudio">

      <div v-if="currentTest" class="audio-content">

        <el-alert title="请仔细听录音，您可以反复播放。" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />

        <audio v-if="currentTest.audioUrl" ref="playAudioRef" controls :src="currentTest.audioUrl" style="width: 100%;"></audio>

        <el-empty v-else description="暂无音频，请查看原文" />

        <p v-if="currentTest.content" class="transcript">{{ currentTest.content }}</p>

      </div>

      <template #footer>

        <el-button type="primary" @click="closePlayDialog">关闭</el-button>

      </template>

    </el-dialog>



    <el-dialog v-model="testDialogVisible" :title="currentTest?.title || '听力测试'" width="60%" top="5vh" destroy-on-close @closed="stopTestAudio">

      <div v-if="currentTest" class="test-content" v-loading="detailLoading">

        <el-alert title="注意：您可以边听边作答。" type="warning" show-icon :closable="false" style="margin-bottom: 15px;" />

        <audio v-if="currentTest.audioUrl" ref="testAudioRef" controls :src="currentTest.audioUrl" style="width: 100%; margin-bottom: 20px;"></audio>

        <div class="questions-box" v-if="questions.length">

          <h3>听力题目</h3>

          <div v-for="(q, index) in questions" :key="index" class="question-item">

            <p><strong>{{ index + 1 }}. {{ q.question }}</strong></p>

            <el-radio-group v-model="answers[index]" class="options-group">

              <el-radio v-for="(opt, oIndex) in q.options" :key="oIndex" :label="opt.value">

                {{ opt.label }}

              </el-radio>

            </el-radio-group>

          </div>

        </div>

        <el-empty v-else description="该测试暂无结构化题目，请使用「开始新听力」从题库生成" />

      </div>

      <template #footer>

        <el-button @click="closeTestDialog">取消</el-button>

        <el-button type="primary" @click="submitTest" :loading="submitting" :disabled="!questions.length">提交答案</el-button>

      </template>

    </el-dialog>



    <el-dialog v-model="keyDialogVisible" :title="(currentTest?.title || '') + ' - 答案解析'" width="50%" top="5vh">

      <div v-if="currentTest" class="key-content" v-loading="detailLoading">

        <el-alert title="注意" type="info" description="以下为标准答案及解析。" show-icon :closable="false" style="margin-bottom: 20px;" />

        <div v-for="(q, index) in questions" :key="index" class="key-item">

          <p><strong>题目 {{ index + 1 }}:</strong> {{ q.question }}</p>

          <p><span class="label">标准答案:</span> <el-tag type="success">{{ answerKeys[index]?.correct }}</el-tag></p>

          <p v-if="answerKeys[index]?.transcript"><span class="label">听力原文片段:</span> <span class="explanation">{{ answerKeys[index].transcript }}</span></p>

          <p><span class="label">解析:</span> <span class="explanation">{{ answerKeys[index]?.explanation }}</span></p>

          <el-divider />

        </div>

      </div>

      <template #footer>

        <el-button type="primary" @click="keyDialogVisible = false">关闭</el-button>

      </template>

    </el-dialog>



    <el-dialog v-model="uploadDialogVisible" title="上传听力材料" width="780px" top="4vh" destroy-on-close>

      <el-scrollbar max-height="68vh">

        <el-form :model="uploadForm" label-width="100px" class="upload-form">

          <el-form-item label="材料名称">

            <el-input v-model="uploadForm.title" placeholder="请输入听力材料名称" />

          </el-form-item>

          <el-form-item label="分类">

            <el-select v-model="uploadForm.category" filterable allow-create default-first-option placeholder="选择或输入分类" style="width: 100%">

              <el-option label="日常口语" value="日常口语" />

              <el-option label="IELTS Mock" value="IELTS Mock" />

              <el-option label="TOEFL模拟" value="TOEFL模拟" />

              <el-option label="TED Talk" value="TED Talk" />

              <el-option label="BBC新闻" value="BBC新闻" />

              <el-option label="Conversation" value="Conversation" />

              <el-option label="Lecture" value="Lecture" />

            </el-select>

          </el-form-item>

          <el-form-item label="音频文件">

            <div class="audio-url-row">

              <el-input v-model="uploadForm.audioUrl" placeholder="上传音频后自动填充地址" readonly />

              <el-upload

                action="#"

                :auto-upload="false"

                :show-file-list="false"

                accept=".mp3,.wav,.m4a,.ogg,audio/*"

                :on-change="handleAudioUpload"

              >

                <el-button type="primary" :loading="audioUploading">选择并上传</el-button>

              </el-upload>

            </div>

            <audio v-if="uploadForm.audioUrl" controls :src="uploadForm.audioUrl" class="audio-preview" />

          </el-form-item>

          <el-form-item label="时长">

            <el-input v-model="uploadForm.duration" placeholder="上传音频后自动识别，也可手动填写如 04:15" />

          </el-form-item>

          <el-form-item label="听力原文">

            <el-input v-model="uploadForm.content" type="textarea" :rows="4" placeholder="请输入听力 transcript 原文" />

          </el-form-item>



          <div class="section-bar">

            <span class="section-title">题目设置（至少 2 道）</span>

            <el-button type="primary" link icon="Plus" @click="addMcqQuestion">添加题目</el-button>

          </div>

          <el-empty v-if="!mcqQuestions.length" description="暂无题目，请点击添加" :image-size="60" />

          <div v-for="(q, qi) in mcqQuestions" :key="qi" class="question-card">

            <div class="question-card-header">

              <span class="question-no">第 {{ qi + 1 }} 题</span>

              <el-button link type="danger" size="small" @click="removeMcqQuestion(qi)" :disabled="mcqQuestions.length <= 2">删除</el-button>

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

            <el-form-item label="原文片段" label-width="80px">

              <el-input v-model="q.transcript" type="textarea" :rows="2" placeholder="对应听力原文片段（选填）" />

            </el-form-item>

          </div>

        </el-form>

      </el-scrollbar>

      <template #footer>

        <el-button @click="uploadDialogVisible = false">取消</el-button>

        <el-button type="primary" @click="submitUpload" :loading="uploadSaving">保存并发布</el-button>

      </template>

    </el-dialog>



    <el-dialog v-model="replaceDialogVisible" title="替换音频" width="560px" top="12vh" destroy-on-close>

      <div v-if="replaceTarget" class="replace-audio-content">

        <el-alert

          title="上传新音频后将替换当前听力材料的音频文件，题目与解析保持不变。"

          type="info"

          show-icon

          :closable="false"

          style="margin-bottom: 16px;"

        />

        <p class="replace-title"><strong>材料：</strong>{{ replaceTarget.title }}</p>

        <p v-if="replaceTarget.duration" class="replace-meta"><strong>当前时长：</strong>{{ replaceTarget.duration }}</p>

        <div v-if="replaceTarget.audioUrl" class="replace-current-audio">

          <p class="replace-meta"><strong>当前音频：</strong></p>

          <audio controls :src="replaceTarget.audioUrl" class="audio-preview" />

        </div>

        <el-divider />

        <el-form label-width="100px">

          <el-form-item label="新音频文件">

            <div class="audio-url-row">

              <el-input v-model="replaceForm.audioUrl" placeholder="上传新音频后自动填充地址" readonly />

              <el-upload

                action="#"

                :auto-upload="false"

                :show-file-list="false"

                accept=".mp3,.wav,.m4a,.ogg,audio/*"

                :on-change="handleReplaceAudioUpload"

              >

                <el-button type="primary" :loading="replaceAudioUploading">选择并上传</el-button>

              </el-upload>

            </div>

            <audio v-if="replaceForm.audioUrl" controls :src="replaceForm.audioUrl" class="audio-preview" />

          </el-form-item>

          <el-form-item label="时长">

            <el-input v-model="replaceForm.duration" placeholder="上传音频后自动识别，也可手动填写如 04:15" />

          </el-form-item>

        </el-form>

      </div>

      <template #footer>

        <el-button @click="replaceDialogVisible = false">取消</el-button>

        <el-button type="primary" @click="submitReplaceAudio" :loading="replaceSaving">确认替换</el-button>

      </template>

    </el-dialog>

  </div>

</template>



<script setup lang="ts">

import { ref, reactive, inject, onMounted, onBeforeUnmount, watch } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { VideoPlay } from '@element-plus/icons-vue'

import axios from 'axios'



interface McqOption { label: string; value: string }

interface McqQuestion {

  question: string

  options: McqOption[]

  correct: string

  explanation: string

  transcript?: string

}



const currentRole = inject<any>('currentRole', ref('User'))

const tableData = ref<any[]>([])

const loading = ref(false)

const detailLoading = ref(false)

const playDialogVisible = ref(false)

const testDialogVisible = ref(false)

const playAudioRef = ref<HTMLAudioElement | null>(null)

const testAudioRef = ref<HTMLAudioElement | null>(null)

const keyDialogVisible = ref(false)

const uploadDialogVisible = ref(false)

const replaceDialogVisible = ref(false)

const audioUploading = ref(false)

const replaceAudioUploading = ref(false)

const uploadSaving = ref(false)

const replaceSaving = ref(false)

const currentTest = ref<any>(null)

const replaceTarget = ref<any>(null)

const questions = ref<any[]>([])

const answerKeys = ref<any[]>([])

const answers = ref<string[]>([])

const submitting = ref(false)

const mcqQuestions = ref<McqQuestion[]>([])



const uploadForm = reactive({

  title: '',

  category: '',

  duration: '',

  audioUrl: '',

  content: ''

})



const replaceForm = reactive({

  audioUrl: '',

  duration: ''

})



const stopAudio = (audioEl: HTMLAudioElement | null | undefined) => {

  if (!audioEl) return

  audioEl.pause()

  audioEl.currentTime = 0

}



const stopPlayAudio = () => stopAudio(playAudioRef.value)



const stopTestAudio = () => stopAudio(testAudioRef.value)



const stopAllListeningAudio = () => {

  stopPlayAudio()

  stopTestAudio()

}



const closePlayDialog = () => {

  stopPlayAudio()

  playDialogVisible.value = false

}



const closeTestDialog = () => {

  stopTestAudio()

  testDialogVisible.value = false

}



watch(playDialogVisible, (visible) => {

  if (!visible) stopPlayAudio()

})



watch(testDialogVisible, (visible) => {

  if (!visible) stopTestAudio()

})



onBeforeUnmount(stopAllListeningAudio)



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



const addMcqQuestion = () => {

  mcqQuestions.value.push(createMcqQuestion())

}



const removeMcqQuestion = (index: number) => {

  mcqQuestions.value.splice(index, 1)

}



const formatDuration = (seconds: number) => {

  const total = Math.floor(seconds)

  const mm = String(Math.floor(total / 60)).padStart(2, '0')

  const ss = String(total % 60).padStart(2, '0')

  return `${mm}:${ss}`

}



const detectAudioDuration = (file: File): Promise<string> => {

  return new Promise((resolve) => {

    const audio = new Audio()

    audio.preload = 'metadata'

    audio.onloadedmetadata = () => {

      resolve(Number.isFinite(audio.duration) ? formatDuration(audio.duration) : '')

      URL.revokeObjectURL(audio.src)

    }

    audio.onerror = () => {

      URL.revokeObjectURL(audio.src)

      resolve('')

    }

    audio.src = URL.createObjectURL(file)

  })

}



const resetUploadForm = () => {

  uploadForm.title = ''

  uploadForm.category = ''

  uploadForm.duration = ''

  uploadForm.audioUrl = ''

  uploadForm.content = ''

  mcqQuestions.value = [createMcqQuestion(), createMcqQuestion()]

}



const openUploadDialog = () => {

  resetUploadForm()

  uploadDialogVisible.value = true

}



const handleAudioUpload = async (file: any) => {

  if (!file?.raw) return

  audioUploading.value = true

  try {

    const duration = await detectAudioDuration(file.raw)

    if (duration) uploadForm.duration = duration



    const audioUrl = await uploadAudioFile(file.raw)

    if (audioUrl) {

      uploadForm.audioUrl = audioUrl

      ElMessage.success('音频上传成功')

    }

  } catch {

    ElMessage.error('音频上传失败')

  } finally {

    audioUploading.value = false

  }

}



const uploadAudioFile = async (rawFile: File): Promise<string | null> => {

  const maxSize = 10 * 1024 * 1024

  if (rawFile.size > maxSize) {

    ElMessage.error('音频文件不能超过 10MB')

    return null

  }



  try {

    const formData = new FormData()

    formData.append('file', rawFile)

    const res = await axios.post('/api/listening/upload', formData, {

      headers: { 'Content-Type': 'multipart/form-data' }

    })

    if (res.data.code === 200) {

      return res.data.data

    }

    ElMessage.error(res.data.message || res.data.msg || '音频上传失败')

    return null

  } catch (err: any) {

    if (err.response?.status === 413) {

      ElMessage.error('音频文件过大，请上传不超过 10MB 的文件')

    } else {

      ElMessage.error(err.response?.data?.message || err.response?.data?.msg || '音频上传失败')

    }

    return null

  }

}



const resetReplaceForm = () => {

  replaceForm.audioUrl = ''

  replaceForm.duration = ''

}



const openReplaceDialog = (row: any) => {

  replaceTarget.value = row

  resetReplaceForm()

  replaceDialogVisible.value = true

}



const handleReplaceAudioUpload = async (file: any) => {

  if (!file?.raw) return

  replaceAudioUploading.value = true

  try {

    const duration = await detectAudioDuration(file.raw)

    if (duration) replaceForm.duration = duration



    const audioUrl = await uploadAudioFile(file.raw)

    if (audioUrl) {

      replaceForm.audioUrl = audioUrl

      ElMessage.success('新音频上传成功')

    }

  } catch {

    ElMessage.error('音频上传失败')

  } finally {

    replaceAudioUploading.value = false

  }

}



const submitReplaceAudio = async () => {

  if (!replaceTarget.value) return

  if (!replaceForm.audioUrl.trim()) {

    ElMessage.warning('请先上传新的音频文件')

    return

  }



  replaceSaving.value = true

  try {

    const res = await axios.put(`/api/listening/${replaceTarget.value.id}/audio`, {

      audioUrl: replaceForm.audioUrl,

      duration: replaceForm.duration || null

    })

    if (res.data.code === 200) {

      ElMessage.success('音频替换成功')

      replaceDialogVisible.value = false

      fetchListening()

    } else {

      ElMessage.error(res.data.message || res.data.msg || '音频替换失败')

    }

  } catch {

    ElMessage.error('音频替换失败，请检查网络或权限')

  } finally {

    replaceSaving.value = false

  }

}



const buildMcqJson = () => {

  const questionsPayload = mcqQuestions.value.map(q => ({

    question: q.question,

    options: q.options.map(o => ({ label: o.label, value: o.value }))

  }))

  const answersPayload = mcqQuestions.value.map(q => {

    const item: Record<string, string> = { correct: q.correct, explanation: q.explanation }

    if (q.transcript) item.transcript = q.transcript

    return item

  })

  return {

    questionsJson: JSON.stringify(questionsPayload),

    answersJson: JSON.stringify(answersPayload)

  }

}



const validateUploadForm = () => {

  if (!uploadForm.title.trim()) {

    ElMessage.warning('请填写材料名称')

    return false

  }

  if (!uploadForm.category.trim()) {

    ElMessage.warning('请选择或填写分类')

    return false

  }

  if (!uploadForm.audioUrl.trim()) {

    ElMessage.warning('请上传音频文件')

    return false

  }

  if (mcqQuestions.value.length < 2) {

    ElMessage.warning('请至少添加 2 道选择题')

    return false

  }

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

  return true

}



const submitUpload = async () => {

  if (!validateUploadForm()) return



  const { questionsJson, answersJson } = buildMcqJson()

  uploadSaving.value = true

  try {

    const payload = {

      title: uploadForm.title,

      moduleType: 'LISTENING',

      content: uploadForm.content,

      category: uploadForm.category,

      duration: uploadForm.duration || null,

      audioUrl: uploadForm.audioUrl,

      questionsJson,

      answersJson,

      status: 'Active'

    }

    const createRes = await axios.post('/api/admin/question-bank', payload)

    if (createRes.data.code !== 200) {

      ElMessage.error(createRes.data.message || '保存题库失败')

      return

    }

    const bankId = createRes.data.data

    const publishRes = await axios.post(`/api/listening/publish/${bankId}`)

    if (publishRes.data.code === 200) {

      ElMessage.success('听力材料已上传并发布')

      uploadDialogVisible.value = false

      fetchListening()

    } else {

      ElMessage.error(publishRes.data.message || '发布到听力列表失败')

    }

  } catch {

    ElMessage.error('上传失败，请检查网络或权限')

  } finally {

    uploadSaving.value = false

  }

}



const fetchListening = async () => {

  loading.value = true

  try {

    const res = await axios.get('/api/listening/list')

    if (res.data.code === 200) {

      tableData.value = res.data.data

    }

  } catch {

    ElMessage.error('无法加载听力列表')

  } finally {

    loading.value = false

  }

}



const loadDetail = async (id: number, withAnswers = false) => {

  detailLoading.value = true

  questions.value = []

  answerKeys.value = []

  try {

    const res = await axios.get(`/api/listening/${id}`)

    if (res.data.code === 200) {

      currentTest.value = res.data.data

      questions.value = res.data.data.questions || []

    }

    if (withAnswers) {

      const ansRes = await axios.get(`/api/listening/${id}/answers`)

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



onMounted(fetchListening)



const handleNewListen = async () => {

  try {

    const res = await axios.post('/api/listening/generate')

    if (res.data.code === 200) {

      ElMessage.success('已从题库随机生成听力训练')

      fetchListening()

    } else {

      ElMessage.error(res.data.message || '生成失败')

    }

  } catch {}

}



const handlePlay = async (row: any) => {

  closeTestDialog()

  playDialogVisible.value = true

  await loadDetail(row.id)

}



const handleDoTest = async (row: any) => {

  closePlayDialog()

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



const handleDelete = (row: any) => {

  ElMessageBox.confirm(`确定删除听力材料「${row.title}」？`, '提示', { type: 'warning' })

    .then(async () => {

      const res = await axios.delete(`/api/listening/${row.id}`)

      if (res.data.code === 200) {

        ElMessage.success('删除成功')

        fetchListening()

      } else {

        ElMessage.error(res.data.message || '删除失败')

      }

    })

    .catch(() => {})

}



const submitTest = async () => {

  if (answers.value.some(a => !a)) {

    ElMessage.warning('请回答所有问题后再提交')

    return

  }

  submitting.value = true

  try {

    const res = await axios.post(`/api/listening/${currentTest.value.id}/submit`, { answers: answers.value })

    if (res.data.code === 200) {

      const score = res.data.data.score

      ElMessage.success(`提交成功！您的得分是: ${score} 分`)

      closeTestDialog()

      fetchListening()

      try {

        await axios.post('/api/record/add', { type: '听力训练', targetId: currentTest.value.id, duration: 300, score })

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

.listening-container { padding: 10px; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

.header-actions { display: flex; gap: 10px; align-items: center; }

.audio-content { display: flex; flex-direction: column; }

.transcript { margin-top: 16px; line-height: 1.8; color: #606266; white-space: pre-wrap; }

.test-content { display: flex; flex-direction: column; }

.questions-box { padding: 10px 0; }

.question-item { margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px dashed #ebeef5; }

.options-group { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; margin-top: 15px; padding-left: 10px; }

.options-group .el-radio { margin-right: 0; white-space: normal; display: flex; align-items: flex-start; }

.options-group :deep(.el-radio__label) { text-align: left; line-height: 1.5; padding-left: 10px; }

.key-item { margin-bottom: 15px; }

.label { font-weight: bold; color: #606266; margin-right: 8px; }

.explanation { color: #666; line-height: 1.6; }

.upload-form { padding-right: 12px; }

.audio-url-row { display: flex; gap: 10px; width: 100%; }

.audio-url-row .el-input { flex: 1; }

.audio-preview { width: 100%; margin-top: 10px; }

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

.replace-audio-content { padding-right: 8px; }

.replace-title { margin: 0 0 8px; color: #303133; }

.replace-meta { margin: 0 0 8px; color: #606266; }

.replace-current-audio { margin-bottom: 8px; }

</style>

