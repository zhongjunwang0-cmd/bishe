<template>
  <div class="cloze-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>选词填空</span>
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

    <!-- Test Dialog -->
    <el-dialog v-model="testDialogVisible" :title="currentTest?.title || '填空测试'" width="60%" top="5vh">
      <div v-if="currentTest" class="test-content">
        <el-alert title="请通读全文并为每个空格选择最恰当的词汇。" type="info" show-icon :closable="false" style="margin-bottom: 15px;" />
        
        <div class="passage-box">
          <p class="passage-text">Learning a foreign language <span class="blank-slot">(1) <el-select v-model="answers[0]" placeholder="选择" size="small" style="width: 100px;"><el-option v-for="opt in mockQuestions[0].options" :key="opt" :label="opt" :value="opt"/></el-select></span> a lot of time and practice. However, it is very rewarding. You can communicate with people from other <span class="blank-slot">(2) <el-select v-model="answers[1]" placeholder="选择" size="small" style="width: 100px;"><el-option v-for="opt in mockQuestions[1].options" :key="opt" :label="opt" :value="opt"/></el-select></span> and understand their culture better.</p>
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
        <el-alert title="以下为标准答案及解析。" type="success" show-icon :closable="false" style="margin-bottom: 20px;" />
        
        <div class="passage-box" style="margin-bottom: 20px;">
          <p class="passage-text">Learning a foreign language <strong>takes</strong> a lot of time and practice. However, it is very rewarding. You can communicate with people from other <strong>countries</strong> and understand their culture better.</p>
        </div>

        <div v-for="(q, index) in mockQuestions" :key="index" class="key-item">
          <p><strong>空格 ({{ index + 1 }}):</strong></p>
          <p><span class="label">标准答案:</span> <el-tag type="success">{{ q.correct }}</el-tag></p>
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
import axios from 'axios'

const tableData = ref<any[]>([])
const loading = ref(false)

const testDialogVisible = ref(false)
const keyDialogVisible = ref(false)
const currentTest = ref<any>(null)
const answers = ref<string[]>([])
const submitting = ref(false)

const mockQuestions = [
  {
    options: ["takes", "spends", "costs", "pays"],
    correct: "takes",
    explanation: "'take time' 意为‘花费时间’，主语通常为物或动名词（Learning），符合语境。"
  },
  {
    options: ["cities", "countries", "villages", "towns"],
    correct: "countries",
    explanation: "结合下文的 'foreign language' 和 'understand their culture'，与外国人交流最恰当的词是 'countries'（国家）。"
  }
]

const fetchClozes = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/cloze/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载选词填空列表')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchClozes()
})

const handleNewCloze = async () => {
  ElMessage.success('正在为您生成新的选词填空题目...')
  try {
    const res = await axios.post('/api/cloze/generate')
    if (res.data.code === 200) {
      ElMessage.success('生成成功！')
      fetchClozes()
    } else {
      ElMessage.error(res.data.msg || '生成失败')
    }
  } catch (error) {
    // 隐藏无法连接报错，axios 拦截器如果返回401会自动跳登录
  }
}

const handleStart = (row: any) => {
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
    ElMessage.warning('请完成所有填空后再提交')
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
    const res = await axios.put(`/api/cloze/${currentTest.value.id}/score`, { score: calculatedScore })
    if (res.data.code === 200) {
      ElMessage.success(`提交成功！您的得分是: ${calculatedScore} 分`)
      testDialogVisible.value = false
      fetchClozes()
      
      // 添加学习记录
      try {
        await axios.post('/api/record/add', { type: '选词填空', targetId: currentTest.value.id, duration: 300 })
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
.cloze-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.passage-box {
  background-color: #f9fafc;
  padding: 25px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  line-height: 2.2;
}
.passage-text {
  font-size: 16px;
  color: #303133;
}
.blank-slot {
  display: inline-block;
  margin: 0 8px;
  font-weight: bold;
  color: #409EFF;
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
