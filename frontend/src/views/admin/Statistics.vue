<template>
  <div class="admin-statistics">
    <div class="page-header fade-in-up">
      <div>
        <h2 class="page-title">数据统计</h2>
        <p class="page-desc">全局运营数据大屏</p>
      </div>
      <div class="period-selector">
        <span v-for="p in ['day', 'week', 'month']" :key="p" class="period-btn" :class="{ active: period === p }" @click="period = p; loadStatistics()">
          {{ { day: '日', week: '周', month: '月' }[p] }}
        </span>
      </div>
    </div>

    <div class="stats-row">
      <div v-for="(stat, i) in statCards" :key="stat.label" class="stat-card fade-in-up" :class="'stagger-' + (i + 1)">
        <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <div class="data-grid">
      <div class="data-card fade-in-up stagger-5">
        <div class="card-header">
          <h3><span class="dot" style="background: var(--sk-neon-cyan);" />热门剧本 TOP10</h3>
        </div>
        <el-table :data="statistics.topScripts || []" style="width: 100%" size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="scriptName" label="剧本" />
          <el-table-column prop="orderCount" label="订单数" width="100">
            <template #default="scope"><span style="color: var(--sk-neon-cyan); font-weight: 600;">{{ scope.row.orderCount }}</span></template>
          </el-table-column>
        </el-table>
      </div>

      <div class="data-card fade-in-up stagger-6">
        <div class="card-header">
          <h3><span class="dot" style="background: var(--sk-neon-purple);" />主持人排行</h3>
        </div>
        <el-table :data="statistics.hostRanking || []" style="width: 100%" size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="hostName" label="主持人" />
          <el-table-column prop="orderCount" label="订单数" width="100">
            <template #default="scope"><span style="color: var(--sk-neon-purple); font-weight: 600;">{{ scope.row.orderCount }}</span></template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="data-card full-width fade-in-up stagger-6" style="margin-top: 20px;">
      <div class="card-header">
        <h3><span class="dot" style="background: var(--sk-neon-amber);" />主持人评分排行</h3>
      </div>
      <el-table :data="statistics.hostRatingRanking || []" style="width: 100%" size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="hostName" label="主持人" />
        <el-table-column prop="averageRating" label="平均评分" width="120">
          <template #default="scope">
            <span style="color: var(--sk-neon-amber); font-weight: 600;">{{ scope.row.averageRating?.toFixed(1) || '0.0' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalReviews" label="评价数" width="100" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/statistics'

const period = ref('month')
const statistics = reactive({ totalOrders: 0, totalRevenue: 0, activeSessions: 0, totalUsers: 0, topScripts: [], hostRanking: [], hostRatingRanking: [] })

const statCards = computed(() => [
  { label: '总订单数', value: statistics.totalOrders || 0, color: '#00e5ff' },
  { label: '总营收', value: '¥' + (statistics.totalRevenue || 0), color: '#ffab00' },
  { label: '活跃场次', value: statistics.activeSessions || 0, color: '#00e676' },
  { label: '总用户数', value: statistics.totalUsers || 0, color: '#ff2d78' }
])

const loadStatistics = async () => {
  try {
    const res = await getDashboard(period.value)
    if (res.code === 200 && res.data) Object.assign(statistics, res.data)
  } catch (e) { console.error(e) }
}

onMounted(() => { loadStatistics() })
</script>

<style scoped lang="scss">
.page-header {
  display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 28px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); margin-bottom: 4px; }
  .page-desc { color: var(--sk-text-muted); font-size: 14px; }

  .period-selector {
    display: flex; gap: 4px;
    padding: 3px; background: rgba(15, 19, 40, 0.6);
    border-radius: 8px; border: 1px solid var(--sk-border);

    .period-btn {
      padding: 6px 16px; border-radius: 6px;
      font-size: 13px; color: var(--sk-text-muted);
      cursor: pointer; transition: all 0.3s;
      &:hover { color: var(--sk-text-secondary); }
      &.active { background: rgba(255, 45, 120, 0.1); color: var(--sk-neon-pink); font-weight: 600; }
    }
  }
}

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
  .stat-card {
    text-align: center;
    padding: 28px 16px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    transition: all 0.35s;
    &:hover { border-color: var(--sk-border-glow); transform: translateY(-3px); }
    .stat-value { font-size: 32px; font-weight: 900; font-family: var(--sk-font-display); margin-bottom: 6px; }
    .stat-label { font-size: 13px; color: var(--sk-text-muted); }
  }
}

.data-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
}

.data-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border); border-radius: var(--sk-radius-lg); padding: 20px;

  .card-header {
    margin-bottom: 16px;
    h3 {
      font-size: 15px; color: var(--sk-text-primary); font-weight: 600;
      display: flex; align-items: center; gap: 8px;
      .dot { width: 8px; height: 8px; border-radius: 50%; }
    }
  }
}

.full-width { grid-column: 1 / -1; }
</style>
