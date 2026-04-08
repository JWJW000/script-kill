<template>
  <div class="my-reviews-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">我的评价</h2>
    </div>

    <div class="review-list fade-in-up stagger-1">
      <div v-for="review in myReviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="review-script">{{ review.scriptName }}</div>
          <div class="review-actions" v-if="canModifyReview(review)">
            <button class="edit-btn" @click="openEditReview(review)">修改</button>
            <button class="delete-btn" @click="handleDeleteReview(review)">删除</button>
          </div>
        </div>
        <div class="review-ratings">
          <div class="rating-item">
            <span class="rating-label">剧本</span>
            <el-rate :model-value="review.scriptRating" disabled size="small" />
          </div>
          <div class="rating-item">
            <span class="rating-label">主持</span>
            <el-rate :model-value="review.hostRating" disabled size="small" />
          </div>
        </div>
        <div class="review-content">
          <p v-if="review.scriptComment" class="review-comment">{{ review.scriptComment }}</p>
          <p v-if="review.hostComment" class="review-comment host">主持评价：{{ review.hostComment }}</p>
        </div>
        <div class="review-time">
          {{ formatTime(review.createTime) }}
          <span v-if="!canModifyReview(review)" class="time-expired">(已超过24小时，无法修改)</span>
        </div>
      </div>
      <div v-if="myReviews.length === 0" class="empty-state">
        <el-empty description="暂无评价记录" />
      </div>
    </div>

    <!-- 修改评价弹窗 -->
    <el-dialog v-model="showEditDialog" title="修改评价" width="450px">
      <el-form v-if="editingReview" :model="reviewForm" label-position="top">
        <el-form-item label="剧本评分">
          <el-rate v-model="reviewForm.scriptRating" allow-half />
        </el-form-item>
        <el-form-item label="剧本评价">
          <el-input v-model="reviewForm.scriptComment" type="textarea" :rows="3" placeholder="分享你的游戏体验..." />
        </el-form-item>
        <el-form-item label="主持评分">
          <el-rate v-model="reviewForm.hostRating" allow-half />
        </el-form-item>
        <el-form-item label="主持评价">
          <el-input v-model="reviewForm.hostComment" type="textarea" :rows="2" placeholder="对主持人的评价..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEditReview">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReviews, updateReview, deleteReview } from '@/api/review'

const myReviews = ref([])
const showEditDialog = ref(false)
const editingReview = ref(null)
const reviewForm = reactive({
  scriptRating: 5,
  hostRating: 5,
  scriptComment: '',
  hostComment: ''
})

const loadReviews = async () => {
  try {
    const res = await getMyReviews()
    if (res.code === 200) myReviews.value = res.data || []
  } catch (e) { console.error(e) }
}

const canModifyReview = (review) => {
  if (!review.createTime) return false
  const createTime = new Date(review.createTime)
  const now = new Date()
  const hoursDiff = (now - createTime) / (1000 * 60 * 60)
  return hoursDiff < 24
}

const openEditReview = (review) => {
  editingReview.value = review
  reviewForm.scriptRating = review.scriptRating
  reviewForm.hostRating = review.hostRating
  reviewForm.scriptComment = review.scriptComment || ''
  reviewForm.hostComment = review.hostComment || ''
  showEditDialog.value = true
}

const confirmEditReview = async () => {
  if (!editingReview.value) return
  try {
    const res = await updateReview(editingReview.value.id, {
      scriptRating: reviewForm.scriptRating,
      hostRating: reviewForm.hostRating,
      scriptComment: reviewForm.scriptComment,
      hostComment: reviewForm.hostComment
    })
    if (res.code === 200) {
      ElMessage.success('评价已更新')
      showEditDialog.value = false
      loadReviews()
    }
  } catch (e) { ElMessage.error(e.message || '修改失败') }
}

const handleDeleteReview = async (review) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteReview(review.id)
    if (res.code === 200) {
      ElMessage.success('评价已删除')
      loadReviews()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

onMounted(() => { loadReviews() })
</script>

<style scoped lang="scss">
.my-reviews-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
  .page-title {
    font-size: 24px;
    font-weight: 800;
    color: var(--sk-text-primary);
  }
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .review-card {
    padding: 20px 24px;
    background: var(--sk-bg-card);
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    transition: all 0.3s;

    &:hover {
      border-color: var(--sk-border-glow);
    }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .review-script {
        font-size: 16px;
        font-weight: 700;
        color: var(--sk-text-primary);
      }

      .review-actions {
        display: flex;
        gap: 8px;

        .edit-btn, .delete-btn {
          padding: 6px 14px;
          border-radius: 6px;
          font-size: 13px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.3s;
          font-family: var(--sk-font-body);
        }

        .edit-btn {
          background: rgba(0, 153, 204, 0.1);
          border: 1px solid rgba(0, 153, 204, 0.25);
          color: var(--sk-neon-cyan);
          &:hover { background: rgba(0, 153, 204, 0.2); }
        }

        .delete-btn {
          background: rgba(204, 34, 68, 0.1);
          border: 1px solid rgba(204, 34, 68, 0.25);
          color: var(--sk-neon-red);
          &:hover { background: rgba(204, 34, 68, 0.2); }
        }
      }
    }

    .review-ratings {
      display: flex;
      gap: 24px;
      margin-bottom: 12px;

      .rating-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .rating-label {
          font-size: 13px;
          color: var(--sk-text-muted);
        }
      }
    }

    .review-content {
      .review-comment {
        font-size: 14px;
        color: var(--sk-text-secondary);
        line-height: 1.6;
        margin-bottom: 6px;
        &.host {
          color: var(--sk-text-muted);
          font-size: 13px;
        }
      }
    }

    .review-time {
      font-size: 12px;
      color: var(--sk-text-muted);
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid var(--sk-border);

      .time-expired {
        margin-left: 8px;
        opacity: 0.7;
      }
    }
  }
}

.empty-state {
  padding: 60px;
}
</style>
