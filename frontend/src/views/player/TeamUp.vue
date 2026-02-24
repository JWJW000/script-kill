<template>
  <div class="team-up-page">
    <div class="page-header fade-in-up">
      <div>
        <h2 class="page-title">组队拼场</h2>
        <p class="page-desc">找到志同道合的玩家，一起解锁新剧本</p>
      </div>
      <button class="create-btn" @click="showCreateDrawer = true">
        <el-icon><Plus /></el-icon>
        发起拼场
      </button>
    </div>

    <div v-if="recommendedTeamUps.length > 0" class="section fade-in-up stagger-1">
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
          <button v-if="!team.joined" class="join-btn" @click="handleJoin(team.id)">加入</button>
          <button v-else class="join-btn leave" @click="handleLeave(team.id)">退出</button>
        </div>
      </div>
    </div>

    <div class="section fade-in-up stagger-2">
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
          <button v-if="!team.joined" class="join-btn" @click="handleJoin(team.id)">加入</button>
          <button v-else class="join-btn leave" @click="handleLeave(team.id)">退出</button>
        </div>
        <div v-if="teamUps.length === 0" class="empty-state">暂无拼场</div>
      </div>
    </div>

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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeamUpList, joinTeamUp, leaveTeamUp, createTeamUp, getRecommendedTeamUps } from '@/api/teamUp'
import { getScripts } from '@/api/script'

const teamUps = ref([])
const recommendedTeamUps = ref([])
const scripts = ref([])
const showCreateDrawer = ref(false)
const createForm = ref({ scriptId: null, expectedTime: '', totalPlayers: 6, remark: '' })

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

const handleJoin = async (id) => {
  try {
    const res = await joinTeamUp(id)
    if (res.code === 200) {
      ElMessage.success('成功加入拼场')
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

onMounted(() => { loadTeamUps(); loadRecommended(); loadScripts() })
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
  margin-bottom: 28px;
  .section-title {
    font-size: 16px; font-weight: 600; color: var(--sk-text-primary);
    display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
    .dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
  }
}

.team-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 14px;

  .team-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;
    background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
    border: 1px solid var(--sk-border);
    border-radius: var(--sk-radius);
    transition: all 0.3s;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: var(--sk-border-glow);
      transform: translateY(-2px);
    }

    &.recommended {
      border-color: rgba(180, 74, 255, 0.2);
      background: linear-gradient(135deg, rgba(20, 15, 40, 0.9), rgba(25, 20, 53, 0.7));
    }

    .card-badge {
      position: absolute;
      top: 8px; right: -24px;
      background: linear-gradient(135deg, var(--sk-neon-purple), rgba(180, 74, 255, 0.7));
      color: white;
      font-size: 10px;
      padding: 2px 30px;
      transform: rotate(45deg);
      font-weight: 600;
    }

    .card-info {
      flex: 1;
      h4 { font-size: 15px; font-weight: 700; color: var(--sk-text-primary); margin-bottom: 6px; }
      .card-time { font-size: 13px; color: var(--sk-text-muted); margin-bottom: 10px; }
      .card-progress {
        display: flex; align-items: center; gap: 10px;
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
        margin-top: 8px; font-size: 12px; color: var(--sk-text-muted);
        font-style: italic; opacity: 0.8;
      }
      .recommend-reason {
        margin-top: 4px;
        font-size: 12px;
        color: var(--sk-text-secondary);
        opacity: 0.95;
      }
    }

    .join-btn {
      padding: 8px 22px; border-radius: 8px;
      background: rgba(0, 229, 255, 0.1);
      color: var(--sk-neon-cyan); border: 1px solid rgba(0, 229, 255, 0.3);
      font-weight: 600; cursor: pointer; transition: all 0.3s;
      font-family: var(--sk-font-body); margin-left: 16px; flex-shrink: 0;
      &:hover { background: rgba(0, 229, 255, 0.2); box-shadow: var(--sk-glow-cyan); }
      &.leave {
        background: rgba(255, 99, 132, 0.08);
        color: #ff6384;
        border-color: rgba(255, 99, 132, 0.4);
        &:hover {
          background: rgba(255, 99, 132, 0.16);
          box-shadow: 0 0 12px rgba(255, 99, 132, 0.45);
        }
      }
    }
  }
}

.create-form {
  .submit-btn {
    width: 100%; padding: 14px; border-radius: 12px; margin-top: 8px;
    background: linear-gradient(135deg, rgba(180, 74, 255, 0.2), rgba(180, 74, 255, 0.06));
    color: var(--sk-neon-purple); border: 1px solid rgba(180, 74, 255, 0.4);
    font-size: 16px; font-weight: 600; cursor: pointer; transition: all 0.3s;
    font-family: var(--sk-font-body); letter-spacing: 2px;
    &:hover { box-shadow: var(--sk-glow-purple); }
  }
}

.empty-state { grid-column: 1/-1; text-align: center; padding: 40px; color: var(--sk-text-muted); }
</style>
