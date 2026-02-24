<template>
  <div class="host-statistics">
    <div class="page-header fade-in-up">
      <h2 class="page-title">数据面板</h2>
      <p class="page-desc">你的主持数据一览</p>
    </div>

    <div class="stats-grid fade-in-up stagger-1">
      <div class="stat-card" v-for="(stat, i) in statCards" :key="stat.label">
        <div class="stat-icon" :style="{ color: stat.color }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <div class="info-card fade-in-up stagger-2">
      <div class="card-header">
        <h3>
          <span class="dot" style="background: var(--sk-neon-purple);" />
          主持数据趋势（最近 7 天）
        </h3>
      </div>
      <div class="charts-grid">
        <div class="chart-block">
          <div class="chart-title">场次完成数</div>
          <div class="chart-body">
            <div v-for="item in sessionTrend" :key="item.date" class="bar">
              <div
                class="bar-inner"
                :style="{ height: (Number(item.completedCount || 0) / maxSessionCount * 100 || 0) + '%' }"
              />
              <div class="bar-value">{{ item.completedCount }}</div>
              <div class="bar-label">{{ item.date.slice(5) }}</div>
            </div>
            <svg v-if="sessionTrend.length" class="trend-line" viewBox="0 0 100 100" preserveAspectRatio="none">
              <polyline :points="sessionLinePoints" />
            </svg>
          </div>
        </div>
        <div class="chart-block">
          <div class="chart-title">主持人评分趋势</div>
          <div class="chart-body">
            <div v-for="item in ratingTrend" :key="item.date" class="bar">
              <div
                class="bar-inner rating"
                :style="{ height: (Number(item.avgRating || 0) / maxRating * 100 || 0) + '%' }"
              />
              <div class="bar-value">{{ (item.avgRating || 0).toFixed(1) }}</div>
              <div class="bar-label">{{ item.date.slice(5) }}</div>
            </div>
            <svg v-if="ratingTrend.length" class="trend-line" viewBox="0 0 100 100" preserveAspectRatio="none">
              <polyline :points="ratingLinePoints" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/statistics'

const rawStats = ref({})

const statCards = computed(() => [
  { label: '总场次', value: rawStats.value.totalSessions || 0, icon: 'Calendar', color: '#b44aff' },
  { label: '平均评分', value: (rawStats.value.averageRating || 0).toFixed(1), icon: 'Star', color: '#ffab00' },
  { label: '好评率', value: (rawStats.value.goodRate || 0).toFixed(1) + '%', icon: 'SuccessFilled', color: '#00e676' },
  { label: '总评价', value: rawStats.value.totalReviews || 0, icon: 'ChatDotSquare', color: '#00e5ff' }
])

const sessionTrend = computed(() => rawStats.value.sessionTrend || [])
const ratingTrend = computed(() => rawStats.value.ratingTrend || [])

const maxSessionCount = computed(() => {
  const arr = sessionTrend.value
  if (!arr.length) return 1
  const max = Math.max(...arr.map(i => Number(i.completedCount || 0)))
  return max || 1
})

const maxRating = computed(() => {
  const arr = ratingTrend.value
  if (!arr.length) return 5
  const max = Math.max(...arr.map(i => Number(i.avgRating || 0)))
  return max > 0 ? max : 5
})

// 折线图坐标（0-100 的归一化坐标，用于 SVG polyline）
const sessionLinePoints = computed(() => {
  const data = sessionTrend.value
  if (!data.length) return ''
  const n = data.length
  const max = maxSessionCount.value || 1
  return data
    .map((item, idx) => {
      const x = ((idx + 0.5) / n) * 100
      const v = Number(item.completedCount || 0)
      const y = 100 - (v / max) * 100
      return `${x},${y}`
    })
    .join(' ')
})

const ratingLinePoints = computed(() => {
  const data = ratingTrend.value
  if (!data.length) return ''
  const n = data.length
  const max = maxRating.value || 5
  return data
    .map((item, idx) => {
      const x = ((idx + 0.5) / n) * 100
      const v = Number(item.avgRating || 0)
      const y = 100 - (v / max) * 100
      return `${x},${y}`
    })
    .join(' ')
})

const loadStatistics = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200 && res.data) rawStats.value = res.data
  } catch (e) { console.error(e) }
}

onMounted(() => { loadStatistics() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 28px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); margin-bottom: 4px; }
  .page-desc { color: var(--sk-text-muted); font-size: 14px; }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 28px;

  .stat-card {
    padding: 24px 20px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.35s;
    &:hover { border-color: var(--sk-border-glow); transform: translateY(-3px); }

    .stat-icon { margin-bottom: 10px; }
    .stat-value { font-size: 28px; font-weight: 900; font-family: var(--sk-font-display); margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: var(--sk-text-muted); }
  }
}

.info-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border);
  border-radius: var(--sk-radius-lg);
  padding: 24px;

  .card-header {
    margin-bottom: 20px;
    h3 {
      font-size: 16px; color: var(--sk-text-primary); font-weight: 600;
      display: flex; align-items: center; gap: 8px;
      .dot { width: 8px; height: 8px; border-radius: 50%; }
    }
  }

  .charts-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;

    .chart-block {
      .chart-title {
        font-size: 14px;
        color: var(--sk-text-secondary);
        margin-bottom: 8px;
      }
      .chart-body {
        position: relative;
        height: 180px;
        display: flex;
        align-items: flex-end;
        gap: 8px;
        padding: 8px 4px 0;
        border: 1px dashed var(--sk-border);
        border-radius: var(--sk-radius);

        .bar {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: flex-end;
          gap: 6px;

          .bar-inner {
            width: 14px;
            border-radius: 6px 6px 2px 2px;
            background: linear-gradient(180deg, rgba(180, 74, 255, 0.9), rgba(180, 74, 255, 0.2));
          }
          .bar-inner.rating {
            background: linear-gradient(180deg, rgba(255, 171, 0, 0.9), rgba(255, 171, 0, 0.2));
          }
          .bar-label {
            font-size: 10px;
            color: var(--sk-text-muted);
            transform: scale(0.9);
          }
          .bar-value {
            font-size: 11px;
            color: var(--sk-text-secondary);
          }
        }

        .trend-line {
          position: absolute;
          inset: 0;
          width: 100%;
          height: 100%;
          pointer-events: none;

          polyline {
            fill: none;
            stroke-width: 1.5;
          }
        }

        .trend-line polyline {
          stroke: rgba(180, 74, 255, 0.9);
        }

        .trend-line:nth-of-type(2) polyline {
          stroke: rgba(255, 171, 0, 0.9);
        }
      }
    }
  }
}
</style>
