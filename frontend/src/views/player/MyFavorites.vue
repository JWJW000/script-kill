<template>
  <div class="my-favorites-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">我的收藏</h2>
    </div>

    <div class="script-list fade-in-up stagger-1">
      <div v-for="script in favorites" :key="script.id" class="script-card" @click="goToScript(script)">
        <div class="script-cover">
          <el-image :src="script.cover" fit="cover" class="cover-img">
            <template #error>
              <div class="cover-fallback">{{ script.name?.charAt(0) }}</div>
            </template>
          </el-image>
        </div>
        <div class="script-info">
          <div class="script-name">{{ script.name }}</div>
          <div class="script-tags">
            <span class="neon-tag neon-tag--cyan">{{ script.type }}</span>
            <span class="neon-tag" :class="'neon-tag--' + difficultyColor(script.difficulty)">
              {{ difficultyText(script.difficulty) }}
            </span>
          </div>
          <div class="script-meta">
            <span>{{ script.minPlayers }}-{{ script.maxPlayers }}人</span>
            <span>{{ script.duration }}分钟</span>
            <span class="price">¥{{ script.price }}</span>
          </div>
        </div>
        <button class="unfavorite-btn" @click.stop="handleUnfavorite(script)">
          <el-icon><Star /></el-icon>
        </button>
      </div>
      <div v-if="favorites.length === 0" class="empty-state">
        <el-empty description="暂无收藏的剧本" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyFavorites, removeFavorite } from '@/api/script'

const router = useRouter()
const favorites = ref([])

const loadFavorites = async () => {
  try {
    const res = await getMyFavorites()
    if (res.code === 200) favorites.value = res.data || []
  } catch (e) { console.error(e) }
}

const goToScript = (script) => {
  router.push({ path: '/player/scripts', query: { scriptId: script.id } })
}

const handleUnfavorite = async (script) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该剧本吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await removeFavorite(script.id || script.scriptId)
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      loadFavorites()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

const difficultyText = (d) => ({ BEGINNER: '新手', INTERMEDIATE: '进阶', EXPERT: '烧脑' }[d] || d)
const difficultyColor = (d) => ({ BEGINNER: 'green', INTERMEDIATE: 'amber', EXPERT: 'red' }[d] || 'cyan')

onMounted(() => { loadFavorites() })
</script>

<style scoped lang="scss">
.my-favorites-page {
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

.script-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .script-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    background: var(--sk-bg-card);
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: var(--sk-border-glow);
      transform: translateX(4px);
    }

    .script-cover {
      width: 80px;
      height: 80px;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;

      .cover-img {
        width: 100%;
        height: 100%;
      }

      .cover-fallback {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, rgba(0, 153, 204, 0.15), rgba(119, 68, 204, 0.15));
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--sk-neon-cyan);
        font-size: 32px;
        font-weight: 900;
      }
    }

    .script-info {
      flex: 1;

      .script-name {
        font-size: 16px;
        font-weight: 700;
        color: var(--sk-text-primary);
        margin-bottom: 8px;
      }

      .script-tags {
        display: flex;
        gap: 8px;
        margin-bottom: 6px;
      }

      .script-meta {
        font-size: 13px;
        color: var(--sk-text-muted);

        span {
          margin-right: 12px;
        }

        .price {
          color: var(--sk-neon-amber);
          font-weight: 600;
        }
      }
    }

    .unfavorite-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(255, 136, 0, 0.1);
      border: 1px solid rgba(255, 136, 0, 0.25);
      color: var(--sk-neon-amber);
      cursor: pointer;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;

      &:hover {
        background: rgba(255, 136, 0, 0.2);
        transform: scale(1.1);
      }
    }
  }
}

.empty-state {
  padding: 60px;
}
</style>
