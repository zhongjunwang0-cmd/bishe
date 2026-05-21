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
          <template #header>用户增长趋势</template>
          <div ref="userChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>内容模块分布</template>
          <div ref="contentChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const stats = [
  { title: '总注册用户数', value: '15,280' },
  { title: '今日活跃用户', value: '3,415' },
  { title: '总题库数量', value: '8,156' },
  { title: '总讨论互动数', value: '98,100' }
]

const userChartRef = ref<HTMLElement | null>(null)
const contentChartRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (userChartRef.value) {
    const chart = echarts.init(userChartRef.value)
    chart.setOption({
      xAxis: { type: 'category', data: ['一月', '二月', '三月', '四月', '五月', '六月'] },
      yAxis: { type: 'value' },
      series: [{ data: [820, 932, 901, 934, 1290, 1330], type: 'line', smooth: true }]
    })
  }
  if (contentChartRef.value) {
    const chart = echarts.init(contentChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: [
            { value: 1048, name: '阅读理解' },
            { value: 735, name: '听力训练' },
            { value: 580, name: '口语练习' },
            { value: 484, name: '选词填空' },
            { value: 300, name: '词汇通关' }
          ]
        }
      ]
    })
  }
})
</script>

<style scoped>
.admin-dashboard-container { padding: 10px; }
.stat-card { text-align: center; }
.stat-header { color: #909399; font-size: 14px; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
</style>
