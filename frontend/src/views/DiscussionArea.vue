<template>
  <div class="discussion-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>讨论区</span>
          <el-button type="primary" icon="ChatLineRound" @click="dialogVisible = true">发表评论</el-button>
        </div>
      </template>
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="40">{{ comment.user[0] }}</el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-user">{{ comment.user }}</span>
            <span class="comment-time">{{ comment.time }}</span>
          </div>
          <p class="comment-text">{{ comment.text }}</p>
          <div class="comment-actions">
            <el-button link type="primary" size="small" @click="handleLike(comment)">点赞 ({{ comment.likes || 0 }})</el-button>
            <el-button link type="info" size="small" @click="handleReply(comment)">回复</el-button>
            <el-button v-if="currentRole === 'Admin'" link type="danger" size="small" @click="handleDelete(comment)">删除此条</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新评论" width="500px">
      <el-input v-model="newComment" type="textarea" :rows="4" placeholder="写下你的想法..." />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const currentRole = inject<any>('currentRole', ref('User'))
const dialogVisible = ref(false)
const newComment = ref('')
const loading = ref(false)

const comments = ref<any[]>([])

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/discussion/list')
    if (res.data.code === 200) {
      comments.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('无法加载讨论内容')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchComments()
})

const submitComment = async () => {
  if (!newComment.value) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  
  try {
    const res = await axios.post('/api/discussion/add', { text: newComment.value })
    if (res.data.code === 200) {
      ElMessage.success('发布成功')
      dialogVisible.value = false
      newComment.value = ''
      fetchComments() // Re-fetch to get the new comment with real ID & User info
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('无法连接服务')
  }
}

const handleLike = (comment: any) => {
  comment.likes = (comment.likes || 0) + 1
  ElMessage.success('点赞成功')
}

const handleReply = (comment: any) => {
  ElMessage.info(`正在回复 ${comment.user} 的评论...`)
  dialogVisible.value = true
}

const handleDelete = (comment: any) => {
  ElMessageBox.confirm('确定要删除这条讨论内容吗？', '管理操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/discussion/${comment.id}`)
      if (res.data.code === 200) {
        ElMessage.success('已移除该评论')
        fetchComments()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，删除失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.discussion-container { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.comment-item { display: flex; gap: 15px; margin-bottom: 20px; border-bottom: 1px solid #ebeef5; padding-bottom: 15px; }
.comment-content { flex: 1; }
.comment-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.comment-user { font-weight: bold; color: #303133; }
.comment-time { font-size: 12px; color: #909399; }
.comment-text { font-size: 14px; color: #606266; line-height: 1.5; margin: 0; }
</style>
