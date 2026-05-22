<template>
  <el-card shadow="hover" class="ai-panel">
    <template #header>
      <div class="panel-header">
        <span><el-icon><Cpu /></el-icon> AI 模型接入</span>
        <div class="header-tags">
          <el-tag type="warning" size="small" effect="dark">T5-GEC</el-tag>
          <el-tag type="primary" size="small" effect="dark">Whisper-WER</el-tag>
        </div>
      </div>
    </template>

    <section class="model-section">
      <p class="section-label">语法纠错 · JFLEG · T5-small</p>
      <p class="panel-intro">
        以下模块已集成 JFLEG 数据集微调的 T5-small 语法纠错模型（Seq2Seq GEC）：
      </p>
      <div class="module-list">
        <div
          v-for="item in t5Modules"
          :key="item.path"
          class="module-item module-item--t5"
          @click="$router.push(item.path)"
        >
          <div class="module-name">
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.name }}
          </div>
          <T5ModelBadge variant="tag" />
        </div>
      </div>
    </section>

    <section class="model-section model-section--whisper">
      <p class="section-label">发音评测 · Common Voice · Whisper</p>
      <p class="panel-intro">
        口语练习已集成 Whisper ASR + WER 发音评测，朗读参考句后自动打分：
      </p>
      <div class="module-list">
        <div class="module-item module-item--whisper" @click="$router.push('/oral')">
          <div class="module-name">
            <el-icon><Microphone /></el-icon>
            口语练习
          </div>
          <WhisperModelBadge variant="tag" />
        </div>
      </div>
    </section>

    <div class="panel-footer">
      <div><span class="dataset-label">语法纠错：</span>JFLEG · T5-small · Seq2Seq GEC</div>
      <div><span class="dataset-label">发音评测：</span>Common Voice · Whisper-tiny · WER</div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import T5ModelBadge from './T5ModelBadge.vue'
import WhisperModelBadge from './WhisperModelBadge.vue'

const t5Modules = [
  { name: '词汇学习', path: '/vocab', icon: 'Collection' },
  { name: '语法学习', path: '/grammar', icon: 'Notebook' },
  { name: '选词填空', path: '/cloze', icon: 'EditPen' },
  { name: 'AI 辅导学习', path: '/ai-tutoring', icon: 'Service' },
]
</script>

<style scoped>
.ai-panel :deep(.el-card__header) {
  padding: 12px 16px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  gap: 8px;
}
.panel-header .el-icon {
  vertical-align: -2px;
  margin-right: 4px;
}
.header-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}
.model-section {
  margin-bottom: 16px;
}
.model-section--whisper {
  padding-top: 14px;
  border-top: 1px dashed #e4e7ed;
}
.section-label {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  letter-spacing: 0.3px;
}
.panel-intro {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px;
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
  cursor: pointer;
  transition: background 0.2s;
}
.module-item--t5 {
  background: #fdf6ec;
  border: 1px solid #faecd8;
}
.module-item--t5:hover {
  background: #faecd8;
}
.module-item--whisper {
  background: #ecf5ff;
  border: 1px solid #d9ecff;
}
.module-item--whisper:hover {
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
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
.dataset-label {
  color: #606266;
  font-weight: 500;
}
</style>
