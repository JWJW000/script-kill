<template>
  <div class="scripts-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">剧本档案库</h2>
      <div class="search-bar">
        <el-input v-model="searchForm.name" placeholder="搜索剧本名称..." clearable @input="loadScripts" size="large">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </div>

    <div class="filter-bar fade-in-up stagger-1">
      <div class="filter-group">
        <span class="filter-label">类型</span>
        <div class="filter-chips">
          <span class="chip" :class="{ active: !searchForm.type }" @click="searchForm.type = ''; loadScripts()">全部</span>
          <span v-for="t in ['恐怖', '情感', '硬核', '欢乐', '推理']" :key="t" class="chip" :class="{ active: searchForm.type === t }" @click="searchForm.type = t; loadScripts()">{{ t }}</span>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-label">难度</span>
        <div class="filter-chips">
          <span class="chip" :class="{ active: !searchForm.difficulty }" @click="searchForm.difficulty = ''; loadScripts()">全部</span>
          <span class="chip" :class="{ active: searchForm.difficulty === 'BEGINNER' }" @click="searchForm.difficulty = 'BEGINNER'; loadScripts()">新手</span>
          <span class="chip" :class="{ active: searchForm.difficulty === 'INTERMEDIATE' }" @click="searchForm.difficulty = 'INTERMEDIATE'; loadScripts()">进阶</span>
          <span class="chip" :class="{ active: searchForm.difficulty === 'EXPERT' }" @click="searchForm.difficulty = 'EXPERT'; loadScripts()">烧脑</span>
        </div>
      </div>
      <div class="filter-group">
        <span class="filter-label">人数</span>
        <div class="filter-chips">
          <span class="chip" :class="{ active: !searchForm.playerCount }" @click="searchForm.playerCount = ''; loadScripts()">全部</span>
          <span class="chip" :class="{ active: searchForm.playerCount === '≤4' }" @click="searchForm.playerCount = '≤4'; loadScripts()">≤4人</span>
          <span class="chip" :class="{ active: searchForm.playerCount === '5' }" @click="searchForm.playerCount = '5'; loadScripts()">5人</span>
          <span class="chip" :class="{ active: searchForm.playerCount === '6' }" @click="searchForm.playerCount = '6'; loadScripts()">6人</span>
          <span class="chip" :class="{ active: searchForm.playerCount === '7' }" @click="searchForm.playerCount = '7'; loadScripts()">7人</span>
          <span class="chip" :class="{ active: searchForm.playerCount === '≥8' }" @click="searchForm.playerCount = '≥8'; loadScripts()">≥8人</span>
        </div>
      </div>
    </div>

    <div class="scripts-grid fade-in-up stagger-2">
      <div v-for="script in scripts" :key="script.id" class="script-card" @click="openDetail(script)">
        <div class="card-cover">
          <el-image :src="script.cover" fit="cover" class="cover-img">
            <template #error>
              <div class="cover-fallback">
                <span class="fallback-text">{{ script.name?.charAt(0) }}</span>
              </div>
            </template>
          </el-image>
          <div class="cover-overlay">
            <span class="neon-tag neon-tag--cyan">{{ script.type }}</span>
          </div>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ script.name }}</h3>
          <div class="card-tags">
            <span class="neon-tag" :class="'neon-tag--' + difficultyColor(script.difficulty)">{{ difficultyText(script.difficulty) }}</span>
            <span class="meta-text">{{ script.minPlayers }}-{{ script.maxPlayers }}人</span>
            <span class="meta-text">{{ script.duration }}min</span>
          </div>
          <p class="card-desc">{{ script.description || '暂无简介' }}</p>
          <div class="card-footer">
            <span class="price">¥{{ script.price }}</span>
            <button class="detail-btn">查看详情</button>
          </div>
        </div>
      </div>
      <div v-if="scripts.length === 0" class="empty-state">
        <p>暂无匹配的剧本</p>
      </div>
    </div>

    <!-- 剧本详情抽屉 -->
    <el-drawer v-model="showDrawer" :title="currentScript?.name" size="480px" direction="rtl">
      <div v-if="currentScript" class="script-detail">
        <div class="detail-cover">
          <el-image :src="currentScript.cover" fit="cover" class="detail-img">
            <template #error>
              <div class="cover-fallback large">{{ currentScript.name?.charAt(0) }}</div>
            </template>
          </el-image>
        </div>
        <div class="detail-tags">
          <span class="neon-tag neon-tag--cyan">{{ currentScript.type }}</span>
          <span class="neon-tag" :class="'neon-tag--' + difficultyColor(currentScript.difficulty)">{{ difficultyText(currentScript.difficulty) }}</span>
        </div>
        <div class="detail-info">
          <div class="info-row"><span class="info-label">人数</span><span class="info-value">{{ currentScript.minPlayers }} - {{ currentScript.maxPlayers }} 人</span></div>
          <div class="info-row"><span class="info-label">时长</span><span class="info-value">{{ currentScript.duration }} 分钟</span></div>
          <div class="info-row"><span class="info-label">价格</span><span class="info-value price">¥{{ currentScript.price }}</span></div>
          <div v-if="scriptStats.totalReviews > 0" class="info-row"><span class="info-label">评分</span><span class="info-value rating">{{ scriptStats.averageRating?.toFixed(1) }} ({{ scriptStats.totalReviews }}条评价)</span></div>
        </div>
        <div class="detail-desc">
          <h4>剧本简介</h4>
          <p>{{ currentScript.description || '这个剧本的故事等你来揭开...' }}</p>
        </div>

        <button class="book-btn" @click="$router.push({ path: '/player/sessions', query: { scriptId: currentScript.id }}); showDrawer = false">立即预约场次</button>

        <div class="action-buttons">
          <button class="action-btn" :class="{ favorited: isFavorited }" @click="toggleFavorite">
            <el-icon><Star /></el-icon>
            {{ isFavorited ? '已收藏' : '加入收藏' }}
          </button>
          <button class="action-btn" @click="showTeamUpDialog">
            <el-icon><UserFilled /></el-icon>
            加入拼场
          </button>
          <button class="action-btn" @click="goToCreateTeamUp">
            <el-icon><Plus /></el-icon>
            发起拼场
          </button>
        </div>

        <!-- 该剧本的拼场列表 -->
        <div v-if="scriptTeamUps.length > 0" class="script-teamups">
          <h4 class="teamups-title">该剧本的拼场</h4>
          <div v-for="team in scriptTeamUps" :key="team.id" class="teamup-item">
            <div class="teamup-info">
              <span class="teamup-time">{{ formatTime(team.expectedTime) }}</span>
              <span class="teamup-players">{{ team.currentPlayers }}/{{ team.totalPlayers }}人</span>
            </div>
            <button v-if="!team.joined" class="teamup-join-btn" @click="handleQuickJoin(team)">加入</button>
            <span v-else class="teamup-joined">已加入</span>
          </div>
        </div>

        <!-- 剧本评价区域 -->
        <div class="detail-reviews">
          <div class="reviews-header">
            <h4>玩家评价</h4>
            <div v-if="scriptStats.totalReviews > 0" class="review-summary">
              <span class="avg-score">{{ scriptStats.averageRating?.toFixed(1) }}</span>
              <el-rate :model-value="scriptStats.averageRating" disabled size="small" />
              <span class="review-count">{{ scriptStats.totalReviews }}条评价</span>
            </div>
          </div>
          <div v-if="scriptReviews.length > 0" class="reviews-list">
            <div v-for="review in scriptReviews" :key="review.id" class="review-item">
              <div class="review-top">
                <span class="reviewer">{{ review.userName || '匿名玩家' }}</span>
                <span class="review-date">{{ formatReviewTime(review.createTime) }}</span>
              </div>
              <div class="review-stars">
                <el-rate :model-value="review.scriptRating" disabled size="small" />
              </div>
              <p class="review-text">{{ review.scriptComment || '该玩家未留下评语' }}</p>
            </div>
          </div>
          <div v-else class="no-reviews">暂无评价，成为第一个评价的人吧</div>
        </div>
      </div>
    </el-drawer>

    <!-- 加入拼场弹窗 -->
    <el-dialog v-model="showJoinDialog" title="加入拼场" width="400px">
      <div v-if="joiningTeam" class="join-form">
        <div class="join-info">
          <h4>{{ joiningTeam.scriptName }}</h4>
          <p>{{ formatTime(joiningTeam.expectedTime) }}</p>
          <div class="current-status">
            <span>当前 {{ joiningTeam.currentPlayers }}/{{ joiningTeam.totalPlayers }} 人</span>
          </div>
        </div>
        <el-form-item label="加入人数">
          <el-input-number v-model="joinCount" :min="1" :max="joiningTeam.totalPlayers - joiningTeam.currentPlayers" style="width: 100%" />
        </el-form-item>
        <p class="join-tip">选择你要加入的人数，系统将为你生成待支付订单</p>
      </div>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmJoin">确认加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getScripts } from '@/api/script'
