<template>
  <div class="system-settings-container">
    <el-card shadow="hover">
      <template #header>系统参数设置</template>
      <el-form :model="form" label-width="150px" style="max-width: 600px">
        <el-form-item label="站点名称">
          <el-input v-model="form.siteName" />
        </el-form-item>
        <el-form-item label="系统公告">
          <el-input v-model="form.announcement" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="允许新用户注册">
          <el-switch v-model="form.allowRegistration" />
        </el-form-item>
        <el-form-item label="AI 推荐引擎模块">
          <el-switch v-model="form.enableAiEngine" />
        </el-form-item>
        <el-form-item label="最大上传文件大小(MB)">
          <el-input-number v-model="form.maxUploadSize" :min="1" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button>重置默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const form = ref({
  siteName: '英语学习系统',
  announcement: '欢迎使用全新的英语学习系统！',
  allowRegistration: true,
  enableAiEngine: true,
  maxUploadSize: 10
})

const saveSettings = () => {
  ElMessage.success('系统设置保存成功')
}
</script>

<style scoped>
.system-settings-container { padding: 10px; }
</style>
