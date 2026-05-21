<template>
  <div class="vocab-learning">
    <!-- 统计概览 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>词汇学习与复习</span>
          <el-tag type="info" size="small">艾宾浩斯间隔：1 → 2 → 4 → 7 → 15 → 30 天</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 词库浏览 -->
        <el-tab-pane label="词库浏览" name="browse">
          <el-table :data="vocabList" v-loading="loadingBrowse" style="width: 100%">
            <el-table-column prop="word" label="单词" width="160">
              <template #default="{ row }">
                <span class="word-text">{{ row.word }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="phonetic" label="音标" width="140" />
            <el-table-column prop="translation" label="释义" />
            <el-table-column prop="level" label="难度" width="100">
              <template #default="{ row }">
                <el-tag :type="levelTag(row.level)" size="small">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="!bookSet.has(row.id)"
                  link type="primary"
                  @click="addToBook(row)"
                >加入生词本</el-button>
                <el-tag v-else type="success" size="small">已在生词本</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 我的生词本 -->
        <el-tab-pane label="我的生词本" name="book">
          <el-empty v-if="!bookList.length && !loadingBook" description="生词本为空，请从词库添加单词" />
          <el-table v-else :data="bookList" v-loading="loadingBook" style="width: 100%">
            <el-table-column prop="word" label="单词" width="160">
              <template #default="{ row }">
                <span class="word-text">{{ row.word }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="translation" label="释义" />
            <el-table-column label="掌握度" width="180">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.masteryLevel || 0"
                  :color="masteryColor(row.masteryLevel)"
                  :stroke-width="14"
                  :text-inside="true"
                />
              </template>
            </el-table-column>
            <el-table-column label="复习阶段" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small">第 {{ (row.reviewStage || 0) + 1 }} 轮</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下次复习" width="170">
              <template #default="{ row }">
                {{ formatTime(row.nextReviewTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" @click="removeFromBook(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 今日复习 -->
        <el-tab-pane name="review">
          <template #label>
            <span>今日复习</span>
            <el-badge v-if="stats.dueToday > 0" :value="stats.dueToday" class="review-badge" />
          </template>

          <div v-if="reviewDone" class="review-done">
            <el-result icon="success" title="今日复习完成！" sub-title="记忆闭环已更新，明天继续加油">
              <template #extra>
                <el-button type="primary" @click="restartReview">再来一轮</el-button>
              </template>
            </el-result>
          </div>

          <div v-else-if="!reviewQueue.length && !loadingReview" class="review-empty">
            <el-empty description="暂无待复习单词">
              <el-button type="primary" @click="activeTab = 'browse'">去词库添加</el-button>
            </el-empty>
          </div>

          <div v-else class="flashcard-area" v-loading="loadingReview">
            <div class="review-progress">
              <span>{{ reviewIndex + 1 }} / {{ reviewQueue.length }}</span>
              <el-progress
                :percentage="Math.round(((reviewIndex) / reviewQueue.length) * 100)"
                :show-text="false"
                style="flex:1; margin-left: 16px;"
              />
            </div>

            <div class="flashcard" @click="flipCard">
              <div :class="['card-inner', { flipped: cardFlipped }]">
                <div class="card-front">
                  <div class="card-word">{{ currentCard?.word }}</div>
                  <div class="card-phonetic">{{ currentCard?.phonetic }}</div>
                  <div class="card-hint">点击卡片查看释义</div>
                </div>
                <div class="card-back">
                  <div class="card-translation">{{ currentCard?.translation }}</div>
                  <div class="card-example" v-if="currentCard?.example">{{ currentCard.example }}</div>
                  <div class="card-meta">
                    <el-tag size="small">掌握度 {{ currentCard?.masteryLevel || 0 }}%</el-tag>
                    <el-tag size="small" type="warning">第 {{ (currentCard?.reviewStage || 0) + 1 }} 轮</el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="review-actions" v-show="cardFlipped">
              <el-button type="danger" size="large" @click="submitReview(false)">
                <el-icon><Close /></el-icon> 忘记了
              </el-button>
              <el-button type="success" size="large" @click="submitReview(true)">
                <el-icon><Check /></el-icon> 记住了
              </el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

interface VocabItem {
  id: number
  word: string
  phonetic: string
  translation: string
  example: string
  level: string
}

interface BookItem extends VocabItem {
  masteryLevel: number
  reviewStage: number
  status: string
  nextReviewTime: string
  lastReviewTime: string
  reviewCount: number
  vocabId: number
}

const activeTab = ref('browse')
const loadingBrowse = ref(false)
const loadingBook = ref(false)
const loadingReview = ref(false)

const vocabList = ref<VocabItem[]>([])
const bookList = ref<BookItem[]>([])
const reviewQueue = ref<BookItem[]>([])
const bookSet = ref(new Set<number>())

const stats = reactive({ total: 0, dueToday: 0, mastered: 0, learning: 0 })

const reviewIndex = ref(0)
const cardFlipped = ref(false)
const reviewDone = ref(false)

const currentCard = computed(() => reviewQueue.value[reviewIndex.value] || null)

const statCards = computed(() => [
  { key: 'total', label: '生词本总量', value: stats.total },
  { key: 'dueToday', label: '今日待复习', value: stats.dueToday },
  { key: 'learning', label: '学习中', value: stats.learning },
  { key: 'mastered', label: '已掌握', value: stats.mastered },
])

const fetchStats = async () => {
  try {
    const res = await axios.get('/api/vocab/book/stats')
    if (res.data.code === 200) {
      Object.assign(stats, res.data.data)
    }
  } catch { /* ignore */ }
}

const fetchVocabList = async () => {
  loadingBrowse.value = true
  try {
    const res = await axios.get('/api/vocab/list')
    if (res.data.code === 200) {
      vocabList.value = res.data.data
    }
  } catch {
    ElMessage.error('加载词库失败')
  } finally {
    loadingBrowse.value = false
  }
}

const fetchBookList = async () => {
  loadingBook.value = true
  try {
    const res = await axios.get('/api/vocab/book/list')
    if (res.data.code === 200) {
      bookList.value = res.data.data
      bookSet.value = new Set(bookList.value.map(b => b.vocabId))
    }
  } catch {
    ElMessage.error('加载生词本失败')
  } finally {
    loadingBook.value = false
  }
}

const fetchReviewQueue = async () => {
  loadingReview.value = true
  reviewDone.value = false
  reviewIndex.value = 0
  cardFlipped.value = false
  try {
    const res = await axios.get('/api/vocab/review/today')
    if (res.data.code === 200) {
      reviewQueue.value = res.data.data
      if (!reviewQueue.value.length) {
        reviewDone.value = false
      }
    }
  } catch {
    ElMessage.error('加载复习队列失败')
  } finally {
    loadingReview.value = false
  }
}

const addToBook = async (row: VocabItem) => {
  try {
    const res = await axios.post(`/api/vocab/book/add/${row.id}`)
    if (res.data.code === 200) {
      ElMessage.success(`"${row.word}" 已加入生词本`)
      bookSet.value.add(row.id)
      await fetchStats()
      if (activeTab.value === 'book') await fetchBookList()
    } else {
      ElMessage.warning(res.data.message)
    }
  } catch {
    ElMessage.error('添加失败')
  }
}

const removeFromBook = (row: BookItem) => {
  ElMessageBox.confirm(`确定将 "${row.word}" 移出生词本？`, '提示', { type: 'warning' })
    .then(async () => {
      await axios.delete(`/api/vocab/book/remove/${row.vocabId}`)
      ElMessage.success('已移除')
      bookSet.value.delete(row.vocabId)
      await fetchBookList()
      await fetchStats()
    })
    .catch(() => {})
}

const flipCard = () => {
  cardFlipped.value = !cardFlipped.value
}

const submitReview = async (remembered: boolean) => {
  const card = currentCard.value
  if (!card) return
  try {
    const res = await axios.post('/api/vocab/review/submit', {
      vocabId: card.vocabId,
      remembered,
    })
    if (res.data.code === 200) {
      const msg = remembered ? '很好，记忆加固成功！' : '没关系，下次会再出现'
      ElMessage({ message: msg, type: remembered ? 'success' : 'warning', duration: 1500 })
      cardFlipped.value = false
      if (reviewIndex.value < reviewQueue.value.length - 1) {
        reviewIndex.value++
      } else {
        reviewDone.value = true
        await fetchStats()
        await fetchBookList()
      }
    }
  } catch {
    ElMessage.error('提交复习结果失败')
  }
}

const restartReview = () => {
  fetchReviewQueue()
}

const onTabChange = (tab: string) => {
  if (tab === 'book') fetchBookList()
  if (tab === 'review') fetchReviewQueue()
}

const levelTag = (level: string) => {
  if (level === 'CET-4') return 'info'
  if (level === 'CET-6') return 'warning'
  return 'danger'
}

const masteryColor = (level: number) => {
  if (level >= 85) return '#67c23a'
  if (level >= 50) return '#e6a23c'
  return '#f56c6c'
}

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    NEW: '新词', LEARNING: '学习中', REVIEWING: '巩固中', MASTERED: '已掌握',
  }
  return map[status] || status
}

const statusTag = (status: string) => {
  const map: Record<string, string> = {
    NEW: 'info', LEARNING: '', REVIEWING: 'warning', MASTERED: 'success',
  }
  return map[status] || 'info'
}

const formatTime = (t: string | null) => {
  if (!t) return '立即复习'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  await Promise.all([fetchVocabList(), fetchBookList(), fetchStats()])
})
</script>

<style scoped>
.vocab-learning { padding: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats-row { margin-bottom: 0; }
.stat-card { text-align: center; padding: 8px 0; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.word-text { font-weight: bold; color: #409eff; }
.review-badge { margin-left: 6px; }

.flashcard-area { max-width: 560px; margin: 24px auto; }
.review-progress { display: flex; align-items: center; margin-bottom: 20px; font-size: 14px; color: #606266; }

.flashcard {
  perspective: 1000px;
  cursor: pointer;
  height: 280px;
  margin-bottom: 24px;
}
.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
  transform-style: preserve-3d;
}
.card-inner.flipped { transform: rotateY(180deg); }
.card-front, .card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}
.card-front {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.card-back {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
  transform: rotateY(180deg);
}
.card-word { font-size: 36px; font-weight: bold; }
.card-phonetic { font-size: 16px; opacity: 0.85; margin-top: 8px; }
.card-hint { font-size: 13px; opacity: 0.6; margin-top: 24px; }
.card-translation { font-size: 22px; font-weight: 600; text-align: center; }
.card-example { font-size: 14px; opacity: 0.85; margin-top: 16px; text-align: center; font-style: italic; }
.card-meta { display: flex; gap: 8px; margin-top: 20px; }

.review-actions { display: flex; justify-content: center; gap: 24px; }
.review-done, .review-empty { padding: 40px 0; }
</style>
