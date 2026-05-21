<template>
  <div class="vocab-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>词汇通关列表</span>
          <el-button v-if="currentRole !== 'User'" type="primary" icon="Plus" @click="handleAdd">新增词汇</el-button>
          <el-tag v-if="currentRole === 'Teacher'" type="warning" size="small" style="margin-left:8px;">教师权限</el-tag>
          <el-tag v-if="currentRole === 'Admin'" type="danger" size="small" style="margin-left:8px;">管理员权限</el-tag>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="word" label="单词" width="180">
          <template #default="{ row }">
            <span class="vocab-word">{{ row.word }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phonetic" label="音标" width="150" />
        <el-table-column prop="translation" label="释义" />
        <el-table-column prop="level" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" v-if="currentRole !== 'User'">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="currentRole === 'Admin'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增词汇" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="单词">
          <el-input v-model="form.word" />
        </el-form-item>
        <el-form-item label="释义">
          <el-input v-model="form.translation" type="textarea" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level" placeholder="请选择">
            <el-option label="CET-4" value="CET-4" />
            <el-option label="CET-6" value="CET-6" />
            <el-option label="IELTS" value="IELTS" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, inject, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const tableData = ref<any[]>([])

const form = reactive({ word: '', translation: '', level: 'CET-4' })

const fetchVocab = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/vocab/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载词汇列表')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchVocab()
})

const getTagType = (level: string) => {
  if (level === 'CET-4') return 'info'
  if (level === 'CET-6') return 'warning'
  return 'danger'
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.word = ''
  form.translation = ''
  form.level = 'CET-4'
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  editId.value = row.id
  form.word = row.word
  form.translation = row.translation
  form.level = row.level
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.word || !form.translation) {
    ElMessage.warning('请填写完整内容')
    return
  }
  
  try {
    if (isEdit.value) {
      // In a real app, you'd call a PUT/POST /api/vocab/update
      // For now, let's just use add or keep it local as per controller availability
      await axios.post('/api/vocab/add', { ...form, id: editId.value })
      ElMessage.success('更新成功')
    } else {
      const res = await axios.post('/api/vocab/add', form)
      if (res.data.code === 200) {
        ElMessage.success('添加成功')
      }
    }
    fetchVocab()
  } catch (error) {
    ElMessage.error('操作失败')
  }
  dialogVisible.value = false
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除单词 "${row.word}" 吗？该操作不可撤销。`,
    '删除警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    }
  ).then(async () => {
    try {
      await axios.delete(`/api/vocab/delete/${row.id}`)
      ElMessage.success('已成功删除词汇')
      fetchVocab()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除操作')
  })
}
</script>

<style scoped>
.vocab-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.vocab-word { font-weight: bold; color: #409eff; }
</style>
