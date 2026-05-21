<template>
  <div class="user-management-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户权限管理</span>
          <el-button type="primary" icon="Plus" @click="dialogVisible = true">新增用户</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'Admin' ? 'danger' : (row.role === 'Teacher' ? 'warning' : 'success')">
              {{ row.role === 'Admin' ? '管理员' : (row.role === 'Teacher' ? '教师' : '普通用户') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-text="启用" inactive-text="禁用" disabled />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditRole(row)">编辑权限</el-button>
            <el-button link type="danger" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add User Dialog -->
    <el-dialog v-model="dialogVisible" title="新增用户" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色分配">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="Admin" />
            <el-option label="教师" value="Teacher" />
            <el-option label="普通用户" value="User" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddUser" :loading="submitting">确认生成</el-button>
      </template>
    </el-dialog>

    <!-- Edit Role Dialog -->
    <el-dialog v-model="roleDialogVisible" title="编辑权限" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="newRole" placeholder="请选择新角色" style="width: 100%">
            <el-option label="管理员" value="Admin" />
            <el-option label="教师" value="Teacher" />
            <el-option label="普通用户" value="User" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditRole" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const tableData = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)

const dialogVisible = ref(false)
const form = ref({ username: '', role: 'User' })

const roleDialogVisible = ref(false)
const currentUser = ref<any>(null)
const newRole = ref('User')

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/user/list')
    if (res.data.code === 200) {
      tableData.value = res.data.data
    } else {
      ElMessage.error(res.data.message || '获取用户列表失败')
    }
  } catch (error) {
    ElMessage.error('无法加载用户列表')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})

const submitAddUser = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  submitting.value = true
  try {
    const res = await axios.post('/api/admin/user/add', form.value)
    if (res.data.code === 200) {
      ElMessage.success('用户新增成功，默认密码为：123456')
      dialogVisible.value = false
      form.value = { username: '', role: 'User' }
      fetchUsers()
    } else {
      ElMessage.error(res.data.msg || '新增失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，无法新增用户')
  } finally {
    submitting.value = false
  }
}

const handleEditRole = (row: any) => {
  currentUser.value = row
  newRole.value = row.role
  roleDialogVisible.value = true
}

const submitEditRole = async () => {
  submitting.value = true
  try {
    const res = await axios.put(`/api/admin/user/${currentUser.value.id}/role`, { role: newRole.value })
    if (res.data.code === 200) {
      ElMessage.success('权限更新成功')
      roleDialogVisible.value = false
      fetchUsers()
    } else {
      ElMessage.error(res.data.msg || '更新失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，无法更新权限')
  } finally {
    submitting.value = false
  }
}

const handleResetPassword = (row: any) => {
  ElMessageBox.confirm(`确定要将用户【${row.username}】的密码重置为 123456 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.put(`/api/admin/user/${row.id}/reset-password`)
      if (res.data.code === 200) {
        ElMessage.success('密码已成功重置为 123456')
      } else {
        ElMessage.error(res.data.msg || '重置失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，无法重置密码')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.user-management-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
