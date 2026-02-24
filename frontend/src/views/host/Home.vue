<template>
  <div class="host-home">
    <div class="page-header fade-in-up">
      <h2 class="page-title">主持人工作台</h2>
      <p class="page-desc">掌控全局，为每一场剧本注入灵魂</p>
    </div>

    <div class="stats-row">
      <div v-for="(stat, i) in stats" :key="stat.label" class="stat-card fade-in-up" :class="'stagger-' + (i + 1)">
        <div class="stat-ring" :style="{ borderColor: stat.color + '30', boxShadow: `0 0 20px ${stat.color}15` }">
          <span class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</span>
        </div>
        <span class="stat-label">{{ stat.label }}</span>
      </div>
    </div>

    <div class="quick-actions fade-in-up stagger-5">
      <h3 class="section-title">
        <span class="dot" style="background: var(--sk-neon-purple);" />
        快捷操作
      </h3>
      <div class="action-grid">
        <div class="action-card" @click="$router.push('/host/sessions')">
          <el-icon :size="28"><Calendar /></el-icon>
          <span>管理场次</span>
        </div>
        <div class="action-card" @click="$router.push('/host/reviews')">
          <el-icon :size="28"><Star /></el-icon>
          <span>查看评价</span>
        </div>
        <div class="action-card" @click="$router.push('/host/statistics')">
          <el-icon :size="28"><DataAnalysis /></el-icon>
          <span>数据面板</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboard } from '@/api/statistics'

const stats = ref([
  { label: '主持场次', value: 0, color: '#b44aff' },
  { label: '平均评分', value: '0.0', color: '#00e5ff' },
  { label: '好评率', value: '0%', color: '#00e676' },
  { label: '评价数量', value: 0, color: '#ffab00' }
])

const loadStatistics = async () => {
  try {
    const res = await getDashboard()
    if (res.code === 200 && res.data) {
      stats.value[0].value = res.data.totalSessions || 0
      stats.value[1].value = (res.data.averageRating || 0).toFixed(1)
      stats.value[2].value = (res.data.goodRate || 0).toFixed(1) + '%'
      stats.value[3].value = res.data.totalReviews || 0
    }
  } catch (e) { console.error(e) }
}

onMounted(() => { loadStatistics() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 32px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); margin-bottom: 4px; }
  .page-desc { color: var(--sk-text-muted); font-size: 14px; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 36px;

  .stat-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 28px 16px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    transition: all 0.35s;

    &:hover { border-color: var(--sk-border-glow); transform: translateY(-3px); }

    .stat-ring {
      width: 80px; height: 80px;
      border-radius: 50%;
      border: 3px solid;
      display: flex; align-items: center; justify-content: center;
      margin-bottom: 12px;

      .stat-value { font-size: 24px; font-weight: 900; font-family: var(--sk-font-display); }
    }

    .stat-label { font-size: 13px; color: var(--sk-text-muted); }
  }
}

.section-title {
  font-size: 16px; font-weight: 600; color: var(--sk-text-primary);
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  .dot { width: 8px; height: 8px; border-radius: 50%; }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 28px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    cursor: pointer;
    transition: all 0.3s;
    color: var(--sk-text-secondary);

    &:hover {
      border-color: rgba(180, 74, 255, 0.3);
      color: var(--sk-neon-purple);
      background: rgba(180, 74, 255, 0.04);
      transform: translateY(-3px);
    }

    span { font-size: 14px; font-weight: 500; }
  }
}
</style>
