<template>
  <div class="home-container">

    <!-- Role-aware welcome banner -->
    <div class="welcome-banner" :class="roleBannerClass">
      <div class="welcome-text">
        <div class="welcome-title">
          <el-icon style="margin-right:8px;"><component :is="roleIcon" /></el-icon>
          欢迎回来，{{ username }}
        </div>
        <div class="welcome-sub">{{ roleWelcomeText }}</div>
      </div>
      <el-tag :type="roleBadgeType" effect="dark" size="large">{{ currentRoleLabel }}</el-tag>
    </div>

    <!-- Stats row - role-aware -->
    <el-row :gutter="20" style="margin-top:16px;">
      <el-col :span="6" v-for="item in currentStats" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><component :is="item.icon" /></el-icon></div>
          <div class="stat-header">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>{{ chartTitle }}</template>
          <div ref="chartRef" style="height: 400px;"></div>
        </el-card>

        <!-- Shortcuts - role-aware -->
        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>快捷入口</template>
          <el-row :gutter="20">
            <el-col :span="6" v-for="shortcut in currentShortcuts" :key="shortcut.name">
              <el-card shadow="hover" class="shortcut-card" @click="$router.push(shortcut.path)">
                <el-icon class="shortcut-icon"><component :is="shortcut.icon" /></el-icon>
                <div class="shortcut-name">{{ shortcut.name }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- Admin: permission matrix panel -->
        <el-card shadow="hover" v-if="currentRole === 'Admin'">
          <template #header>
            <span><el-icon><Setting /></el-icon> 权限一览</span>
          </template>
          <el-table :data="permMatrix" size="small" border>
            <el-table-column prop="action" label="功能" width="110" />
            <el-table-column label="管理员" align="center">
              <template #default><el-icon color="#67c23a"><Select /></el-icon></template>
            </el-table-column>
            <el-table-column prop="teacher" label="教师" align="center">
              <template #default="{ row }">
                <el-icon :color="row.teacher ? '#67c23a' : '#f56c6c'">
                  <component :is="row.teacher ? 'Select' : 'CloseBold'" />
                </el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="user" label="学生" align="center">
              <template #default="{ row }">
                <el-icon :color="row.user ? '#67c23a' : '#f56c6c'">
                  <component :is="row.user ? 'Select' : 'CloseBold'" />
                </el-icon>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- Teacher: teaching status panel -->
        <el-card shadow="hover" v-else-if="currentRole === 'Teacher'">
          <template #header>
            <span><el-icon><Reading /></el-icon> 教学状态</span>
          </template>
          <div class="teacher-stats">
            <div class="t-stat" v-for="s in teacherStatus" :key="s.label">
              <span class="t-label">{{ s.label }}</span>
              <el-tag :type="s.type" size="small">{{ s.value }}</el-tag>
            </div>
          </div>
          <el-button type="primary" style="margin-top:20px; width:100%;" @click="$router.push('/admin/tests')" plain>进入教学管理</el-button>
        </el-card>

        <!-- User: AI assistant panel -->
        <el-card shadow="hover" v-else>
          <template #header>AI 学习助手</template>
          <div class="ai-box">
            <p>基于您的学习记录，AI 建议：</p>
            <el-skeleton :rows="3" animated v-if="loadingAdvice" />
            <div v-else>
              <el-tag type="success" style="margin-bottom: 10px;">智能学习分析</el-tag>
              <p style="color: #666; font-size: 14px; line-height: 1.6;">{{ aiAdvice }}</p>
            </div>
            <el-button type="primary" style="margin-top: 20px;" @click="$router.push('/ai-tutoring')" plain>进入AI辅导</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const username = ref(localStorage.getItem('username') || '用户')
const aiAdvice = ref('')
const loadingAdvice = ref(true)
const chartRef = ref<HTMLElement | null>(null)

// ── Role metadata ──────────────────────────────────────
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
const roleBannerClass = computed(() => `banner-${currentRole.value.toLowerCase()}`)
const roleIcon = computed(() => {
  if (currentRole.value === 'Admin') return 'Setting'
  if (currentRole.value === 'Teacher') return 'Reading'
  return 'User'
})
const roleWelcomeText = computed(() => {
  if (currentRole.value === 'Admin') return '您拥有系统最高权限，可管理所有用户与内容'
  if (currentRole.value === 'Teacher') return '您可以发布内容、管理测试题库，查看学生学情'
  return '继续您的英语学习之旅，今天也要加油哦！'
})

// ── Role-aware stats ────────────────────────────────────
const adminStats = [
  { title: '总注册用户数', value: '15,280', icon: 'User' },
  { title: '今日活跃用户', value: '3,415', icon: 'TrendCharts' },
  { title: '总题库数量', value: '8,156', icon: 'Collection' },
  { title: '总讨论互动数', value: '98,100', icon: 'ChatDotRound' }
]
const teacherStats = [
  { title: '我的班级学生', value: '128', icon: 'User' },
  { title: '已发布内容', value: '47', icon: 'Document' },
  { title: '待批改测试', value: '12', icon: 'EditPen' },
  { title: '本周互动次数', value: '236', icon: 'ChatDotRound' }
]
const userStats = [
  { title: '已学词汇', value: '1,280', icon: 'Collection' },
  { title: '完成文献', value: '15', icon: 'Document' },
  { title: '学习时长(H)', value: '156', icon: 'Clock' },
  { title: '今日自测', value: '98/100', icon: 'Trophy' }
]
const currentStats = computed(() => {
  if (currentRole.value === 'Admin') return adminStats
  if (currentRole.value === 'Teacher') return teacherStats
  return userStats
})

// ── Role-aware shortcuts ────────────────────────────────
const adminShortcuts = [
  { name: '用户管理', icon: 'User', path: '/admin/users' },
  { name: '系统设置', icon: 'Setting', path: '/admin/settings' },
  { name: '测试管理', icon: 'DocumentChecked', path: '/admin/tests' },
  { name: 'API集成', icon: 'Connection', path: '/admin/tools' }
]
const teacherShortcuts = [
  { name: '词汇管理', icon: 'Collection', path: '/vocab' },
  { name: '语法管理', icon: 'Notebook', path: '/grammar' },
  { name: '测试管理', icon: 'DocumentChecked', path: '/admin/tests' },
  { name: '文献上传', icon: 'Document', path: '/literature' }
]
const userShortcuts = [
  { name: '口语练习', icon: 'Microphone', path: '/oral' },
  { name: '听力训练', icon: 'Headset', path: '/listening' },
  { name: '阅读理解', icon: 'Document', path: '/reading' },
  { name: '选词填空', icon: 'EditPen', path: '/cloze' }
]
const currentShortcuts = computed(() => {
  if (currentRole.value === 'Admin') return adminShortcuts
  if (currentRole.value === 'Teacher') return teacherShortcuts
  return userShortcuts
})

const chartTitle = computed(() => {
  if (currentRole.value === 'Admin') return '用户增长趋势 (近6个月)'
  if (currentRole.value === 'Teacher') return '内容发布与互动趋势 (近6个月)'
  return '个人学习趋势 (本周)'
})

// ── Permission matrix (for Admin panel) ──────────────────
const permMatrix = [
  { action: '发布内容', teacher: true, user: false },
  { action: '删除内容', teacher: false, user: false },
  { action: '生成测试', teacher: true, user: false },
  { action: '管理用户', teacher: false, user: false },
  { action: '发表评论', teacher: true, user: true },
  { action: '查看学情', teacher: true, user: false },
  { action: 'AI辅导', teacher: true, user: true }
]

// ── Teacher status items ──────────────────────────────────
const teacherStatus = [
  { label: '今日发布', value: '2 篇', type: 'success' },
  { label: '待批改', value: '12 份', type: 'warning' },
  { label: '学生消息', value: '5 条', type: 'danger' },
  { label: '本月评分', value: '4.9 ⭐', type: 'info' }
]

// ── Chart and data fetching ───────────────────────────────
const fetchAiAdvice = async () => {
  try {
    const res = await axios.post('/api/ai/advice', {})
    if (res.data.code === 200) {
      aiAdvice.value = res.data.data.advice
    }
  } catch (error) {
    aiAdvice.value = '无法获取 AI 建议，请稍后再试。'
  } finally {
    loadingAdvice.value = false
  }
}

onMounted(() => {
  if (currentRole.value === 'User') fetchAiAdvice()
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    let option: any
    if (currentRole.value === 'Admin') {
      option = {
        tooltip: { trigger: 'axis' },
        color: ['#f56c6c'],
        xAxis: { type: 'category', data: ['一月', '二月', '三月', '四月', '五月', '六月'] },
        yAxis: { type: 'value' },
        series: [{ name: '新增用户', data: [820, 932, 901, 1034, 1290, 1530], type: 'line', smooth: true, areaStyle: {} }]
      }
    } else if (currentRole.value === 'Teacher') {
      option = {
        tooltip: { trigger: 'axis' },
        color: ['#e6a23c', '#409eff'],
        legend: { data: ['发布内容', '学生互动'] },
        xAxis: { type: 'category', data: ['一月', '二月', '三月', '四月', '五月', '六月'] },
        yAxis: { type: 'value' },
        series: [
          { name: '发布内容', data: [5, 8, 6, 10, 9, 12], type: 'bar' },
          { name: '学生互动', data: [30, 55, 41, 68, 72, 90], type: 'line', smooth: true }
        ]
      }
    } else {
      option = {
        color: ['#409eff'],
        xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
        yAxis: { type: 'value' },
        series: [{ data: [30, 45, 60, 20, 80, 100, 50], type: 'line', smooth: true, areaStyle: {} }]
      }
    }
    chart.setOption(option)
  }
})
</script>

