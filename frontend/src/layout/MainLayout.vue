<template>
  <el-container class="layout-container">
    <el-aside width="240px">
      <div class="logo">
        <el-icon><Reading /></el-icon>
        <span>英语学习系统</span>
      </div>

      <!-- Role Badge -->
      <div class="role-badge-wrap">
        <el-avatar :size="40" :style="{ background: roleBadgeColor }">
          {{ username.substring(0, 1).toUpperCase() }}
        </el-avatar>
        <div class="role-info">
          <div class="role-username">{{ username }}</div>
          <el-tag :type="roleBadgeType" size="small" effect="dark">{{ currentRoleLabel }}</el-tag>
        </div>
      </div>

      <el-menu :default-active="$route.path" router class="el-menu-vertical">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页看板</span>
        </el-menu-item>

        <el-menu-item-group title="学习模块">
          <el-menu-item index="/vocab">
            <el-icon><Collection /></el-icon>
            <span class="menu-item-label">词汇学习<T5ModelBadge variant="menu" /></span>
          </el-menu-item>
          <el-menu-item index="/grammar">
            <el-icon><Notebook /></el-icon>
            <span class="menu-item-label">语法学习<T5ModelBadge variant="menu" /></span>
          </el-menu-item>
          <el-menu-item index="/oral">
            <el-icon><Microphone /></el-icon>
            <span class="menu-item-label">口语练习<WhisperModelBadge variant="menu" /></span>
          </el-menu-item>
          <el-menu-item index="/literature">
            <el-icon><Document /></el-icon>
            <span>英语文献阅读</span>
          </el-menu-item>
          <el-menu-item index="/reading">
            <el-icon><DocumentCopy /></el-icon>
            <span>阅读理解</span>
          </el-menu-item>
          <el-menu-item index="/listening">
            <el-icon><Headset /></el-icon>
            <span>听力训练</span>
          </el-menu-item>
          <el-menu-item index="/cloze">
            <el-icon><EditPen /></el-icon>
            <span class="menu-item-label">选词填空<T5ModelBadge variant="menu" /></span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="辅助功能">
          <el-menu-item index="/record">
            <el-icon><DataLine /></el-icon>
            <span>学习记录追踪</span>
          </el-menu-item>
          <el-menu-item index="/discussion">
            <el-icon><ChatDotRound /></el-icon>
            <span>讨论区评论</span>
          </el-menu-item>
          <el-menu-item index="/ai-tutoring">
            <el-icon><Service /></el-icon>
            <span class="menu-item-label">AI辅导学习<T5ModelBadge variant="menu" /></span>
          </el-menu-item>
        </el-menu-item-group>

        <!-- Teacher + Admin: 教学管理 -->
        <el-sub-menu index="teacher-group" v-if="currentRole === 'Teacher' || currentRole === 'Admin'">
          <template #title>
            <el-icon><EditPen /></el-icon>
            <span>教学管理</span>
          </template>
          <el-menu-item index="/admin/teaching">
            <el-icon><Reading /></el-icon>
            <span>教学管理首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/vocab" v-if="currentRole === 'Teacher' || currentRole === 'Admin'">
            <el-icon><Collection /></el-icon>
            <span>词汇内容管理</span>
          </el-menu-item>
          <el-menu-item index="/grammar">
            <el-icon><Notebook /></el-icon>
            <span>语法内容管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tests">
            <el-icon><DocumentChecked /></el-icon>
            <span>测试题库管理</span>
          </el-menu-item>
          <el-menu-item index="/reading">
            <el-icon><DocumentCopy /></el-icon>
            <span>阅读测试生成</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- Admin only: 系统管理 -->
        <el-sub-menu index="admin-group" v-if="currentRole === 'Admin'">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户与权限控制</span>
          </el-menu-item>
          <el-menu-item index="/admin/settings">
            <el-icon><Operation /></el-icon>
            <span>系统参数设置</span>
          </el-menu-item>
          <el-menu-item index="/admin/tools">
            <el-icon><Connection /></el-icon>
            <span>工具与API集成</span>
          </el-menu-item>
        </el-sub-menu>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.name }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="roleBadgeType" effect="plain" style="margin-right:12px;">{{ currentRoleLabel }}</el-tag>
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :style="{ background: roleBadgeColor }">{{ username.substring(0, 1).toUpperCase() }}</el-avatar>
              <span class="username">{{ username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, provide } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { preserveSavedLoginOnClear } from '../utils/authStorage'
import T5ModelBadge from '../components/T5ModelBadge.vue'
import WhisperModelBadge from '../components/WhisperModelBadge.vue'

const router = useRouter()

const currentRole = ref(localStorage.getItem('userRole') || 'User')
const username = ref(localStorage.getItem('username') || '用户')
provide('currentRole', currentRole)

const currentRoleLabel = computed(() => {
  if (currentRole.value === 'Admin') return '管理员'
  if (currentRole.value === 'Teacher') return '教师'
  return '普通用户'
})

const roleBadgeType = computed(() => {
  if (currentRole.value === 'Admin') return 'danger'
  if (currentRole.value === 'Teacher') return 'warning'
  return 'success'
})

const roleBadgeColor = computed(() => {
  if (currentRole.value === 'Admin') return '#f56c6c'
  if (currentRole.value === 'Teacher') return '#e6a23c'
  return '#67c23a'
})

const handleLogout = async () => {
  try {
    await axios.get('/api/user/logout')
  } catch (e) {}
  preserveSavedLoginOnClear()
  router.push('/login')
}
</script>

<style scoped>
.layout-container { height: 100vh; background: #f5f7fa; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; color: #409eff; gap: 8px; border-bottom: 1px solid #e4e7ed; }
.role-badge-wrap { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: linear-gradient(135deg, #f5f7fa 0%, #eef0f3 100%); border-bottom: 1px solid #e4e7ed; }
.role-info { display: flex; flex-direction: column; gap: 4px; }
.role-username { font-size: 13px; font-weight: 600; color: #303133; }
.el-menu-vertical { border-right: none; height: calc(100vh - 140px); overflow-y: auto; }
.el-header { background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #ebeef5; padding: 0 20px; }
.user-info { display: flex; align-items: center; cursor: pointer; gap: 10px; }
.username { font-size: 14px; color: #303133; }
.menu-item-label {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
}
</style>
