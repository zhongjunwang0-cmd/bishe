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
                <T5ModelBadge v-if="shortcut.model === 't5'" variant="tag" class="shortcut-badge" />
                <WhisperModelBadge v-else-if="shortcut.model === 'whisper'" variant="tag" class="shortcut-badge" />
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="8">
        <T5IntegrationPanel style="margin-bottom: 20px;" />

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
          <el-button type="primary" style="margin-top:20px; width:100%;" @click="goTeachingManagement" plain>进入教学管理</el-button>
        </el-card>

        <!-- User: AI assistant panel -->
        <el-card shadow="hover" v-else>
          <template #header>
            <div class="recommend-header">
              <span>AI 学习助手</span>
              <el-tag v-if="recommendSource === 'dkt_model'" type="success" size="small">DKT 模型</el-tag>
              <el-tag v-else-if="recommendSource === 'ml_model'" type="success" size="small">ML 模型</el-tag>
              <el-tag v-else-if="recommendSource" type="info" size="small">规则推荐</el-tag>
            </div>
          </template>
          <div class="ai-box">
            <el-skeleton :rows="4" animated v-if="loadingRecommend" />
            <div v-else>
              <div class="mastery-row">
                <span class="mastery-label">综合掌握度</span>
                <el-progress
                  :percentage="Math.round((recommend.mastery || 0) * 100)"
                  :stroke-width="10"
                  style="flex: 1; margin-left: 12px;"
                />
              </div>
              <p v-if="recommend.advice" class="advice-text">{{ recommend.advice }}</p>
              <div v-if="recommend.weakModules?.length" class="weak-section">
                <div class="section-title">今日薄弱模块</div>
                <el-tag
                  v-for="m in recommend.weakModules"
                  :key="m"
                  type="warning"
                  style="margin: 0 6px 6px 0;"
                >{{ m }}</el-tag>
              </div>
              <div v-if="recommend.todayTasks?.length" class="task-section">
                <div class="section-title">推荐任务</div>
                <ul class="task-list">
                  <li v-for="(task, i) in recommend.todayTasks" :key="i">{{ task }}</li>
                </ul>
              </div>
              <p v-if="!recommend.weakModules?.length && !recommend.todayTasks?.length" class="advice-text">
                完成任意模块练习后，系统将基于学习记录生成个性化推荐。
              </p>
            </div>
            <el-button type="primary" style="margin-top: 16px;" @click="$router.push('/ai-tutoring')" plain>进入AI辅导</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import axios from 'axios'
import T5IntegrationPanel from '../components/T5IntegrationPanel.vue'
import T5ModelBadge from '../components/T5ModelBadge.vue'
import WhisperModelBadge from '../components/WhisperModelBadge.vue'

const router = useRouter()

