<template>
  <el-tag v-if="variant === 'tag'" type="primary" size="small" effect="dark" class="whisper-tag">
    Whisper-WER
  </el-tag>

  <span v-else-if="variant === 'menu'" class="whisper-menu-badge" title="已接入 Whisper-WER 发音评测">Whi</span>

  <div v-else-if="variant === 'banner'" class="whisper-banner">
    <div class="whisper-banner-icon">
      <el-icon :size="20"><Microphone /></el-icon>
    </div>
    <div class="whisper-banner-body">
      <div class="whisper-banner-title">
        Whisper 发音评测已接入
        <el-tag type="primary" size="small" effect="plain" class="whisper-inline-tag">Whisper-WER</el-tag>
      </div>
      <div class="whisper-banner-desc">{{ moduleHint }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'tag' | 'banner' | 'menu'
    module?: 'oral'
  }>(),
  {
    variant: 'tag',
    module: 'oral',
  }
)

const HINTS: Record<string, string> = {
  oral: '录音朗读英文参考句，由 Whisper 转写后与参考文本对齐，通过 WER 计算 0–100 发音得分（Common Voice 标定）。',
}

const moduleHint = computed(() => HINTS[props.module] || HINTS.oral)
</script>

<style scoped>
.whisper-tag {
  font-weight: 600;
  letter-spacing: 0.3px;
}

.whisper-menu-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 16px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 100%);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  vertical-align: middle;
  flex-shrink: 0;
}

.whisper-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border: 1px solid #b3d8ff;
}

.whisper-banner-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  flex-shrink: 0;
}

.whisper-banner-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.whisper-banner-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.whisper-inline-tag {
  font-weight: 600;
}
</style>