<style scoped>
.home-container { padding: 10px; }

.welcome-banner {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 24px; border-radius: 10px; color: #fff;
}
.banner-admin { background: linear-gradient(135deg, #f56c6c, #d93025); }
.banner-teacher { background: linear-gradient(135deg, #e6a23c, #c77c1a); }
.banner-user { background: linear-gradient(135deg, #409eff, #1677d1); }
.welcome-title { font-size: 18px; font-weight: 700; display: flex; align-items: center; }
.welcome-sub { font-size: 13px; margin-top: 4px; opacity: 0.88; }

.stat-card { text-align: center; }
.stat-icon { font-size: 28px; color: #409eff; margin-bottom: 6px; }
.stat-header { color: #909399; font-size: 13px; margin-bottom: 6px; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; }

.shortcut-card { text-align: center; cursor: pointer; transition: all 0.3s; }
.shortcut-card:hover { transform: translateY(-5px); }
.shortcut-icon { font-size: 30px; color: #409eff; margin-bottom: 8px; }
.shortcut-name { font-size: 13px; color: #606266; }

.teacher-stats { display: flex; flex-direction: column; gap: 12px; padding: 10px 0; }
.t-stat { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
.t-label { font-size: 13px; color: #606266; }
.ai-box { padding: 10px; }
</style>
