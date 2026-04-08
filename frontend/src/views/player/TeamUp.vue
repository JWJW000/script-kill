<template>
  <div class="team-up-page">
    <div class="page-header fade-in-up">
      <div>
        <h2 class="page-title">组队拼场</h2>
        <p class="page-desc">找到志同道合的玩家，一起解锁新剧本</p>
      </div>
      <button class="create-btn" @click="openCreateDrawer">
        <el-icon><Plus /></el-icon>
        发起拼场
      </button>
    </div>

    <!-- 当从剧本详情页进入时显示该剧本的拼场 -->
    <div v-if="filterScriptId && filteredTeamUps.length > 0" class="section fade-in-up stagger-1">
      <h3 class="section-title">
        <span class="dot" style="background: var(--sk-neon-amber);" />
        {{ filterScriptName }} 的拼场
      </h3>
      <div class="team-cards">
        <div v-for="team in filteredTeamUps" :key="team.id" class="team-card">
          <div class="card-info">
            <h4>{{ team.scriptName }}</h4>
            <p class="card-time">{{ formatTime(team.expectedTime) }}</p>
            <div class="card-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (team.currentPlayers / team.totalPlayers * 100) + '%' }" />
              </div>
              <span>{{ team.currentPlayers }}/{{ team.totalPlayers }}</span>
            </div>
            <p v-if="team.remark" class="card-remark">{{ team.remark }}</p>
          </div>
          <button v-if="!team.joined" class="join-btn" @click="handleJoin(team)">加入</button>
          <button v-else class="join-btn leave" @click="handleLeave(team.id)">退出</button>
        </div>
      </div>
    </div>

    <div v-if="recommendedTeamUps.length > 0 && !filterScriptId" class="section fade-in-up stagger-1">
      <h3 class="section-title">
        <span class="dot" style="background: var(--sk-neon-purple);" />
        AI 智能推荐
      </h3>
      <div class="team-cards">
        <div v-for="team in recommendedTeamUps" :key="'r' + team.id" class="team-card recommended">
          <div class="card-badge">推荐</div>
          <div class="card-info">
            <h4>{{ team.scriptName }}</h4>
            <p class="card-time">{{ formatTime(team.expectedTime) }}</p>
            <div class="card-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (team.currentPlayers / team.totalPlayers * 100) + '%' }" />
              </div>
              <span>{{ team.currentPlayers }}/{{ team.totalPlayers }}</span>
            </div>
            <p v-if="team.remark" class="card-remark">{{ team.remark }}</p>
            <p v-if="team.recommendReason" class="recommend-reason">AI 推荐理由：{{ team.recommendReason }}</p>
          </div>
          <button v-if="!team.joined" class="join-btn" @click="handleJoin(team)">加入</button>
          <button v-else class="join-btn leave" @click="handleLeave(team.id)">退出</button>
        </div>
      </div>
    </div>

    <div v-if="!filterScriptId" class="section fade-in-up stagger-2">
      <h3 class="section-title">
        <span class="dot" style="background: var(--sk-neon-cyan);" />
        全部拼场
      </h3>
      <div class="team-cards">
        <div v-for="team in teamUps" :key="team.id" class="team-card">
          <div class="card-info">
            <h4>{{ team.scriptName }}</h4>
            <p class="card-time">{{ formatTime(team.expectedTime) }}</p>
            <div class="card-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (team.currentPlayers / team.totalPlayers * 100) + '%' }" />
              </div>
              <span>{{ team.currentPlayers }}/{{ team.totalPlayers }}</span>
            </div>
            <p v-if="team.remark" class="card-remark">{{ team.remark }}</p>
          </div>
          <button v-if="!team.joined" class="join-btn" @click="handleJoin(team)">加入</button>
          <button v-else class="join-btn leave" @click="handleLeave(team.id)">退出</button>
        </div>
        <div v-if="teamUps.length === 0" class="empty-state">暂无拼场</div>
      </div>
    </div>

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

    <!-- 发起拼场抽屉 -->
    <el-drawer v-model="showCreateDrawer" title="发起拼场" size="420px" direction="rtl">
      <div class="create-form">
        <el-form :model="createForm" label-position="top">
          <el-form-item label="选择剧本" required>
            <el-select v-model="createForm.scriptId" placeholder="搜索或选择剧本" filterable style="width: 100%">
              <el-option v-for="s in scripts" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="期望时间" required>
            <el-date-picker v-model="createForm.expectedTime" type="datetime" placeholder="选择期望时间" style="width: 100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
          <el-form-item label="总人数" required>
            <el-input-number v-model="createForm.totalPlayers" :min="2" :max="20" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注信息">
            <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="添加备注，让更多玩家了解你的需求..." />
          </el-form-item>
          <button type="button" class="submit-btn" @click="handleCreate">发起拼场</button>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTeamUpList, joinTeamUp, leaveTeamUp, createTeamUp, getRecommendedTeamUps } from '@/api/teamUp'