import { getReviewList, getScriptStatistics } from '@/api/review'
import { addFavorite, removeFavorite, getFavorites } from '@/api/script'
import { getTeamUpList, joinTeamUp } from '@/api/teamUp'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, UserFilled, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const scripts = ref([])
const showDrawer = ref(false)
const currentScript = ref(null)
const scriptReviews = ref([])
const scriptStats = ref({ averageRating: 0, totalReviews: 0, goodRate: 0 })
const isFavorited = ref(false)
const favoriteId = ref(null)
const scriptTeamUps = ref([])
const showJoinDialog = ref(false)
const joiningTeam = ref(null)
const joinCount = ref(1)

const searchForm = reactive({
  name: '',
  type: '',
  difficulty: '',
  playerCount: ''
})

const loadScripts = async () => {
  try {
    const params = {
      name: searchForm.name || undefined,
      type: searchForm.type || undefined,
      difficulty: searchForm.difficulty || undefined,
      playerCount: searchForm.playerCount || undefined
    }
    // 如果有 scriptId 参数，只加载该剧本
    if (route.query.scriptId) {
      params.id = route.query.scriptId
    }
    const res = await getScripts(params)
    if (res.code === 200) scripts.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadScriptTeamUps = async (scriptId) => {
  try {
    const res = await getTeamUpList({ scriptId })
    if (res.code === 200) scriptTeamUps.value = res.data || []
  } catch (e) { console.error(e) }
}

const openDetail = async (script) => {
  currentScript.value = script
  showDrawer.value = true
  scriptReviews.value = []
  scriptStats.value = { averageRating: 0, totalReviews: 0, goodRate: 0 }
  isFavorited.value = false
  favoriteId.value = null
  scriptTeamUps.value = []

  try {
    const [reviewsRes, statsRes, favRes] = await Promise.all([
      getReviewList({ scriptId: script.id }),
      getScriptStatistics(script.id),
      getFavorites()
    ])
    if (reviewsRes.code === 200) scriptReviews.value = reviewsRes.data || []
    if (statsRes.code === 200 && statsRes.data) scriptStats.value = statsRes.data
    if (favRes.code === 200) {
      // FavoriteVO has id (favorite record id) and scriptId
      const fav = (favRes.data || []).find(f => f.scriptId === script.id)
      if (fav) {
        isFavorited.value = true
        favoriteId.value = fav.id  // This is the favorite record ID
      }
    }
    // Load team-ups for this script
    await loadScriptTeamUps(script.id)
  } catch (e) { console.error(e) }
}

const formatReviewTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const difficultyText = (d) => ({ BEGINNER: '新手', INTERMEDIATE: '进阶', EXPERT: '烧脑' }[d] || d)
const difficultyColor = (d) => ({ BEGINNER: 'green', INTERMEDIATE: 'amber', EXPERT: 'red' }[d] || 'cyan')

const toggleFavorite = async () => {
  if (!currentScript.value) return
  try {
    if (isFavorited.value) {
      // Pass scriptId for removal since backend removeFavorite takes scriptId
      const res = await removeFavorite(currentScript.value.id)
      if (res.code === 200) {
        isFavorited.value = false
        favoriteId.value = null
        ElMessage.success('已取消收藏')
      }
    } else {
      const res = await addFavorite({ scriptId: currentScript.value.id })
      if (res.code === 200) {
        isFavorited.value = true
        favoriteId.value = res.data
        ElMessage.success('已添加到收藏')
      }
    }
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}

const showTeamUpDialog = () => {
  if (scriptTeamUps.value.length > 0) {
    showJoinDialog.value = true
    joiningTeam.value = scriptTeamUps.value[0]
  } else {
    ElMessage.info('暂无该剧本的拼场，你可以发起一个新拼场')
    goToCreateTeamUp()
  }
}

const handleQuickJoin = (team) => {
  joiningTeam.value = team
  joinCount.value = 1
  showJoinDialog.value = true
}

const confirmJoin = async () => {
  if (!joiningTeam.value) return
  try {
    const res = await joinTeamUp(joiningTeam.value.id, joinCount.value)
    if (res.code === 200) {
      ElMessage.success('成功加入拼场')
      showJoinDialog.value = false
      await loadScriptTeamUps(currentScript.value.id)
    }
  } catch (e) { ElMessage.error(e.message || '加入失败') }
}

const goToCreateTeamUp = () => {
  if (!currentScript.value) return
  showDrawer.value = false
  router.push({ path: '/player/team-up', query: { scriptId: currentScript.value.id, action: 'create' }})
}

onMounted(() => { loadScripts() })
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 800;
    color: var(--sk-text-bright);
  }
  .search-bar { width: 320px; }
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 28px;

  .filter-group {
    display: flex;
    align-items: center;
    gap: 12px;

    .filter-label {
      font-size: 13px;
      color: var(--sk-text-muted);
      min-width: 36px;
    }
    .filter-chips {
      display: flex;
      gap: 8px;

      .chip {
        padding: 5px 16px;
        border-radius: 20px;
        font-size: 13px;
        color: var(--sk-text-muted);
        background: rgba(15, 19, 40, 0.6);
        border: 1px solid var(--sk-border);
        cursor: pointer;
        transition: all 0.3s;

        &:hover { color: var(--sk-text-secondary); border-color: var(--sk-border-glow); }
        &.active {
          color: var(--sk-neon-cyan);
          background: rgba(0, 229, 255, 0.08);
          border-color: rgba(0, 229, 255, 0.35);
        }
      }
    }
  }
}

.scripts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;

  .script-card {
    border-radius: var(--sk-radius-lg);
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-6px);
      border-color: var(--sk-border-glow);
      box-shadow: var(--sk-glow-cyan);
    }

    .card-cover {
      position: relative;
      height: 180px;
      overflow: hidden;

      .cover-img { width: 100%; height: 100%; }
      .cover-fallback {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, rgba(0, 229, 255, 0.1), rgba(180, 74, 255, 0.1));
        display: flex;
        align-items: center;
        justify-content: center;
        .fallback-text {
          font-size: 48px;
          font-weight: 900;
          color: rgba(0, 229, 255, 0.4);
        }
      }
      .cover-overlay {
        position: absolute;
        bottom: 10px;
        left: 10px;
      }
    }

    .card-body {
      padding: 16px;

      .card-title { font-size: 16px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 8px; }
      .card-tags {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        .meta-text { font-size: 12px; color: var(--sk-text-muted); }
      }
      .card-desc {
        font-size: 13px;
        color: var(--sk-text-muted);
        line-height: 1.5;
        margin-bottom: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .price {
          font-size: 20px;
          font-weight: 900;
          color: var(--sk-neon-pink);
          font-family: var(--sk-font-display);
        }
        .detail-btn {
          padding: 5px 14px;
          border: 1px solid rgba(0, 229, 255, 0.3);
          border-radius: 6px;
          background: transparent;
          color: var(--sk-neon-cyan);
          font-size: 12px;
          cursor: pointer;
          transition: all 0.3s;
          font-family: var(--sk-font-body);
          &:hover {
            background: rgba(0, 229, 255, 0.1);
            box-shadow: var(--sk-glow-cyan);
          }
        }
      }
    }
  }
}

