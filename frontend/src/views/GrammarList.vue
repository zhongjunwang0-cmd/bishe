<template>
  <div class="grammar-container">
    <T5ModelBadge variant="banner" module="grammar" />

    <el-card shadow="hover" class="gec-card">
      <template #header>
        <div class="card-header">
          <span>T5 语法纠错（JFLEG 微调）</span>
          <el-tag type="warning" size="small">T5-GEC</el-tag>
        </div>
      </template>
      <p class="gec-hint">输入英文句子，系统使用 JFLEG 数据集微调的 T5 模型进行语法纠错。</p>
      <el-input
        v-model="gecInput"
        type="textarea"
        :rows="3"
        placeholder="例如：he like doges and i dont know why"
      />
      <div class="gec-actions">
        <el-button type="primary" :loading="gecLoading" @click="runGrammarCorrect">纠错</el-button>
      </div>
      <div v-if="gecResult" class="gec-result">
        <p><strong>修改后：</strong>{{ gecResult.corrected }}</p>
        <div v-if="gecResult.issues?.length">
          <p><strong>问题说明：</strong></p>
          <ul>
            <li v-for="(issue, idx) in gecResult.issues" :key="idx">
              [{{ issue.type }}] {{ issue.message }}
            </li>
          </ul>
        </div>
        <el-tag size="small" type="info">来源：{{ gecResult.source }}</el-tag>
      </div>
    </el-card>

    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>语法解析库</span>
          <div class="header-tags">
            <T5ModelBadge variant="tag" />
            <el-button v-if="currentRole !== 'User'" type="success" icon="Edit" @click="handleAdd">提交新解析</el-button>
            <el-tag v-if="currentRole === 'Teacher'" type="warning" size="small">教师权限</el-tag>
            <el-tag v-if="currentRole === 'Admin'" type="danger" size="small">管理员权限</el-tag>
          </div>
        </div>
      </template>
      <el-collapse v-model="activeNames">
        <el-collapse-item v-for="item in grammarList" :key="item.id" :title="item.title" :name="item.id">
          <div class="grammar-content">
            <el-tag size="small" style="margin-bottom: 10px;">{{ item.category }}</el-tag>
            <p>{{ item.content }}</p>
            <div style="margin-top: 15px; text-align: right;">
              <el-button type="primary" size="small" @click="handleGrammarTest(item)">去测试此语法</el-button>
              <el-button v-if="currentRole === 'Admin'" type="danger" size="small" @click="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-dialog v-model="dialogVisible" title="提交新解析" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="语法标题">
          <el-input v-model="form.title" placeholder="例如：虚拟语气 (Subjunctive Mood)" />
        </el-form-item>
        <el-form-item label="语法分类">
          <el-input v-model="form.category" placeholder="例如：句型结构" />
        </el-form-item>
        <el-form-item label="解析内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="输入语法解析内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定并提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import T5ModelBadge from '../components/T5ModelBadge.vue'

const router = useRouter()

const currentRole = inject<any>('currentRole', ref('User'))
const activeNames = ref<any[]>([])
const dialogVisible = ref(false)
const loading = ref(false)

const form = reactive({
  title: '',
  category: '',
  content: ''
})

const grammarList = ref<any[]>([])
const gecInput = ref('')
const gecLoading = ref(false)
const gecResult = ref<any>(null)

const runGrammarCorrect = async () => {
  if (!gecInput.value.trim()) {
    ElMessage.warning('请输入英文句子')
    return
  }
  gecLoading.value = true
  gecResult.value = null
  try {
    const res = await axios.post('/api/grammar/correct', { text: gecInput.value.trim() })
    if (res.data.code === 200) {
      gecResult.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '纠错失败')
    }
  } catch {
    ElMessage.error('语法纠错服务不可用')
  } finally {
    gecLoading.value = false
  }
}

const fetchGrammar = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/grammar/list')
    if (res.data.code === 200) {
      grammarList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载语法列表')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchGrammar()
})

const handleAdd = () => {
  form.title = ''
  form.category = ''
  form.content = ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.title || !form.category || !form.content) {
    ElMessage.warning('请填写完整的解析信息')
    return
  }
  
  try {
    const res = await axios.post('/api/grammar/add', form)
    if (res.data.code === 200) {
      ElMessage.success('新解析提交成功！')
      fetchGrammar()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
  dialogVisible.value = false
}

const handleGrammarTest = (item: any) => {
  router.push({ path: '/grammar/test', query: { id: String(item.id) } })
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm(`确定要删除语法解析【${item.title}】吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/api/grammar/delete/${item.id}`)
      ElMessage.success('删除成功')
      fetchGrammar()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}
</script>

<style scoped>
.grammar-container { padding: 10px; display: flex; flex-direction: column; gap: 12px; }
.gec-card { margin-bottom: 0; }
.gec-hint { color: #606266; margin: 0 0 12px; font-size: 14px; }
.gec-actions { margin-top: 12px; text-align: right; }
.gec-result { margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 8px; line-height: 1.7; }
.gec-result ul { margin: 8px 0 12px 20px; padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.header-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.grammar-content { padding: 15px; background: #fafafa; border-radius: 8px; line-height: 1.8; color: #444; }
</style>
