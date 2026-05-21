<template>
  <div class="grammar-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>语法解析库</span>
          <el-button v-if="currentRole !== 'User'" type="success" icon="Edit" @click="handleAdd">提交新解析</el-button>
          <el-tag v-if="currentRole === 'Teacher'" type="warning" size="small" style="margin-left:8px;">教师权限</el-tag>
          <el-tag v-if="currentRole === 'Admin'" type="danger" size="small" style="margin-left:8px;">管理员权限</el-tag>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

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
  ElMessage.success(`正在跳转至【${item.title}】的测试题库...`)
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
.grammar-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.grammar-content { padding: 15px; background: #fafafa; border-radius: 8px; line-height: 1.8; color: #444; }
</style>
