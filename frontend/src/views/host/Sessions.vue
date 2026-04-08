<template>
  <div class="host-sessions">
    <div class="page-header fade-in-up">
      <h2 class="page-title">我的场次</h2>
    </div>

    <div class="sessions-list fade-in-up stagger-1">
      <div v-for="session in sessions" :key="session.id" class="session-card">
        <div class="card-main">
          <div class="session-title">{{ session.scriptName || '未知剧本' }}</div>
          <div class="session-meta">
            <span><el-icon><Clock /></el-icon> {{ formatTime(session.sessionTime) }}</span>
            <span><el-icon><Location /></el-icon> {{ session.location }}</span>
            <span><el-icon><User /></el-icon> {{ session.currentPlayers }}/{{ session.maxPlayers }}人</span>
          </div>
        </div>
        <div class="card-status">
          <span class="neon-tag" :class="'neon-tag--' + statusColor(session.status)">{{ statusText(session.status) }}</span>
          <span v-if="session.attendanceConfirmed === 1" class="attendance-badge">
            <el-icon><CircleCheck /></el-icon> 已确认到场
          </span>
        </div>
        <div class="card-actions">
          <el-button size="small" @click="viewDetails(session)">详情</el-button>
          <template v-if="session.attendanceConfirmed === 1">
            <el-button size="small" type="warning" @click="handleCancelConfirm(session)">取消确认到场</el-button>
            <el-button v-if="session.status !== 3" size="small" type="primary" @click="handleEnd(session)">结束本场</el-button>
          </template>
          <template v-else>
            <el-button size="small" type="success" @click="handleConfirm(session)">确认到场</el-button>
          </template>
          <el-button v-if="new Date(session.sessionTime) > new Date()" size="small" type="danger" @click="handleCancel(session)">取消</el-button>
        </div>
      </div>
      <div v-if="sessions.length === 0" class="empty-state">暂无场次</div>
    </div>

    <!-- 场次详情抽屉 -->
    <el-drawer v-model="showDetailDrawer" title="场次详情" size="560px">
      <div v-if="currentSession" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item"><span class="label">剧本</span><span class="value">{{ currentSession.scriptName }}</span></div>
          <div class="detail-item"><span class="label">时间</span><span class="value">{{ formatTime(currentSession.sessionTime) }}</span></div>
          <div class="detail-item"><span class="label">地点</span><span class="value">{{ currentSession.location }}</span></div>
          <div class="detail-item"><span class="label">价格</span><span class="value price">¥{{ currentSession.price }}</span></div>
          <div class="detail-item"><span class="label">已预约</span><span class="value">{{ currentSession.currentPlayers }}人</span></div>
          <div class="detail-item"><span class="label">最大人数</span><span class="value">{{ currentSession.maxPlayers }}人</span></div>
        </div>

        <h4 class="sub-title">预约玩家</h4>
        <el-table :data="sessionOrders" style="width: 100%" size="small">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="userName" label="玩家" />
          <el-table-column prop="playerCount" label="人数" width="80" />
          <el-table-column prop="totalAmount" label="金额" width="80" />
          <el-table-column prop="orderStatus" label="状态" width="100">
            <template #default="scope">
              <span class="neon-tag" :class="'neon-tag--' + orderStatusColor(scope.row.orderStatus)">{{ orderStatusText(scope.row.orderStatus) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, Clock, Location, User } from '@element-plus/icons-vue'
import { getHostSessions, confirmAttendance, cancelConfirmAttendance, cancelSession, endSession } from '@/api/session'
import { getSessionOrders } from '@/api/order'

const sessions = ref([])
const showDetailDrawer = ref(false)
const currentSession = ref(null)
const sessionOrders = ref([])

const loadSessions = async () => {
  try {
    const res = await getHostSessions()
    if (res.code === 200) sessions.value = res.data || []
  } catch (e) { console.error(e) }
}

const viewDetails = async (session) => {
  currentSession.value = session
  try {
    const res = await getSessionOrders(session.id)
    if (res.code === 200) sessionOrders.value = res.data || []
  } catch (e) { console.error(e) }
  showDetailDrawer.value = true
}

const handleConfirm = async (session) => {
  try {
    const res = await confirmAttendance(session.id)
    if (res.code === 200) {
      ElMessage.success('已确认到场')
      loadSessions()
    }
  } catch (e) { ElMessage.error('确认失败') }
}

const handleCancelConfirm = async (session) => {
  try {
    await ElMessageBox.confirm('确定要取消「已确认到场」吗？', '取消确认到场', { type: 'warning' })
    const res = await cancelConfirmAttendance(session.id)
    if (res.code === 200) {
      ElMessage.success('已取消确认到场')
      loadSessions()
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const handleCancel = async (session) => {
  try {
    await ElMessageBox.confirm('确定取消此场次？', '取消确认', { type: 'warning' })
    const res = await cancelSession(session.id)
    if (res.code === 200) { ElMessage.success('已取消'); loadSessions() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('取消失败') }
}

const handleEnd = async (session) => {
  try {
    await ElMessageBox.confirm('确定要结束此场次吗？结束后订单将标记为已完成。', '结束场次', { type: 'warning' })
    const res = await endSession(session.id)
    if (res.code === 200) { ElMessage.success('场次已结束'); loadSessions() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''
const statusText = (s) => ({ 0: '已取消', 1: '可预约', 2: '已满员', 3: '已结束' }[s] || '未知')
const statusColor = (s) => ({ 0: 'red', 1: 'green', 2: 'amber', 3: 'cyan' }[s] || 'cyan')
const orderStatusText = (s) => ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成' }[s] || '未知')
const orderStatusColor = (s) => ({ 0: 'amber', 1: 'green', 2: 'red', 3: 'cyan' }[s] || 'cyan')

onMounted(() => { loadSessions() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .session-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 20px 24px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.3s;

    &:hover { border-color: var(--sk-border-glow); transform: translateX(4px); }

    .card-main {
      flex: 1;
      .session-title { font-size: 16px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 8px; }
      .session-meta {
        display: flex; gap: 20px; font-size: 13px; color: var(--sk-text-muted);
        span { display: flex; align-items: center; gap: 4px; }
      }
    }
    .card-status {
  margin-right: 12px;
  display: flex;
  align-items: center;
  gap: 10px;

  .attendance-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    color: var(--sk-neon-cyan);
    background: rgba(0, 229, 255, 0.12);
    border: 1px solid var(--sk-neon-cyan);
    box-shadow: 0 0 12px rgba(0, 229, 255, 0.3);
  }
}
    .card-actions { display: flex; gap: 8px; flex-shrink: 0; }
  }
}

.detail-content {
  .detail-grid {
    display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 24px;
    .detail-item {
      padding: 12px; background: rgba(0, 229, 255, 0.03);
      border-radius: 8px; border: 1px solid var(--sk-border);
      .label { display: block; font-size: 12px; color: var(--sk-text-muted); margin-bottom: 4px; }
      .value { font-size: 15px; color: var(--sk-text-primary); font-weight: 600; }
      .value.price { color: var(--sk-neon-pink); font-family: var(--sk-font-display); }
    }
  }
  .sub-title { color: var(--sk-text-secondary); margin-bottom: 12px; font-size: 15px; }
}

.empty-state { text-align: center; padding: 60px; color: var(--sk-text-muted); }
</style>
