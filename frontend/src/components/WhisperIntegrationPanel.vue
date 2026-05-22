<template>
  <el-card shadow="hover" class="whisper-panel">
    <template #header>
      <div class="panel-header">
        <span><el-icon><Microphone /></el-icon> 语音模型接入</span>
        <el-tag type="primary" size="small" effect="dark">Whisper-WER</el-tag>
      </div>
    </template>
    <p class="panel-intro">
      口语练习已集成 Whisper ASR + WER 发音评测（Common Voice 数据集标定）：
    </p>
    <div class="module-list">
      <div class="module-item" @click="$router.push('/oral')">
        <div class="module-name">
          <el-icon><Microphone /></el-icon>
          口语练习
        </div>
        <WhisperModelBadge variant="tag" />
      </div>
    </div>
    <div class="panel-footer">
      <span class="dataset-label">标定数据：</span>
      <span>Common Voice · Whisper · WER 发音评分</span>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import WhisperModelBadge from './WhisperModelBadge.vue'
</script>

<style scoped>
.whisper-panel :deep(.el-card__header) {
  padding: 12px 16px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
.panel-header .el-icon {
  vertical-align: -2px;
  margin-right: 4px;
}
.panel-intro {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px;
}
.module-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.module-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  cursor: pointer;
  transition: background 0.2s;
}
.module-item:hover {
  background: #d9ecff;
}
.module-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
}
.panel-footer {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #909399;
}
.dataset-label {
  color: #606266;
  font-weight: 500;
}
</style>
