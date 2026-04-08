<template>
  <div class="player-home">
    <section class="hero-banner fade-in-up">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="greeting">Welcome back,</span>
          <span class="name">{{ userInfo?.realName || userInfo?.username || '侦探' }}</span>
        </h1>
        <p class="hero-desc">每一个线索都可能改变结局，你准备好了吗？</p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="$router.push('/player/scripts')">探索剧本</button>
          <button class="hero-btn ghost" @click="$router.push('/player/team-up')">加入拼场</button>
        </div>
      </div>
      <div class="hero-deco">
        <div class="orbit orbit-1" />
        <div class="orbit orbit-2" />
        <div class="orbit orbit-3" />
      </div>
    </section>

    <section class="stats-row">
      <div v-for="(stat, i) in statCards" :key="stat.label" class="stat-card fade-in-up" :class="'stagger-' + (i + 1)">
        <div class="stat-icon" :style="{ color: stat.color, filter: `drop-shadow(0 0 8px ${stat.color}60)` }">
          <el-icon :size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
        <div class="stat-glow" :style="{ background: `radial-gradient(circle, ${stat.color}15 0%, transparent 70%)` }" />
      </div>
    </section>

    <section class="content-grid">
      <div class="grid-card hot-scripts fade-in-up stagger-3">
        <div class="card-header">
          <h3>
            <span class="dot" style="background: var(--sk-neon-cyan);" />
            热门剧本
          </h3>
          <a class="more-link" @click="$router.push('/player/scripts')">查看全部</a>
        </div>
        <div class="script-list">
          <div v-for="script in hotScripts" :key="script.id" class="script-item" @click="goToScriptDetail(script)">
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
                <span class="neon-tag" :class="'neon-tag--' + difficultyColor(script.difficulty)">{{ difficultyText(script.difficulty) }}</span>
              </div>
              <div class="script-meta">
                <span>{{ script.minPlayers }}-{{ script.maxPlayers }}人</span>
                <span>{{ script.duration }}分钟</span>
              </div>
            </div>
            <div class="script-price">¥{{ script.price }}</div>
          </div>
          <div v-if="hotScripts.length === 0" class="empty-state">暂无剧本数据</div>
        </div>
      </div>

      <div class="grid-card team-ups fade-in-up stagger-4">
        <div class="card-header">
          <h3>
            <span class="dot" style="background: var(--sk-neon-purple);" />
            猜你喜欢
          </h3>
          <a class="more-link" @click="$router.push('/player/team-up')">发起拼场</a>
        </div>
        <div class="team-up-list">
          <div v-for="team in recommendedTeamUps" :key="team.id" class="team-up-item">
            <div class="team-info">
              <div class="team-script">{{ team.scriptName }}</div>
              <div class="team-time">{{ formatTime(team.expectedTime) }}</div>
              <div class="team-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (team.currentPlayers / team.totalPlayers * 100) + '%' }" />
                </div>
                <span class="progress-text">{{ team.currentPlayers }}/{{ team.totalPlayers }}</span>
              </div>
            </div>
            <button class="join-btn" @click="handleJoinTeamUp(team.id)">加入</button>
          </div>
          <div v-if="recommendedTeamUps.length === 0" class="empty-state">暂无拼场</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { getDashboard } from '@/api/statistics'
import { getScripts } from '@/api/script'
import { getRecommendedTeamUps, joinTeamUp as joinTeamUpApi } from '@/api/teamUp'

const userStore = useUserStore()
const router = useRouter()
const userInfo = computed(() => userStore.userInfo)
const rawStats = ref({})
const hotScripts = ref([])
const recommendedTeamUps = ref([])

const statCards = computed(() => [
  { label: '我的订单', value: rawStats.value.totalOrders || 0, icon: 'Document', color: '#00e5ff' },
  { label: '我的拼场', value: rawStats.value.totalTeamUps || 0, icon: 'UserFilled', color: '#b44aff' },
  { label: '我的评价', value: rawStats.value.totalReviews || 0, icon: 'Star', color: '#ffab00' },
  { label: '我的收藏', value: rawStats.value.favoriteCount || 0, icon: 'StarFilled', color: '#00e676' }
])

