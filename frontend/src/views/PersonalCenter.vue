<template>
  <div class="user-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="profile-card">
          <div class="avatar-wrapper">
            <el-avatar :size="100">User</el-avatar>
          </div>
          <div class="user-info">
            <h2>{{ userInfo.username }}</h2>
            <p>{{ userInfo.email }}</p>
            <el-tag type="success" size="small">普通用户</el-tag>
          </div>
          <el-divider />
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-num">128</div>
              <div class="stat-label">连续打卡</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">Level 5</div>
              <div class="stat-label">学习等级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>基本信息设置</template>
          <el-form :model="form" label-width="100px" style="max-width: 500px;">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="form.bio" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const userInfo = ref({
  username: 'Admin',
  email: 'admin@example.com'
})

const form = reactive({
  username: 'admin',
  nickname: 'Learning Master',
  email: 'admin@example.com',
  bio: 'Keep learning, keep growing.'
})

const saveProfile = () => {
  ElMessage.success('个人信息更新成功 (演示模式)')
  userInfo.value.username = form.nickname
  userInfo.value.email = form.email
}
</script>

<style scoped>
.user-container { padding: 10px; }
.profile-card { text-align: center; }
.avatar-wrapper { margin: 20px 0; }
.user-info h2 { margin: 10px 0 5px; color: #303133; }
.user-info p { color: #909399; margin: 0 0 15px; font-size: 14px; }
.user-stats { display: flex; justify-content: space-around; margin-top: 20px; text-align: center; }
.stat-num { font-size: 20px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 12px; color: #909399; margin-top: 5px; }
</style>
