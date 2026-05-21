<template>
  <div class="admin-dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-header">{{ item.title }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>平台学习时长趋势</template>
          <div ref="userChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>学习模块分布</template>
          <div ref="contentChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const stats = ref([
  { title: '总注册用户数', value: '-' },
  { title: '今日活跃用户', value: '-' },
  { title: '学习记录总数', value: '-' },
  { title: '覆盖模块数', value: '-' }
])

const userChartRef = ref<HTMLElement | null>(null)
const contentChartRef = ref<HTMLElement | null>(null)

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/stats', { params: { scope: 'global' } })
    if (res.data.code !== 200) return

    const data = res.data.data
    const summary = data.summary ?? {}

    stats.value = [
      { title: '总注册用户数', value: String(summary.totalUsers ?? '-') },
      { title: '今日活跃用户', value: String(summary.todayActiveUsers ?? '-') },
      { title: '学习记录总数', value: String(summary.totalRecords ?? '-') },
      { title: '覆盖模块数', value: String(summary.moduleTypes ?? '-') }
    ]

    if (userChartRef.value) {
      const chart = echarts.init(userChartRef.value)
      const durationTrend = data.durationTrend ?? { categories: [], values: [] }
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: durationTrend.categories },
        yAxis: { type: 'value', name: '小时' },
        series: [{ name: '学习时长', data: durationTrend.values, type: 'line', smooth: true, areaStyle: {} }]
      })
    }

    if (contentChartRef.value) {
      const chart = echarts.init(contentChartRef.value)
      const distribution = data.moduleDistribution ?? []
      chart.setOption({
        tooltip: { trigger: 'item' },
        series: [
          {
            type: 'pie',
            radius: '50%',
            data: distribution.length > 0 ? distribution : [{ name: '暂无数据', value: 0 }]
          }
        ]
      })
    }
  } catch {
    // keep defaults
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.admin-dashboard-container { padding: 10px; }
.stat-card { text-align: center; }
.stat-header { color: #909399; font-size: 14px; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
</style>