const difficultyText = (d) => ({ BEGINNER: '新手', INTERMEDIATE: '进阶', EXPERT: '烧脑' }[d] || d)
const difficultyColor = (d) => ({ BEGINNER: 'green', INTERMEDIATE: 'amber', EXPERT: 'red' }[d] || 'cyan')

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const loadStatistics = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200 && res.data) rawStats.value = res.data
  } catch (e) { console.error(e) }
}

const loadHotScripts = async () => {
  try {
    const res = await getScripts({ status: 1 })
    if (res.code === 200) hotScripts.value = (res.data || []).slice(0, 5)
  } catch (e) { console.error(e) }
}

const loadRecommendedTeamUps = async () => {
  try {
    const res = await getRecommendedTeamUps()
    if (res.code === 200) recommendedTeamUps.value = (res.data || []).slice(0, 5)
  } catch (e) { console.error(e) }
}

const handleJoinTeamUp = async (id) => {
  try {
    const res = await joinTeamUpApi(id)
    if (res.code === 200) {
      ElMessage.success('成功加入拼场')
      loadRecommendedTeamUps()
      loadStatistics()
    }
  } catch (e) { ElMessage.error(e.message || '加入失败') }
}

const goToScriptDetail = (script) => {
  router.push({ path: '/player/scripts', query: { scriptId: script.id } })
}

onMounted(() => {
  loadStatistics()
  loadHotScripts()
  loadRecommendedTeamUps()
})
</script>

<style scoped lang="scss">
.player-home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-banner {
  position: relative;
  padding: 48px 40px;
  border-radius: var(--sk-radius-lg);
  background: linear-gradient(135deg, rgba(0, 229, 255, 0.06), rgba(180, 74, 255, 0.04));
  border: 1px solid var(--sk-border);
  overflow: hidden;
  margin-bottom: 28px;

  .hero-content {
    position: relative;
    z-index: 1;
  }

  .hero-title {
    margin-bottom: 12px;

    .greeting {
      display: block;
      font-family: var(--sk-font-display);
      font-size: 14px;
      color: var(--sk-text-muted);
      letter-spacing: 3px;
      text-transform: uppercase;
      margin-bottom: 4px;
    }
    .name {
      font-size: 32px;
      font-weight: 900;
      color: var(--sk-text-bright);
      background: linear-gradient(135deg, var(--sk-neon-cyan), var(--sk-neon-purple));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .hero-desc {
    color: var(--sk-text-secondary);
    font-size: 15px;
    margin-bottom: 24px;
    letter-spacing: 0.5px;
  }

  .hero-actions {
    display: flex;
    gap: 12px;
  }

  .hero-btn {
    padding: 10px 28px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s;
    font-family: var(--sk-font-body);
    letter-spacing: 1px;

    &.primary {
      background: linear-gradient(135deg, rgba(0, 229, 255, 0.2), rgba(0, 229, 255, 0.08));
      color: var(--sk-neon-cyan);
      border: 1px solid rgba(0, 229, 255, 0.35);
      &:hover {
        box-shadow: var(--sk-glow-cyan);
        transform: translateY(-2px);
      }
    }
    &.ghost {
      background: transparent;
      color: var(--sk-text-secondary);
      border: 1px solid var(--sk-border);
      &:hover {
        border-color: var(--sk-neon-purple);
        color: var(--sk-neon-purple);
      }
    }
  }

  .hero-deco {
    position: absolute;
    right: 60px;
    top: 50%;
    transform: translateY(-50%);
    width: 180px;
    height: 180px;

    .orbit {
      position: absolute;
      border-radius: 50%;
      border: 1px solid;
      animation: spin linear infinite;

      &.orbit-1 {
        inset: 0;
        border-color: rgba(0, 229, 255, 0.15);
        animation-duration: 12s;
      }
      &.orbit-2 {
        inset: 25px;
        border-color: rgba(180, 74, 255, 0.12);
        animation-duration: 8s;
        animation-direction: reverse;
      }
      &.orbit-3 {
        inset: 55px;
        border-color: rgba(255, 45, 120, 0.1);
        animation-duration: 6s;
      }
    }
  }
}

@keyframes spin { from { transform: rotate(0); } to { transform: rotate(360deg); } }

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;

  .stat-card {
    position: relative;
    padding: 24px 20px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    overflow: hidden;
    transition: all 0.35s;

    &:hover {
      border-color: var(--sk-border-glow);
      transform: translateY(-3px);
    }

    .stat-icon { margin-bottom: 12px; }
    .stat-value {
      font-size: 28px;
      font-weight: 900;
      color: var(--sk-text-bright);
      font-family: var(--sk-font-display);
      margin-bottom: 4px;
    }
    .stat-label {
      font-size: 13px;
      color: var(--sk-text-muted);
      letter-spacing: 0.5px;
    }
    .stat-glow {
      position: absolute;
      right: -20px;
      bottom: -20px;
      width: 100px;
      height: 100px;
      pointer-events: none;
    }
  }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;

  .grid-card {
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    padding: 24px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        font-size: 16px;
        color: var(--sk-text-primary);
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;

        .dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          display: inline-block;
        }
      }

      .more-link {
        font-size: 13px;
        color: var(--sk-text-muted);
        cursor: pointer;
        transition: color 0.3s;
        &:hover { color: var(--sk-neon-cyan); }
      }
    }
  }
}

