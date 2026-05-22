<template>
  <div class="teaching-management-container">
    <el-card shadow="hover" class="intro-card">
      <div class="intro-title">教学管理</div>
      <div class="intro-sub">发布与管理教学内容、测试题库，查看学生学习情况</div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6" v-for="item in modules" :key="item.path">
        <el-card shadow="hover" class="module-card" @click="goTo(item.path)">
          <el-icon class="module-icon"><component :is="item.icon" /></el-icon>
          <div class="module-name">{{ item.name }}</div>
          <div class="module-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const modules = [
  { name: '词汇内容管理', desc: '维护词汇库与学习素材', icon: 'Collection', path: '/admin/vocab' },
  { name: '语法内容管理', desc: '编辑语法知识点与练习', icon: 'Notebook', path: '/grammar' },
  { name: '测试题库管理', desc: '管理阅读、听力、填空题库', icon: 'DocumentChecked', path: '/admin/tests' },
  { name: '阅读测试生成', desc: '生成阅读理解测试内容', icon: 'DocumentCopy', path: '/reading' }
]

const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.teaching-management-container { padding: 10px; }
.intro-card { background: linear-gradient(135deg, #e6a23c, #c77c1a); color: #fff; }
.intro-title { font-size: 22px; font-weight: 700; }
.intro-sub { margin-top: 8px; font-size: 14px; opacity: 0.9; }
.module-card { text-align: center; cursor: pointer; transition: all 0.3s; min-height: 160px; }
.module-card:hover { transform: translateY(-5px); }
.module-icon { font-size: 34px; color: #409eff; margin-bottom: 10px; }
.module-name { font-size: 15px; font-weight: 600; color: #303133; }
.module-desc { margin-top: 8px; font-size: 13px; color: #909399; line-height: 1.5; }
</style>
