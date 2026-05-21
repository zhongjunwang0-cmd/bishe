<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-title">英语学习系统</div>
      
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" @submit.prevent="handleLogin">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleLogin">立即登录</el-button>
            </el-form-item>
            <div class="login-footer">
              <el-link type="primary" :underline="false" @click="forgotPasswordVisible = true">忘记密码？</el-link>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" @submit.prevent="handleRegister">
            <el-form-item>
              <el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="success" class="login-btn" @click="handleRegister">立即注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Forgot Password Dialog -->
    <el-dialog v-model="forgotPasswordVisible" title="重置密码" width="400px">
      <el-form :model="forgotPasswordForm">
        <el-form-item label="用户名" label-width="80px">
          <el-input v-model="forgotPasswordForm.username" placeholder="请输入绑定的用户名" />
        </el-form-item>
        <el-form-item label="新密码" label-width="80px">
          <el-input v-model="forgotPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const activeTab = ref('login')
const forgotPasswordVisible = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })
const forgotPasswordForm = reactive({ username: '', newPassword: '' })

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  
  try {
    const res = await axios.post('/api/user/login', loginForm)
    if (res.data.code === 200) {
      ElMessage.success('欢迎回来！')
      const user = res.data.data
      let role = 'User'
      if (user.roleId === 1) role = 'Admin'
      else if (user.roleId === 2) role = 'Teacher'
      
      localStorage.setItem('userRole', role)
      localStorage.setItem('username', user.username)
      router.push('/')
    } else {
      ElMessage.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    ElMessage.error('服务器连接失败，请稍后再试')
  }
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('请填写完整的注册信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  try {
    const res = await axios.post('/api/user/register', {
      username: registerForm.username,
      password: registerForm.password
    })
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.username = registerForm.username
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
    } else {
      ElMessage.error(res.data.msg || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册过程中发生错误')
  }
}

const handleResetPassword = async () => {
  if (!forgotPasswordForm.username || !forgotPasswordForm.newPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const res = await axios.post('/api/user/forgot-password', {
      username: forgotPasswordForm.username,
      password: forgotPasswordForm.newPassword
    })
    if (res.data.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      forgotPasswordVisible.value = false
      forgotPasswordForm.username = ''
      forgotPasswordForm.newPassword = ''
    } else {
      ElMessage.error(res.data.msg || '重置失败')
    }
  } catch (error) {
    ElMessage.error('重置过程中发生错误')
  }
}
</script>

<style scoped>
.login-container { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%); }
.login-card { width: 400px; padding: 20px; border-radius: 15px; }
.login-title { font-size: 24px; font-weight: bold; text-align: center; margin-bottom: 20px; color: #409eff; }
.login-btn { width: 100%; height: 40px; }
.login-footer { display: flex; justify-content: flex-end; margin-top: 10px; font-size: 14px; }
</style>
