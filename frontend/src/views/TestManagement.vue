<template>
  <div class="test-management-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>自测题目管理</span>
          <el-button type="primary" icon="Plus" @click="dialogVisible = true">新增题库</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="title" label="试卷/题目名称" />
        <el-table-column prop="type" label="题型" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'Active' ? 'success' : 'info'">
              {{ row.status === 'Active' ? '使用中' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">在线编辑</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'Active'" @click="handleToggleStatus(row)">下线</el-button>
            <el-button link type="success" size="small" v-else @click="handleToggleStatus(row)">上线</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增自测题目" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题目名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="form.type" placeholder="选择题型" style="width: 100%">
            <el-option label="阅读理解" value="Reading" />
            <el-option label="听力测试" value="Listening" />
            <el-option label="选词填空" value="Cloze" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
           <el-input v-model="form.content" type="textarea" :rows="4" placeholder="支持Markdown格式输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const dialogVisible = ref(false)
const form = ref({ title: '', type: 'Reading', content: '' })

const handleEdit = (row: any) => {
  ElMessage.info(`正在编辑【${row.title}】...`)
}

const handleToggleStatus = (row: any) => {
  if (row.status === 'Active') {
    row.status = 'Inactive'
    ElMessage.success(`【${row.title}】已成功下线`)
  } else {
    row.status = 'Active'
    ElMessage.success(`【${row.title}】已成功上线`)
  }
}

const tableData = ref([
  { id: 1, title: 'CET4 阅读理解真题演练 (一)', type: '阅读理解', status: 'Active' },
  { id: 2, title: '进阶听力训练 V1.0', type: '听力测试', status: 'Active' },
  { id: 3, title: '科技类短文选词填空', type: '选词填空', status: 'Inactive' }
])
const handleSave = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
}
</script>

<style scoped>
.test-management-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
