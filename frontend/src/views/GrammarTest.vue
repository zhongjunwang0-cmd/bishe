<template>
  <div class="grammar-test-container">
    <T5ModelBadge variant="banner" module="grammar" />

    <el-card shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="title-block">
            <el-button link type="primary" icon="ArrowLeft" @click="goBack">返回语法库</el-button>
            <span class="test-title">{{ grammar?.title || '语法测试' }}</span>
            <el-tag v-if="grammar?.category" size="small">{{ grammar.category }}</el-tag>
            <T5ModelBadge variant="tag" />
          </div>
        </div>
      </template>

      <el-alert
        v-if="grammar"
        :title="`共 ${questions.length} 道选择题，请根据【${grammar.title}】选择最佳答案。`"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-empty v-if="!loading && !questions.length" description="暂无测试题目" />

      <div v-else class="questions-box">
        <div v-for="(q, index) in questions" :key="index" class="question-item">
          <p><strong>{{ index + 1 }}. {{ q.question }}</strong></p>
          <el-radio-group v-model="answers[index]" class="options-group">
            <el-radio v-for="opt in q.options" :key="opt.value" :label="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <div v-if="questions.length" class="submit-row">
        <el-button type="primary" :loading="submitting" @click="submitTest">提交答案</el-button>
      </div>

      <el-divider v-if="questions.length">造句练习 · T5 语法纠错</el-divider>
      <div v-if="questions.length" class="sentence-practice">
        <p class="practice-hint">用本语法点造一个英文句子，提交后由 T5-GEC 模型纠错：</p>
        <el-input v-model="practiceSentence" type="textarea" :rows="2" placeholder="例如：If I were you, I would study harder." />
        <div class="practice-actions">
          <el-button type="warning" :loading="practiceLoading" @click="checkSentence">提交造句纠错</el-button>
        </div>
        <div v-if="practiceResult" class="practice-result">
          <p><strong>修改后：</strong>{{ practiceResult.corrected }}</p>
          <ul v-if="practiceResult.issues?.length">
            <li v-for="(issue, idx) in practiceResult.issues" :key="idx">{{ issue.message }}</li>
          </ul>
          <el-tag size="small">{{ practiceResult.source }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="resultVisible" :title="`${grammar?.title || ''} - 测试结果`" width="560px">
      <el-result
        :icon="score >= 60 ? 'success' : 'warning'"
        :title="`得分：${score} 分`"
        :sub-title="score >= 60 ? '继续加油，语法掌握不错！' : '建议返回语法库复习后再试。'"
      />
      <div v-for="(detail, index) in resultDetails" :key="index" class="key-item">
        <p><strong>第 {{ index + 1 }} 题</strong></p>
        <p>
          <span class="label">你的答案:</span>
          <el-tag :type="detail.userAnswer === detail.correct ? 'success' : 'danger'" size="small">
            {{ detail.userAnswer || '未作答' }}
          </el-tag>
        </p>
        <p><span class="label">标准答案:</span> <el-tag type="success" size="small">{{ detail.correct }}</el-tag></p>
        <p><span class="label">解析:</span> <span class="explanation">{{ detail.explanation }}</span></p>
        <el-divider />
      </div>
      <template #footer>
        <el-button @click="goBack">返回语法库</el-button>
        <el-button type="primary" @click="retryTest">再测一次</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import T5ModelBadge from '../components/T5ModelBadge.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const resultVisible = ref(false)
const grammar = ref<any>(null)
const questions = ref<any[]>([])
const answers = ref<string[]>([])
const score = ref(0)
const resultDetails = ref<any[]>([])
const practiceSentence = ref('')
const practiceLoading = ref(false)
const practiceResult = ref<any>(null)

const grammarId = () => Number(route.query.id)

const fetchTest = async () => {
  const id = grammarId()
  if (!id) {
    ElMessage.error('缺少语法知识点 ID')
    router.replace('/grammar')
    return
  }
  loading.value = true
  try {
    const res = await axios.get(`/api/grammar/${id}/test`)
    if (res.data.code === 200) {
      grammar.value = res.data.data.grammar
      questions.value = res.data.data.questions || []
      answers.value = Array(questions.value.length).fill('')
    } else {
      ElMessage.error(res.data.message || '加载测试失败')
      router.replace('/grammar')
    }
  } catch {
    ElMessage.error('无法加载语法测试')
    router.replace('/grammar')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTest)

const goBack = () => router.push('/grammar')

const retryTest = () => {
  resultVisible.value = false
  answers.value = Array(questions.value.length).fill('')
  score.value = 0
  resultDetails.value = []
  practiceSentence.value = ''
  practiceResult.value = null
}

const checkSentence = async () => {
  if (!practiceSentence.value.trim()) {
    ElMessage.warning('请先输入造句')
    return
  }
  practiceLoading.value = true
  practiceResult.value = null
  try {
    const res = await axios.post('/api/grammar/correct', { text: practiceSentence.value.trim() })
    if (res.data.code === 200) {
      practiceResult.value = res.data.data
      ElMessage.success('T5 语法纠错完成')
    } else {
      ElMessage.error(res.data.message || '纠错失败')
    }
  } catch {
    ElMessage.error('语法纠错服务不可用')
  } finally {
    practiceLoading.value = false
  }
}

const submitTest = async () => {
  if (answers.value.some(a => !a)) {
    ElMessage.warning('请回答所有问题后再提交')
    return
  }
  submitting.value = true
  try {
    const res = await axios.post(`/api/grammar/${grammarId()}/test/submit`, { answers: answers.value })
    if (res.data.code === 200) {
      score.value = res.data.data.score
      resultDetails.value = res.data.data.details || []
      resultVisible.value = true
      ElMessage.success(`提交成功！得分 ${score.value} 分`)
    } else {
      ElMessage.error(res.data.message || '提交失败')
    }
  } catch {
    ElMessage.error('网络错误，无法提交答案')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.grammar-test-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title-block { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.test-title { font-size: 16px; font-weight: 600; }
.questions-box { padding: 10px 0; }
.question-item { margin-bottom: 25px; padding-bottom: 15px; border-bottom: 1px dashed #ebeef5; }
.options-group { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; margin-top: 15px; padding-left: 10px; }
.options-group .el-radio { margin-right: 0; white-space: normal; display: flex; align-items: flex-start; }
.options-group :deep(.el-radio__label) { text-align: left; line-height: 1.5; padding-left: 10px; }
.submit-row { margin-top: 10px; text-align: right; }
.sentence-practice { margin-top: 8px; padding: 16px; background: #fafafa; border-radius: 8px; }
.practice-hint { color: #606266; margin: 0 0 10px; font-size: 14px; }
.practice-actions { margin-top: 10px; text-align: right; }
.practice-result { margin-top: 12px; line-height: 1.7; }
.practice-result ul { margin: 8px 0; padding-left: 20px; }
.key-item { margin-bottom: 8px; }
.label { font-weight: bold; color: #606266; margin-right: 8px; }
.explanation { color: #666; line-height: 1.6; }
</style>
