<template>
  <div class="tools-integration-container">
    <el-card shadow="hover">
      <template #header>系统工具与 API 集成</template>
      <el-alert title="启用外部 AI 后，AI 问答将优先调用配置的 API；若连接失败则自动降级为内置规则引擎。" type="info" show-icon style="margin-bottom: 20px;" />
      <el-alert title="注意：修改 API 配置可能导致系统部分功能不可用，请谨慎操作。" type="warning" show-icon style="margin-bottom: 20px;" />
      
      <el-form :model="form" label-width="150px" style="max-width: 600px" v-loading="loading">
        <el-divider content-position="left">AI 辅助接口配置 (DeepSeek / GPT 等 OpenAI 兼容)</el-divider>
        <el-form-item label="启用外部 AI">
          <el-switch v-model="form.aiEnabled" />
        </el-form-item>
        <el-form-item label="API Endpoint">
          <el-input v-model="form.aiEndpoint" :disabled="!form.aiEnabled" placeholder="https://api.deepseek.com/v1/chat/completions" />
        </el-form-item>
        <el-form-item label="Model">
          <el-input v-model="form.aiModel" :disabled="!form.aiEnabled" placeholder="deepseek-chat / gpt-4o-mini" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.aiApiKey" type="password" show-password :disabled="!form.aiEnabled" placeholder="保存后仅显示末四位" />
        </el-form-item>

        <el-divider content-position="left">语音评测接口配置</el-divider>
        <el-form-item label="启用语音评测">
          <el-switch v-model="form.voiceEnabled" />
        </el-form-item>
        <el-form-item label="提供商">
          <el-select v-model="form.voiceProvider" placeholder="选择提供商" :disabled="!form.voiceEnabled" style="width: 100%">
             <el-option label="Azure Cognitive Services" value="azure" />
             <el-option label="Google Cloud Speech" value="google" />
             <el-option label="科大讯飞" value="iflytek" />
          </el-select>
        </el-form-item>
        <el-form-item label="Access Token">
          <el-input v-model="form.voiceToken" type="password" show-password :disabled="!form.voiceEnabled" />
        </el-form-item>

        <el-form-item style="margin-top: 30px;">
          <el-button type="primary" @click="saveIntegration" :loading="saving">保存配置并测试连接</el-button>
          <el-button @click="resetDefaults">重置默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const defaultForm = () => ({
  aiEnabled: false,
  aiEndpoint: 'https://api.deepseek.com/v1/chat/completions',
  aiModel: 'deepseek-chat',
  aiApiKey: '',
  voiceEnabled: false,
  voiceProvider: 'azure',
  voiceToken: ''
})

const form = ref(defaultForm())

const loading = ref(false)
const saving = ref(false)

const fetchConfig = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/system/config')
    if (res.data.code === 200 && res.data.data) {
      form.value = { ...defaultForm(), ...res.data.data }
    }
  } catch (error) {
    ElMessage.error('无法加载系统配置')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchConfig()
})

const saveIntegration = async () => {
  saving.value = true
  try {
    const res = await axios.put('/api/admin/system/config', form.value)
    if (res.data.code === 200) {
      const msg = res.data.data?.message || '配置已保存并测试成功！'
      ElMessage.success(msg)
      await fetchConfig()
    } else {
      ElMessage.warning(res.data.message || '配置保存失败')
      await fetchConfig()
    }
  } catch (error: any) {
    const msg = error.response?.data?.message || '网络错误，无法保存配置'
    ElMessage.warning(msg)
    await fetchConfig()
  } finally {
    saving.value = false
  }
}

const resetDefaults = () => {
  form.value = defaultForm()
  ElMessage.info('已恢复默认配置内容，请记得点击保存')
}
</script>

<style scoped>
.tools-integration-container { padding: 10px; }
</style>
