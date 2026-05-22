<template>
  <div class="user-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="profile-card">
          <div class="avatar-wrapper">
            <el-avatar :size="100">{{ avatarLetter }}</el-avatar>
          </div>
          <div class="user-info">
            <h2>{{ userInfo.nickname || userInfo.username }}</h2>
            <p>{{ userInfo.email || '未设置邮箱' }}</p>
            <el-tag type="success" size="small">普通用户</el-tag>
          </div>
          <el-divider />
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.level || 'B1' }}</div>
              <div class="stat-label">当前水平</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.dailyGoal || 30 }} 分</div>
              <div class="stat-label">每日目标</div>
            </div>
            <div class="stat-item">
              <div class="stat-num">{{ userInfo.targetExam || 'GENERAL' }}</div>
              <div class="stat-label">目标考试</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>个性化学习档案</template>
          <el-form :model="form" label-width="110px" style="max-width: 560px;">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
            <el-form-item label="英语水平">
              <el-select v-model="form.level" placeholder="选择当前水平" style="width: 100%;">
                <el-option v-for="opt in levelOptions" :key="opt" :label="opt" :value="opt" />
              </el-select>
            </el-form-item>
            <el-form-item label="目标考试">
              <el-select v-model="form.targetExam" placeholder="选择备考目标" style="width: 100%;">
                <el-option v-for="opt in examOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="每日目标">
              <el-slider v-model="form.dailyGoal" :min="10" :max="120" :step="5" show-input />
              <div class="form-tip">建议 30–60 分钟，系统将据此生成周计划任务量</div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存并更新计划</el-button>
              <el-button :loading="generating" @click="regeneratePlan">重新生成本周计划</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="plan-header">
              <span>本周学习计划</span>
              <span v-if="weeklyPlan" class="plan-meta">
                {{ weeklyPlan.weekStart }} ~ {{ weeklyPlan.weekEnd }}
                · 完成 {{ weeklyPlan.completedCount }}/{{ weeklyPlan.totalCount }}
              </span>
            </div>
          </template>

          <el-skeleton :rows="6" animated v-if="loadingPlan" />
          <el-empty v-else-if="!weeklyPlan" description="保存学习档案后将自动生成周计划" />
          <div v-else>
            <el-progress
              :percentage="planProgress"
              :stroke-width="12"
              style="margin-bottom: 16px;"
            />
            <el-collapse v-model="expandedDays">
              <el-collapse-item
                v-for="day in weeklyPlan.days"
                :key="day.date"
                :name="day.date"
              >
                <template #title>
                  <div class="day-title">
                    <span>{{ day.dayLabel }}（{{ day.date }}）</span>
                    <el-tag v-if="day.today" type="primary" size="small">今天</el-tag>
                    <el-tag size="small" type="info">
                      {{ dayCompleted(day) }}/{{ day.items.length }}
                    </el-tag>
                  </div>
                </template>
                <div v-for="item in day.items" :key="item.id" class="plan-item">
                  <el-checkbox
                    :model-value="item.completed"
                    @change="(val: boolean) => toggleComplete(item, val)"
                  >
                    <el-tag size="small" effect="plain">{{ item.moduleLabel }}</el-tag>
                    <el-tag v-if="item.source === 'ai_recommend'" size="small" type="warning">AI</el-tag>
                    {{ item.taskText }}
                    <span class="item-minutes">约 {{ item.targetMinutes }} 分钟</span>
                  </el-checkbox>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

interface PlanItem {
  id: number
  planDate: string
  dayOfWeek: number
  dayLabel: string
  module: string
  moduleLabel: string
  source?: string
  taskText: string
  targetMinutes: number
  completed: boolean
}

interface PlanDay {
  date: string
  dayOfWeek: number
  dayLabel: string
  today: boolean
  items: PlanItem[]
}

interface WeeklyPlan {
  weekStart: string
  weekEnd: string
  level: string
  targetExam: string
  dailyGoal: number
  completedCount: number
  totalCount: number
  days: PlanDay[]
  todayItems: PlanItem[]
}

const levelOptions = ['A1', 'A2', 'B1', 'B2', 'CET-4', 'CET-6', 'IELTS', 'TOEFL']
const examOptions = [
  { label: '通用提升', value: 'GENERAL' },
  { label: '大学英语四级 (CET-4)', value: 'CET-4' },
  { label: '大学英语六级 (CET-6)', value: 'CET-6' },
  { label: '雅思 (IELTS)', value: 'IELTS' },
  { label: '托福 (TOEFL)', value: 'TOEFL' }
]

