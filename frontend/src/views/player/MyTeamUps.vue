<template>
  <div class="my-teamups-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">我的拼场</h2>
    </div>

    <div class="team-up-list fade-in-up stagger-1">
      <div v-for="team in myTeamUps" :key="team.id" class="team-card">
        <div class="team-info">
          <div class="team-script">{{ team.scriptName }}</div>
          <div class="team-time">{{ formatTime(team.expectedTime) }}</div>
          <div class="team-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (team.currentPlayers / team.totalPlayers * 100) + '%' }" />
            </div>
            <span>{{ team.currentPlayers }}/{{ team.totalPlayers }}</span>
          </div>
          <div class="team-status">
            <span class="neon-tag" :class="'neon-tag--' + teamStatusColor(team.status)">{{ teamStatusText(team.status) }}</span>
          </div>
        </div>
        <div class="team-actions">
          <button v-if="team.status === 1" class="action-btn danger" @click="handleLeaveTeamUp(team)">退出拼场</button>
        </div>
      </div>
      <div v-if="myTeamUps.length === 0" class="empty-state">
        <el-empty description="暂无参与的拼场" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyTeamUps, leaveTeamUp } from '@/api/teamUp'

const myTeamUps = ref([])

const loadTeamUps = async () => {
  try {
    const res = await getMyTeamUps()
    if (res.code === 200) myTeamUps.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleLeaveTeamUp = async (team) => {
  try {
    await ElMessageBox.confirm('确定要退出该拼场吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await leaveTeamUp(team.id)
    if (res.code === 200) {
      ElMessage.success('已退出拼场')
      loadTeamUps()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '退出失败')
    }
  }
}

const teamStatusText = (s) => ({ 0: '已关闭', 1: '进行中', 2: '已达标', 3: '已生成场次' }[s] || '未知')
const teamStatusColor = (s) => ({ 0: 'red', 1: 'green', 2: 'cyan', 3: 'amber' }[s] || 'cyan')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''

onMounted(() => { loadTeamUps() })
</script>

<style scoped lang="scss">
.my-teamups-page {
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

.team-up-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .team-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    background: var(--sk-bg-card);
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius-lg);
    transition: all 0.3s;

    &:hover {
      border-color: var(--sk-border-glow);
    }

    .team-info {
      flex: 1;
      .team-script {
        font-size: 16px;
        font-weight: 700;
        color: var(--sk-text-primary);
        margin-bottom: 8px;
      }
      .team-time {
        font-size: 14px;
        color: var(--sk-text-muted);
        margin-bottom: 10px;
      }
      .team-progress {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
        .progress-bar {
          flex: 1;
          height: 6px;
          background: var(--sk-bg-input);
          border-radius: 3px;
          overflow: hidden;
          .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--sk-neon-purple), var(--sk-neon-cyan));
            border-radius: 3px;
            transition: width 0.5s;
          }
        }
        span {
          font-size: 13px;
          color: var(--sk-text-secondary);
          font-weight: 600;
        }
      }
    }

    .team-actions {
      margin-left: 20px;
      .action-btn {
        padding: 8px 20px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s;
        font-family: var(--sk-font-body);

        &.danger {
          background: rgba(204, 34, 68, 0.1);
          color: var(--sk-neon-red);
          border: 1px solid rgba(204, 34, 68, 0.3);
          &:hover {
            background: rgba(204, 34, 68, 0.2);
          }
        }
      }
    }
  }
}

.empty-state {
  padding: 60px;
}
</style>