.script-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .script-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;

    &:hover {
      background: rgba(0, 229, 255, 0.04);
      border-color: var(--sk-border);
    }

    .script-cover {
      width: 60px;
      height: 60px;
      border-radius: 10px;
      overflow: hidden;
      flex-shrink: 0;

      .cover-img { width: 100%; height: 100%; }
      .cover-fallback {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, rgba(0, 229, 255, 0.15), rgba(180, 74, 255, 0.15));
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--sk-neon-cyan);
        font-size: 24px;
        font-weight: 900;
      }
    }

    .script-info {
      flex: 1;
      min-width: 0;

      .script-name {
        font-weight: 600;
        color: var(--sk-text-primary);
        margin-bottom: 6px;
        font-size: 14px;
      }
      .script-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 4px;
      }
      .script-meta {
        font-size: 12px;
        color: var(--sk-text-muted);
        span { margin-right: 12px; }
      }
    }

    .script-price {
      font-size: 18px;
      font-weight: 900;
      color: var(--sk-neon-pink);
      font-family: var(--sk-font-display);
      flex-shrink: 0;
    }
  }
}

.team-up-list {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .team-up-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px;
    border-radius: 10px;
    border: 1px solid var(--sk-border);
    transition: all 0.3s;

    &:hover {
      border-color: rgba(180, 74, 255, 0.25);
      background: rgba(180, 74, 255, 0.03);
    }

    .team-info {
      flex: 1;
      .team-script { font-weight: 600; color: var(--sk-text-primary); font-size: 14px; margin-bottom: 4px; }
      .team-time { font-size: 12px; color: var(--sk-text-muted); margin-bottom: 8px; }
      .team-progress {
        display: flex; align-items: center; gap: 10px;
        .progress-bar {
          flex: 1; height: 4px; background: var(--sk-bg-elevated); border-radius: 2px; overflow: hidden;
          .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--sk-neon-purple), var(--sk-neon-cyan));
            border-radius: 2px;
            transition: width 0.5s;
          }
        }
        .progress-text { font-size: 12px; color: var(--sk-text-secondary); font-weight: 600; flex-shrink: 0; }
      }
    }

    .join-btn {
      padding: 6px 18px;
      border: 1px solid rgba(180, 74, 255, 0.4);
      border-radius: 8px;
      background: rgba(180, 74, 255, 0.1);
      color: var(--sk-neon-purple);
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      font-family: var(--sk-font-body);
      margin-left: 12px;
      flex-shrink: 0;

      &:hover {
        background: rgba(180, 74, 255, 0.2);
        box-shadow: var(--sk-glow-purple);
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 30px;
  color: var(--sk-text-muted);
  font-size: 14px;
}
</style>
