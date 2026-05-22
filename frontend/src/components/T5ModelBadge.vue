<template>
  <el-tag v-if="variant === 'tag'" type="warning" size="small" effect="dark" class="t5-tag">
    T5-GEC
  </el-tag>

  <span v-else-if="variant === 'menu'" class="t5-menu-badge" title="已接入 T5 语法纠错">T5</span>

  <div v-else-if="variant === 'banner'" class="t5-banner">
    <div class="t5-banner-icon">
      <el-icon :size="20"><Cpu /></el-icon>
    </div>
    <div class="t5-banner-body">
      <div class="t5-banner-title">
        T5 语法纠错模型已接入
        <el-tag type="warning" size="small" effect="plain" class="t5-inline-tag">T5-GEC</el-tag>
      </div>
      <div class="t5-banner-desc">{{ moduleHint }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'tag' | 'banner' | 'menu'
    module?: 'vocab' | 'grammar' | 'cloze' | 'ai'
  }>(),
  {
    variant: 'tag',
    module: 'grammar',
  }
)

const HINTS: Record<string, string> = {
  vocab: '造句练习调用 JFLEG 微调的 T5-small 模型，实时检验词汇在句子中的语法用法。',
  grammar: '语法纠错与造句练习由 T5-GEC 驱动，基于 JFLEG 英文学员作文纠错数据集微调。',
  cloze: '完成选词填空后，可使用 T5-GEC 对自造句进行语法纠错，巩固语境表达。',
  ai: '发送英文句子进行写作批改时，优先调用 T5-GEC 模型（JFLEG 微调 · Seq2Seq GEC）。',
}

const moduleHint = computed(() => HINTS[props.module] || HINTS.grammar)
</script>

<style scoped>
.t5-tag {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.t5-menu-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 16px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 8px;
  background: linear-gradient(135deg, #e6a23c 0%, #f5c06a 100%);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 1;
  vertical-align: middle;
  flex-shrink: 0;
}

.t5-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #fdf6ec 0%, #fef9f0 100%);
  border: 1px solid #f5dab1;
}

.t5-banner-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #e6a23c;
  color: #fff;
  flex-shrink: 0;
}

.t5-banner-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.t5-banner-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.t5-inline-tag {
  font-weight: 600;
}
</style>
