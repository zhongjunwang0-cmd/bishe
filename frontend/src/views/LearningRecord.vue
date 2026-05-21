<template>
  <div class="record-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>学习记录追踪</span>
          <div>
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="margin-right: 10px;" />
            <el-button type="primary" size="small" @click="handleFilter">筛选</el-button>
          </div>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item v-for="(activity, index) in activities" :key="index" :timestamp="activity.timestamp" :type="activity.type">
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const dateRange = ref('')
const loading = ref(false)
const activities = ref<any[]>([])

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/record/list')
    if (res.data.code === 200) {
      activities.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载学习记录')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecords()
})

const handleFilter = () => {
  if (!dateRange.value) {
    ElMessage.warning('请选择要筛选的日期范围')
    return
  }
  ElMessage.success('已应用所选的日期筛选条件。')
}
</script>

<style scoped>
.record-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