const userInfo = ref({
  username: '',
  nickname: '',
  email: '',
  level: 'B1',
  targetExam: 'GENERAL',
  dailyGoal: 30
})

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  level: 'B1',
  targetExam: 'GENERAL',
  dailyGoal: 30
})

const weeklyPlan = ref<WeeklyPlan | null>(null)
const loadingPlan = ref(true)
const saving = ref(false)
const generating = ref(false)
const expandedDays = ref<string[]>([])

const avatarLetter = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || 'U'
  return name.substring(0, 1).toUpperCase()
})

const planProgress = computed(() => {
  if (!weeklyPlan.value || !weeklyPlan.value.totalCount) return 0
  return Math.round((weeklyPlan.value.completedCount / weeklyPlan.value.totalCount) * 100)
})

const dayCompleted = (day: PlanDay) => day.items.filter(i => i.completed).length

const fetchProfile = async () => {
  try {
    const res = await axios.get('/api/profile/info')
    if (res.data.code === 200 && res.data.data) {
      const d = res.data.data
      userInfo.value = { ...d }
      form.username = d.username || ''
      form.nickname = d.nickname || ''
      form.email = d.email || ''
      form.level = d.level || 'B1'
      form.targetExam = d.targetExam || 'GENERAL'
      form.dailyGoal = d.dailyGoal ?? 30
    }
  } catch {
    ElMessage.error('加载个人信息失败')
  }
}

const fetchPlan = async () => {
  loadingPlan.value = true
  try {
    const res = await axios.get('/api/plan/current')
    if (res.data.code === 200) {
      weeklyPlan.value = res.data.data
      const today = weeklyPlan.value?.days?.find(d => d.today)
      expandedDays.value = today ? [today.date] : []
    }
  } catch {
    weeklyPlan.value = null
  } finally {
    loadingPlan.value = false
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await axios.put('/api/profile', {
      nickname: form.nickname,
      email: form.email,
      level: form.level,
      targetExam: form.targetExam,
      dailyGoal: form.dailyGoal
    })
    if (res.data.code === 200) {
      userInfo.value = { ...res.data.data }
      ElMessage.success('学习档案已保存，周计划已更新')
      await fetchPlan()
    } else {
      ElMessage.error(res.data.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const regeneratePlan = async () => {
  generating.value = true
  try {
    const res = await axios.post('/api/plan/generate')
    if (res.data.code === 200) {
      weeklyPlan.value = res.data.data
      ElMessage.success('本周计划已重新生成')
    } else {
      ElMessage.error(res.data.message || '生成失败')
    }
  } catch {
    ElMessage.error('生成计划失败')
  } finally {
    generating.value = false
  }
}

const toggleComplete = async (item: PlanItem, checked: boolean) => {
  if (!checked || item.completed) return
  try {
    const res = await axios.patch(`/api/plan/item/${item.id}/complete`)
    if (res.data.code === 200) {
      item.completed = true
      if (weeklyPlan.value) {
        weeklyPlan.value.completedCount = weeklyPlan.value.days
          .flatMap(d => d.items)
          .filter(i => i.completed).length
      }
    }
  } catch {
    ElMessage.error('标记完成失败')
  }
}

onMounted(async () => {
  await fetchProfile()
  await fetchPlan()
})
</script>

<style scoped>
.user-container { padding: 10px; }
.profile-card { text-align: center; }
.avatar-wrapper { margin: 20px 0; }
.user-info h2 { margin: 10px 0 5px; color: #303133; }
.user-info p { color: #909399; margin: 0 0 15px; font-size: 14px; }
.user-stats { display: flex; justify-content: space-around; margin-top: 20px; text-align: center; flex-wrap: wrap; gap: 12px; }
.stat-num { font-size: 16px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 12px; color: #909399; margin-top: 5px; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.plan-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.plan-meta { font-size: 13px; color: #909399; }
.day-title { display: flex; align-items: center; gap: 8px; }
.plan-item { padding: 6px 0; border-bottom: 1px dashed #f0f0f0; }
.plan-item:last-child { border-bottom: none; }
.item-minutes { color: #909399; font-size: 12px; margin-left: 8px; }
</style>