const currentRole = inject<any>('currentRole', ref('User'))
const username = ref(localStorage.getItem('username') || '用户')
const loadingRecommend = ref(true)
const recommendSource = ref('')
const recommend = ref<{
  weakModules: string[]
  todayTasks: string[]
  mastery: number
  advice: string
  source: string
}>({
  weakModules: [],
  todayTasks: [],
  mastery: 0,
  advice: '',
  source: ''
})
const chartRef = ref<HTMLElement | null>(null)
const statsData = ref<any>(null)

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
const adminStats = ref([
  { title: '总注册用户数', value: '-', icon: 'User' },
  { title: '今日活跃用户', value: '-', icon: 'TrendCharts' },
  { title: '学习记录总数', value: '-', icon: 'Collection' },
  { title: '覆盖模块数', value: '-', icon: 'ChatDotRound' }
])
const teacherStats = ref([
  { title: '我的班级学生', value: '128', icon: 'User' },
  { title: '已发布内容', value: '47', icon: 'Document' },
  { title: '待批改测试', value: '12', icon: 'EditPen' },
  { title: '本周互动次数', value: '236', icon: 'ChatDotRound' }
])
const userStats = ref([
  { title: '已学词汇', value: '-', icon: 'Collection' },
  { title: '完成文献', value: '-', icon: 'Document' },
  { title: '学习时长(H)', value: '-', icon: 'Clock' },
  { title: '今日自测', value: '-', icon: 'Trophy' }
])
const currentStats = computed(() => {
  if (currentRole.value === 'Admin') return adminStats.value
  if (currentRole.value === 'Teacher') return teacherStats.value
  return userStats.value
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
  { name: '口语练习', icon: 'Microphone', path: '/oral', model: 'whisper' },
  { name: '听力训练', icon: 'Headset', path: '/listening' },
  { name: '阅读理解', icon: 'Document', path: '/reading' },
  { name: '选词填空', icon: 'EditPen', path: '/cloze', model: 't5' },
]
const currentShortcuts = computed(() => {
  if (currentRole.value === 'Admin') return adminShortcuts
  if (currentRole.value === 'Teacher') return teacherShortcuts
  return userShortcuts
})

const chartTitle = computed(() => {
  if (currentRole.value === 'Admin') return '平台学习时长趋势 (近6个月)'
  if (currentRole.value === 'Teacher') return '平台学习时长与成绩趋势 (近6个月)'
  return '个人学习趋势 (本周)'
})

const applySummary = (summary: any) => {
  if (!summary) return
  if (currentRole.value === 'Admin') {
    adminStats.value = [
      { title: '总注册用户数', value: String(summary.totalUsers ?? '-'), icon: 'User' },
      { title: '今日活跃用户', value: String(summary.todayActiveUsers ?? '-'), icon: 'TrendCharts' },
      { title: '学习记录总数', value: String(summary.totalRecords ?? '-'), icon: 'Collection' },
      { title: '覆盖模块数', value: String(summary.moduleTypes ?? '-'), icon: 'ChatDotRound' }
    ]
  } else if (currentRole.value === 'User') {
    userStats.value = [
      { title: '已学词汇', value: String(summary.masteredVocab ?? 0), icon: 'Collection' },
      { title: '完成文献', value: String(summary.litCompleted ?? 0), icon: 'Document' },
      { title: '学习时长(H)', value: String(summary.totalDurationHours ?? 0), icon: 'Clock' },
      { title: '今日自测', value: summary.todayScore != null ? `${summary.todayScore}/${summary.todayScoreMax}` : '暂无', icon: 'Trophy' }
    ]
  }
}

const buildChartOption = (data: any) => {
  const durationTrend = data?.durationTrend ?? { categories: [], values: [] }
  const scoreTrend = data?.scoreTrend ?? { categories: [], values: [] }
  const isGlobal = currentRole.value === 'Admin' || currentRole.value === 'Teacher'

  if (isGlobal) {
    return {
      tooltip: { trigger: 'axis' },
      legend: { data: ['学习时长(小时)', '平均成绩'] },
      color: ['#409eff', '#67c23a'],
      xAxis: { type: 'category', data: durationTrend.categories },
      yAxis: [
        { type: 'value', name: '小时' },
        { type: 'value', name: '分数', min: 0, max: 100 }
      ],
      series: [
        { name: '学习时长(小时)', data: durationTrend.values, type: 'line', smooth: true, areaStyle: {} },
        { name: '平均成绩', data: scoreTrend.values, type: 'line', smooth: true, yAxisIndex: 1 }
      ]
    }
  }

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['学习时长(分钟)', '平均成绩'] },
    color: ['#409eff', '#67c23a'],
    xAxis: { type: 'category', data: durationTrend.categories },
    yAxis: [
      { type: 'value', name: '分钟' },
      { type: 'value', name: '分数', min: 0, max: 100 }
    ],
    series: [
      { name: '学习时长(分钟)', data: durationTrend.values, type: 'line', smooth: true, areaStyle: {} },
      { name: '平均成绩', data: scoreTrend.values, type: 'line', smooth: true, yAxisIndex: 1 }
    ]
  }
}

const fetchStats = async () => {
  const scope = (currentRole.value === 'Admin' || currentRole.value === 'Teacher') ? 'global' : 'personal'
  try {
    const res = await axios.get('/api/stats', { params: { scope } })
    if (res.data.code === 200) {
      statsData.value = res.data.data
      applySummary(res.data.data.summary)
      if (chartRef.value) {
        const chart = echarts.init(chartRef.value)
        chart.setOption(buildChartOption(res.data.data))
      }
    }
  } catch {
    // keep defaults on error
  }
}

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

const goTeachingManagement = () => {
  router.push('/admin/teaching')
}

// ── Chart and data fetching ───────────────────────────────
const fetchRecommend = async () => {
  loadingRecommend.value = true
  try {
    const res = await axios.get('/api/ai/recommend')
    if (res.data.code === 200 && res.data.data) {
      const d = res.data.data
      recommend.value = {
        weakModules: d.weakModules || [],
        todayTasks: d.todayTasks || [],
        mastery: d.mastery ?? 0,
        advice: d.advice || '',
        source: d.source || ''
      }
      recommendSource.value = d.source || ''
    }
  } catch {
    recommend.value.advice = '无法获取学习推荐，请确认后端与 AI 服务已启动。'
  } finally {
    loadingRecommend.value = false
  }
}

onMounted(() => {
  if (currentRole.value === 'User') fetchRecommend()
  fetchStats()
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

.shortcut-card { text-align: center; cursor: pointer; transition: all 0.3s; position: relative; }
.shortcut-card:hover { transform: translateY(-5px); }
.shortcut-badge { margin-top: 8px; }
.shortcut-icon { font-size: 30px; color: #409eff; margin-bottom: 8px; }
.shortcut-name { font-size: 13px; color: #606266; }

.teacher-stats { display: flex; flex-direction: column; gap: 12px; padding: 10px 0; }
.t-stat { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
.t-label { font-size: 13px; color: #606266; }
.ai-box { padding: 10px; }
.recommend-header { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.mastery-row { display: flex; align-items: center; margin-bottom: 12px; }
.mastery-label { font-size: 13px; color: #606266; white-space: nowrap; }
.advice-text { color: #666; font-size: 14px; line-height: 1.6; margin: 0 0 12px; }
.section-title { font-size: 13px; font-weight: 600; color: #303133; margin-bottom: 8px; }
.task-list { margin: 0; padding-left: 18px; color: #606266; font-size: 13px; line-height: 1.8; }
.weak-section, .task-section { margin-bottom: 12px; }
</style>