.script-detail {
  .detail-cover {
    width: 100%;
    height: 240px;
    border-radius: var(--sk-radius);
    overflow: hidden;
    margin-bottom: 20px;
    .detail-img { width: 100%; height: 100%; }
    .cover-fallback.large {
      width: 100%; height: 100%;
      background: linear-gradient(135deg, rgba(0, 229, 255, 0.1), rgba(180, 74, 255, 0.1));
      display: flex; align-items: center; justify-content: center;
      font-size: 64px; font-weight: 900; color: rgba(0, 229, 255, 0.3);
    }
  }

  .detail-tags { display: flex; gap: 8px; margin-bottom: 20px; }

  .detail-info {
    background: rgba(0, 229, 255, 0.03);
    border-radius: 10px;
    padding: 16px;
    margin-bottom: 20px;

    .info-row {
      display: flex; justify-content: space-between; padding: 8px 0;
      border-bottom: 1px solid var(--sk-border);
      &:last-child { border-bottom: none; }
      .info-label { color: var(--sk-text-muted); font-size: 14px; }
      .info-value { color: var(--sk-text-primary); font-weight: 600; }
      .info-value.price { color: var(--sk-neon-pink); font-family: var(--sk-font-display); font-size: 18px; }
      .info-value.rating { color: var(--sk-neon-amber); }
    }
  }

  .detail-desc {
    margin-bottom: 24px;
    h4 { color: var(--sk-text-secondary); margin-bottom: 8px; font-size: 14px; }
    p { color: var(--sk-text-muted); line-height: 1.8; font-size: 14px; }
  }

  .book-btn {
    width: 100%;
    padding: 14px;
    border: 1px solid rgba(0, 229, 255, 0.4);
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(0, 229, 255, 0.15), rgba(0, 229, 255, 0.05));
    color: var(--sk-neon-cyan);
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    font-family: var(--sk-font-body);
    letter-spacing: 2px;
    margin-bottom: 12px;

    &:hover {
      box-shadow: var(--sk-glow-cyan);
      transform: translateY(-2px);
    }
  }

  .action-buttons {
    display: flex;
    gap: 10px;
    margin-bottom: 24px;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 10px;
      border-radius: 10px;
      border: 1px solid var(--sk-border);
      background: rgba(15, 19, 40, 0.6);
      color: var(--sk-text-secondary);
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      font-family: var(--sk-font-body);

      &:hover {
        border-color: var(--sk-border-glow);
        color: var(--sk-text-primary);
      }

      &.favorited {
        border-color: rgba(255, 171, 0, 0.4);
        color: var(--sk-neon-amber);
        background: rgba(255, 171, 0, 0.08);
      }
    }
  }

  .script-teamups {
    margin-bottom: 24px;
    padding: 16px;
    background: rgba(180, 74, 255, 0.03);
    border: 1px solid rgba(180, 74, 255, 0.15);
    border-radius: 10px;

    .teamups-title {
      font-size: 14px;
      color: var(--sk-text-secondary);
      margin-bottom: 12px;
    }

    .teamup-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid rgba(180, 74, 255, 0.1);
      &:last-child { border-bottom: none; }

      .teamup-info {
        display: flex;
        gap: 12px;
        .teamup-time { font-size: 13px; color: var(--sk-text-primary); }
        .teamup-players { font-size: 12px; color: var(--sk-text-muted); }
      }

      .teamup-join-btn {
        padding: 5px 14px;
        border-radius: 6px;
        background: rgba(0, 229, 255, 0.1);
        border: 1px solid rgba(0, 229, 255, 0.3);
        color: var(--sk-neon-cyan);
        font-size: 12px;
        cursor: pointer;
        &:hover { background: rgba(0, 229, 255, 0.2); }
      }

      .teamup-joined {
        font-size: 12px;
        color: var(--sk-neon-amber);
      }
    }
  }

  .detail-reviews {
    border-top: 1px solid var(--sk-border);
    padding-top: 20px;

    .reviews-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 { color: var(--sk-text-secondary); font-size: 15px; }

      .review-summary {
        display: flex;
        align-items: center;
        gap: 8px;

        .avg-score {
          font-size: 20px;
          font-weight: 900;
          color: var(--sk-neon-amber);
          font-family: var(--sk-font-display);
        }
        .review-count {
          font-size: 12px;
          color: var(--sk-text-muted);
        }
      }
    }

    .reviews-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-height: 400px;
      overflow-y: auto;

      .review-item {
        padding: 14px;
        background: rgba(0, 229, 255, 0.02);
        border: 1px solid var(--sk-border);
        border-radius: 10px;
        transition: border-color 0.3s;

        &:hover { border-color: var(--sk-border-glow); }

        .review-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;

          .reviewer {
            font-size: 13px;
            font-weight: 600;
            color: var(--sk-text-primary);
          }
          .review-date {
            font-size: 11px;
            color: var(--sk-text-muted);
          }
        }

        .review-stars { margin-bottom: 8px; }

        .review-text {
          font-size: 13px;
          color: var(--sk-text-secondary);
          line-height: 1.6;
        }
      }
    }

    .no-reviews {
      text-align: center;
      padding: 32px;
      color: var(--sk-text-muted);
      font-size: 13px;
      border: 1px dashed var(--sk-border);
      border-radius: 10px;
    }
  }
}

.join-form {
  .join-info {
    background: rgba(0, 229, 255, 0.03);
    border-radius: 10px;
    padding: 16px;
    margin-bottom: 20px;
    border: 1px solid var(--sk-border);

    h4 { font-size: 16px; color: var(--sk-text-primary); margin-bottom: 8px; }
    p { font-size: 13px; color: var(--sk-text-muted); margin-bottom: 8px; }
    .current-status {
      display: flex;
      justify-content: space-between;
      align-items: center;
      span {
        padding: 4px 12px;
        background: rgba(180, 74, 255, 0.1);
        border: 1px solid rgba(180, 74, 255, 0.3);
        border-radius: 6px;
        color: var(--sk-neon-purple);
        font-size: 13px;
        font-weight: 600;
      }
    }
  }

  .join-tip {
    font-size: 12px;
    color: var(--sk-text-muted);
    margin-top: 8px;
    text-align: center;
  }
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  color: var(--sk-text-muted);
}
</style>
