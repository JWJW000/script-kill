<template>
  <div class="host-reviews">
    <div class="page-header fade-in-up">
      <h2 class="page-title">玩家评价</h2>
    </div>

    <div class="stats-row">
      <div class="stat-card fade-in-up stagger-1">
        <div class="stat-value" style="color: var(--sk-neon-cyan);">{{ statistics.averageRating?.toFixed(1) || '0.0' }}</div>
        <div class="stat-label">平均评分</div>
      </div>
      <div class="stat-card fade-in-up stagger-2">
        <div class="stat-value" style="color: var(--sk-neon-purple);">{{ statistics.totalReviews || 0 }}</div>
        <div class="stat-label">评价数量</div>
      </div>
      <div class="stat-card fade-in-up stagger-3">
        <div class="stat-value" style="color: var(--sk-neon-green);">{{ statistics.goodRate?.toFixed(1) || '0.0' }}%</div>
        <div class="stat-label">好评率</div>
      </div>
    </div>

    <div class="reviews-list fade-in-up stagger-4">
      <div v-for="review in reviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="review-main">
            <span class="reviewer-name">{{ review.userName || '匿名玩家' }}</span>
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
          <div class="review-meta">
            <span v-if="review.scriptName" class="meta-pill">剧本：{{ review.scriptName }}</span>
            <span v-if="review.sessionTime" class="meta-pill">场次：{{ formatTime(review.sessionTime) }}</span>
          </div>
        </div>
        <div class="review-rating">
          <el-rate :model-value="review.hostRating" disabled size="small" />
          <span class="rating-value">{{ review.hostRating }}分</span>
        </div>
        <p class="review-content">{{ review.hostComment || '暂无评价内容' }}</p>
      </div>
      <div v-if="reviews.length === 0" class="empty-state">暂无评价</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReviewList, getHostStatistics } from '@/api/review'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const reviews = ref([])
const statistics = ref({ averageRating: 0, totalReviews: 0, goodRate: 0 })

const loadReviews = async () => {
  try {
    const res = await getReviewList({ hostId: userStore.userInfo?.id })
    if (res.code === 200) reviews.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadStatistics = async () => {
  try {
    if (userStore.userInfo?.id) {
      const res = await getHostStatistics(userStore.userInfo.id)
      if (res.code === 200 && res.data) statistics.value = res.data
    }
  } catch (e) { console.error(e) }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

onMounted(() => { loadReviews(); loadStatistics() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 28px;

  .stat-card {
    text-align: center;
    padding: 28px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    transition: all 0.35s;

    &:hover { border-color: var(--sk-border-glow); transform: translateY(-3px); }

    .stat-value { font-size: 36px; font-weight: 900; font-family: var(--sk-font-display); margin-bottom: 6px; }
    .stat-label { font-size: 13px; color: var(--sk-text-muted); }
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .review-card {
    padding: 20px 24px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.3s;

    &:hover { border-color: var(--sk-border-glow); }

    .review-header {
      display: flex;
      flex-direction: column;
      gap: 4px;
      margin-bottom: 10px;

      .review-main {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .reviewer-name { font-weight: 600; color: var(--sk-text-primary); }
        .review-time { font-size: 12px; color: var(--sk-text-muted); }
      }

      .review-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .meta-pill {
          font-size: 11px;
          color: var(--sk-text-secondary);
          padding: 2px 8px;
          border-radius: 999px;
          border: 1px solid var(--sk-border);
          background: rgba(0, 229, 255, 0.03);
        }
      }
    }

    .review-rating {
      display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
      .rating-value { font-size: 13px; color: var(--sk-neon-amber); font-weight: 600; }
    }

    .review-content { font-size: 14px; color: var(--sk-text-secondary); line-height: 1.7; }
  }
}

.empty-state { text-align: center; padding: 60px; color: var(--sk-text-muted); }
</style>
