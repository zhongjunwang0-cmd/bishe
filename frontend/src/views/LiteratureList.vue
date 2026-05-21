<template>
  <div class="literature-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文献阅读</span>
          <el-button v-if="currentRole !== 'User'" type="primary" icon="Plus" @click="handleAdd">上传新文献</el-button>
          <el-tag v-if="currentRole === 'Teacher'" type="warning" size="small" style="margin-left:8px;">教师权限</el-tag>
          <el-tag v-if="currentRole === 'Admin'" type="danger" size="small" style="margin-left:8px;">管理员权限</el-tag>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="title" label="文献标题" />
        <el-table-column prop="author" label="作者" width="180" />
        <el-table-column prop="date" label="上传日期" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handlePreview(row)">预览/阅读</el-button>
            <el-button v-if="currentRole === 'Admin'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="上传新文献" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="文献标题">
          <el-input v-model="form.title" placeholder="请输入文献标题" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="请输入作者名称" />
        </el-form-item>
        <el-form-item label="文献文件">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">点击上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF, Word 格式文件，单文件不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定并上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="文献预览" width="800px" top="5vh">
      <div v-if="currentPreview" class="preview-content">
        <h3 style="text-align: center; margin-bottom: 20px;">{{ currentPreview.title }}</h3>
        <p style="text-align: center; color: #666; margin-bottom: 10px;">作者: {{ currentPreview.author }}</p>
        <div class="pdf-viewer-container">
          <iframe 
            v-if="currentPreview.content" 
            :src="currentPreview.content" 
            width="100%" 
            height="600px" 
            frameborder="0"
          ></iframe>
          <div v-else class="no-content-tip">
            暂无文献内容预览
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, inject } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const dialogVisible = ref(false)
const previewVisible = ref(false)
const currentPreview = ref<any>(null)
const selectedFile = ref<any>(null)
const tableData = ref<any[]>([])

const form = reactive({
  title: '',
  author: ''
})

// Base URL for API requests. Adjust if proxy is configured differently.
const API_BASE = '/api/literature'

const fetchList = async () => {
  try {
    const res = await axios.get(`${API_BASE}/list`)
    if (res.data.code === 200) {
      tableData.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取文献列表失败')
  }
}

onMounted(() => {
  fetchList()
})

const handleAdd = () => {
  form.title = ''
  form.author = ''
  selectedFile.value = null
  dialogVisible.value = true
}

const handlePreview = (row: any) => {
  currentPreview.value = row
  previewVisible.value = true
}

const handleFileChange = (file: any) => {
  selectedFile.value = file
}

const handleFileRemove = () => {
  selectedFile.value = null
}

const submitForm = async () => {
  if (!form.title || !form.author) {
    ElMessage.warning('请填写完整的文献信息')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请上传文献文件附件')
    return
  }
  
  try {
    // 1. Upload file first
    const formData = new FormData()
    formData.append('file', selectedFile.value.raw)
    
    const uploadRes = await axios.post(`${API_BASE}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    
    if (uploadRes.data.code !== 200) {
      ElMessage.error('文件上传失败: ' + uploadRes.data.msg)
      return
    }
    
    const fileUrl = uploadRes.data.data // This is the /uploads/xxx path from backend
    
    // 2. Save metadata
    await axios.post(API_BASE, {
      title: form.title,
      author: form.author,
      content: fileUrl // Reusing content field to store the URL for simplicity in this demo
    })
    
    ElMessage.success('文献上传成功！')
    dialogVisible.value = false
    selectedFile.value = null
    fetchList()
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败，请稍后再试')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除文献 "${row.title}" 吗？`,
    '删除警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    }
  ).then(async () => {
    try {
      await axios.delete(`${API_BASE}/${row.id}`)
      ElMessage.success('已成功删除文献')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除操作')
  })
}
</script>

<style scoped>
.literature-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pdf-viewer-container {
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 400px;
  border: 1px solid #dcdfe6;
  overflow: hidden;
}
.no-content-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  color: #909399;
}
</style>