import { getScripts } from '@/api/script'

const route = useRoute()
const router = useRouter()

const teamUps = ref([])
const recommendedTeamUps = ref([])
const scripts = ref([])
const showCreateDrawer = ref(false)
const showJoinDialog = ref(false)
const joiningTeam = ref(null)
const joinCount = ref(1)
const createForm = ref({ scriptId: null, expectedTime: '', totalPlayers: 6, remark: '' })
const filterScriptId = ref(null)
const filterScriptName = ref('')

const filteredTeamUps = computed(() => {
  if (!filterScriptId.value) return []
  return teamUps.value.filter(t => t.scriptId === filterScriptId.value)
})

const loadTeamUps = async () => {
  try {
    const res = await getTeamUpList({})
    if (res.code === 200) teamUps.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadRecommended = async () => {
  try {
    const res = await getRecommendedTeamUps()
    if (res.code === 200) recommendedTeamUps.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadScripts = async () => {
  try {
    const res = await getScripts({})
    if (res.code === 200) scripts.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleJoin = (team) => {
  joiningTeam.value = team
  joinCount.value = 1
  showJoinDialog.value = true
}

const confirmJoin = async () => {
  if (!joiningTeam.value) return
  try {
    const res = await joinTeamUp(joiningTeam.value.id, joinCount.value)
    if (res.code === 200) {
      if (res.data && res.data.orderNo) {
        ElMessage.success('成功加入拼场！已生成待支付订单，请前往支付')
        showJoinDialog.value = false
      } else {
        ElMessage.success('成功加入拼场')
        showJoinDialog.value = false
      }
      loadTeamUps()
      loadRecommended()
    }
  } catch (e) { ElMessage.error(e.message || '加入失败') }
}

const handleLeave = async (id) => {
  try {
    const res = await leaveTeamUp(id)
    if (res.code === 200) {
      ElMessage.success('已退出该拼场')
      loadTeamUps()
      loadRecommended()
    }
  } catch (e) {
    ElMessage.error(e.message || '退出失败')
  }
}

const openCreateDrawer = () => {
  // 如果有filterScriptId，自动选中
  if (filterScriptId.value) {
    createForm.value.scriptId = filterScriptId.value
    createForm.value.remark = `想要玩《${filterScriptName.value}》，一起来吧！`
  }
  showCreateDrawer.value = true
}

const handleCreate = async () => {
  if (!createForm.value.scriptId || !createForm.value.expectedTime) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await createTeamUp(createForm.value)
    if (res.code === 200) {
      ElMessage.success('拼场创建成功')
      showCreateDrawer.value = false
      createForm.value = { scriptId: null, expectedTime: '', totalPlayers: 6, remark: '' }
      loadTeamUps()
    }
  } catch (e) { ElMessage.error(e.message || '创建失败') }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  // 检查是否有来自剧本详情页的query参数
  if (route.query.scriptId) {
    filterScriptId.value = Number(route.query.scriptId)
    // 获取剧本名称
    await loadScripts()
    const script = scripts.value.find(s => s.id === filterScriptId.value)
    if (script) {
      filterScriptName.value = script.name
    }
  }
  
  loadTeamUps()
  loadRecommended()
  loadScripts()

  // 如果action=create，自动打开创建抽屉
  if (route.query.action === 'create' && route.query.scriptId) {
    setTimeout(() => {
      openCreateDrawer()
    }, 100)
  }
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;

  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); margin-bottom: 4px; }
  .page-desc { color: var(--sk-text-muted); font-size: 14px; }

  .create-btn {
    display: flex; align-items: center; gap: 6px;
    padding: 10px 24px; border-radius: 10px;
    background: linear-gradient(135deg, rgba(180, 74, 255, 0.2), rgba(180, 74, 255, 0.06));
    color: var(--sk-neon-purple); border: 1px solid rgba(180, 74, 255, 0.35);
    font-size: 14px; font-weight: 600; cursor: pointer;
    transition: all 0.3s; font-family: var(--sk-font-body);
    &:hover { box-shadow: var(--sk-glow-purple); transform: translateY(-2px); }
  }
}

.section {
  margin-bottom: 32px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 700;
    color: var(--sk-text-primary);
    margin-bottom: 16px;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }
  }
}

.team-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .team-card {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px 20px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.3s;
    position: relative;

    &:hover { border-color: var(--sk-border-glow); transform: translateX(4px); }

    &.recommended {
      border-color: rgba(180, 74, 255, 0.2);
    }

    .card-badge {
      position: absolute;
      top: -8px;
      right: 12px;
      padding: 2px 10px;
      background: linear-gradient(135deg, rgba(180, 74, 255, 0.2), rgba(180, 74, 255, 0.1));
      border: 1px solid rgba(180, 74, 255, 0.3);
      border-radius: 10px;
      font-size: 11px;
      color: var(--sk-neon-purple);
      font-weight: 600;
    }

    .card-info {
      flex: 1;
      h4 { font-size: 15px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 6px; }
      .card-time { font-size: 13px; color: var(--sk-text-muted); margin-bottom: 8px; }
      .card-progress {
        display: flex; align-items: center; gap: 10px; margin-bottom: 8px;
        .progress-bar {
          flex: 1; height: 4px; background: var(--sk-bg-elevated); border-radius: 2px; overflow: hidden;
          .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--sk-neon-purple), var(--sk-neon-cyan));
            border-radius: 2px; transition: width 0.5s;
          }
        }
        span { font-size: 12px; color: var(--sk-text-secondary); font-weight: 600; flex-shrink: 0; }
      }
      .card-remark {
        font-size: 13px;
        color: var(--sk-text-muted);
        line-height: 1.5;
        margin-top: 4px;
      }
      .recommend-reason {
        font-size: 12px;
        color: var(--sk-neon-purple);
        background: rgba(180, 74, 255, 0.06);
        padding: 8px 12px;
        border-radius: 6px;
        margin-top: 8px;
        border-left: 2px solid rgba(180, 74, 255, 0.4);
      }
    }

    .join-btn {
      padding: 8px 20px;
      border-radius: 8px;
      background: linear-gradient(135deg, rgba(0, 229, 255, 0.15), rgba(0, 229, 255, 0.05));
      border: 1px solid rgba(0, 229, 255, 0.35);
      color: var(--sk-neon-cyan);
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      font-family: var(--sk-font-body);
      flex-shrink: 0;

      &:hover { box-shadow: var(--sk-glow-cyan); }

      &.leave {
        background: rgba(255, 99, 132, 0.08);
        border-color: rgba(255, 99, 132, 0.4);
        color: #ff6384;
        &:hover { background: rgba(255, 99, 132, 0.16); }
      }
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

.create-form {
  padding: 0 20px;

  :deep(.el-form-item__label) {
    color: var(--sk-text-secondary);
    font-weight: 600;
  }

  .submit-btn {
    width: 100%;
    padding: 14px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(180, 74, 255, 0.2), rgba(180, 74, 255, 0.08));
    border: 1px solid rgba(180, 74, 255, 0.4);
    color: var(--sk-neon-purple);
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    transition: all 0.3s;
    font-family: var(--sk-font-body);
    margin-top: 8px;

    &:hover {
      box-shadow: var(--sk-glow-purple);
      transform: translateY(-2px);
    }
  }
}

.empty-state { text-align: center; padding: 60px; color: var(--sk-text-muted); }
</style>
