<template>
  <div class="admin-home">
    <div class="page-header fade-in-up">
      <h2 class="page-title">系统控制台</h2>
      <p class="page-desc">全局数据概览与运营监控</p>
    </div>

    <div class="stats-row">
      <div v-for="(stat, i) in stats" :key="stat.label" class="stat-card fade-in-up" :class="'stagger-' + (i + 1)">
        <div class="stat-icon" :style="{ color: stat.color, filter: `drop-shadow(0 0 8px ${stat.color}60)` }">
          <el-icon :size="28"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
        <div class="stat-glow" :style="{ background: `radial-gradient(circle, ${stat.color}12 0%, transparent 70%)` }" />
      </div>
    </div>

    <div class="quick-nav fade-in-up stagger-5">
      <h3 class="section-title"><span class="dot" style="background: var(--sk-neon-pink);" />快捷管理</h3>
      <div class="nav-grid">
        <div class="nav-card" v-for="nav in navItems" :key="nav.path" @click="$router.push(nav.path)">
          <el-icon :size="24"><component :is="nav.icon" /></el-icon>
          <span>{{ nav.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/statistics'

const rawStats = ref({})

const stats = computed(() => [
  { label: '总用户数', value: rawStats.value.totalUsers || 0, icon: 'User', color: '#ff2d78' },
  { label: '总订单数', value: rawStats.value.totalOrders || 0, icon: 'Document', color: '#00e5ff' },
  { label: '总营收', value: '¥' + (rawStats.value.totalRevenue || 0), icon: 'Wallet', color: '#ffab00' },
  { label: '活跃场次', value: rawStats.value.activeSessions || 0, icon: 'Calendar', color: '#00e676' }
])

const navItems = [
  { label: '用户管理', icon: 'User', path: '/admin/users' },
  { label: '剧本管理', icon: 'Document', path: '/admin/scripts' },
  { label: '场次管理', icon: 'Calendar', path: '/admin/sessions' },
  { label: '订单管理', icon: 'List', path: '/admin/orders' },
  { label: '数据统计', icon: 'DataAnalysis', path: '/admin/statistics' }
]

const loadStatistics = async () => {
  try {
    const res = await getDashboard('month')
    if (res.code === 200 && res.data) rawStats.value = res.data
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
    position: relative;
    padding: 24px 20px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    overflow: hidden;
    transition: all 0.35s;
    &:hover { border-color: var(--sk-border-glow); transform: translateY(-3px); }

    .stat-icon { margin-bottom: 12px; }
    .stat-value { font-size: 28px; font-weight: 900; color: var(--sk-text-bright); font-family: var(--sk-font-display); margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: var(--sk-text-muted); }
    .stat-glow { position: absolute; right: -20px; bottom: -20px; width: 100px; height: 100px; pointer-events: none; }
  }
}

.section-title {
  font-size: 16px; font-weight: 600; color: var(--sk-text-primary);
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  .dot { width: 8px; height: 8px; border-radius: 50%; }
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;

  .nav-card {
    display: flex; flex-direction: column; align-items: center; gap: 10px;
    padding: 24px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    cursor: pointer; transition: all 0.3s;
    color: var(--sk-text-secondary);

    &:hover {
      border-color: rgba(255, 45, 120, 0.3);
      color: var(--sk-neon-pink);
      transform: translateY(-3px);
    }

    span { font-size: 13px; font-weight: 500; }
  }
}
</style>
