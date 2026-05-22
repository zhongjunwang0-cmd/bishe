<template>
  <div class="record-container">
    <el-card shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>学习记录追踪</span>
          <div>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              value-format="YYYY-MM-DD"
              style="margin-right: 10px;"
            />
            <el-button type="primary" size="small" @click="handleFilter">筛选</el-button>
            <el-button size="small" @click="handleReset">重置</el-button>
          </div>
        </div>
      </template>
      <el-empty v-if="!loading && activities.length === 0" description="暂无学习记录" />
      <el-timeline v-else>
        <el-timeline-item v-for="(activity, index) in activities" :key="index" :timestamp="activity.timestamp" :type="activity.type">
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const dateRange = ref<string[]>([])
const loading = ref(false)
const activities = ref<any[]>([])

const formatDateParam = (value: string | Date) => {
  if (typeof value === 'string') return value.slice(0, 10)
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const fetchRecords = async (range?: string[]) => {
  loading.value = true
  try {
    const scope = (currentRole.value === 'Admin' || currentRole.value === 'Teacher') ? 'global' : 'personal'
    const params: Record<string, string> = { scope }
    if (range?.length === 2) {
      params.startDate = formatDateParam(range[0])
      params.endDate = formatDateParam(range[1])
    }
    const res = await axios.get('/api/record/list', { params })
    if (res.data.code === 200) {
      activities.value = res.data.data || []
    }
  } catch {
    ElMessage.error('无法加载学习记录')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecords()
})

const handleFilter = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择要筛选的日期范围')
    return
  }
  fetchRecords(dateRange.value)
}

const handleReset = () => {
  dateRange.value = []
  fetchRecords()
}
</script>

<style scoped>
.record-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
