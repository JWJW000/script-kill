<template>
  <div class="admin-sessions">
    <div class="page-header fade-in-up">
      <h2 class="page-title">场次管理</h2>
      <el-button type="primary" @click="handleAdd">新增场次</el-button>
    </div>

    <div class="table-card fade-in-up stagger-1">
      <el-table :data="sessions" style="width: 100%">
        <el-table-column prop="scriptName" label="剧本" />
        <el-table-column prop="hostName" label="主持人" width="120" />
        <el-table-column label="时间" width="170">
          <template #default="scope">{{ formatTime(scope.row.sessionTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" />
        <el-table-column label="预约" width="100">
          <template #default="scope">{{ scope.row.currentPlayers }}/{{ scope.row.maxPlayers }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <span class="neon-tag" :class="'neon-tag--' + statusColor(scope.row.status)">{{ statusText(scope.row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button size="small" @click="editSession(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="toggleStatus(scope.row)">下架</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑场次' : '新增场次'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="选择剧本" prop="scriptId">
              <el-select v-model="form.scriptId" placeholder="选择剧本" filterable style="width: 100%">
                <el-option v-for="s in scriptList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选择主持人" prop="hostId">
              <el-select v-model="form.hostId" placeholder="选择主持人" filterable style="width: 100%">
                <el-option v-for="h in hosts" :key="h.id" :label="h.realName || h.username" :value="h.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="场次时间" prop="sessionTime">
              <el-date-picker v-model="form.sessionTime" type="datetime" style="width: 100%" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" :disabled-date="disablePastDate" />
              <div class="form-hint">玩家端仅显示「未来时间」且「可预约」的场次，请选择尚未开始的时间</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点" prop="location"><el-input v-model="form.location" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最大人数" prop="maxPlayers"><el-input-number v-model="form.maxPlayers" :min="2" :max="20" style="width: 100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionList, createSession, updateSession, deleteSession, updateSessionStatus } from '@/api/session'
import { getScripts } from '@/api/script'
import { getUserList } from '@/api/user'

const sessions = ref([])
const scriptList = ref([])
const hosts = ref([])
const showDialog = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = reactive({ id: null, scriptId: null, hostId: null, sessionTime: '', location: '', price: 0, maxPlayers: 6 })

const rules = {
  scriptId: [{ required: true, message: '请选择剧本', trigger: 'change' }],
  hostId: [{ required: true, message: '请选择主持人', trigger: 'change' }],
  sessionTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  maxPlayers: [{ required: true, message: '请输入最大人数', trigger: 'blur' }]
}

const loadSessions = async () => {
  try { const res = await getSessionList({}); if (res.code === 200) sessions.value = res.data || [] } catch (e) { console.error(e) }
}
const loadScripts = async () => {
  try { const res = await getScripts({}); if (res.code === 200) scriptList.value = res.data || [] } catch (e) { console.error(e) }
}
const loadHosts = async () => {
  try { const res = await getUserList(); if (res.code === 200) hosts.value = (res.data || []).filter(u => u.role === 'HOST') } catch (e) { console.error(e) }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, scriptId: null, hostId: null, sessionTime: '', location: '', price: 0, maxPlayers: 6 })
  showDialog.value = true
}
const editSession = (s) => { isEdit.value = true; Object.assign(form, { ...s }); showDialog.value = true }

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 直接传本地时间字符串，避免 toISOString() 转成 UTC 导致存库/显示时间不一致
        const data = { ...form, sessionTime: form.sessionTime }
        const res = isEdit.value ? await updateSession(form.id, data) : await createSession(data)
        if (res.code === 200) { ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); showDialog.value = false; loadSessions() }
      } catch (e) { ElMessage.error('操作失败') }
    }
  })
}

const handleDelete = async (s) => {
  try {
    await ElMessageBox.confirm('确定删除？', '删除确认', { type: 'warning' })
    const res = await deleteSession(s.id)
    if (res.code === 200) { ElMessage.success('已删除'); loadSessions() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const toggleStatus = async (s) => {
  try {
    const res = await updateSessionStatus(s.id, 0)
    if (res.code === 200) { ElMessage.success('已下架'); loadSessions() }
  } catch (e) { ElMessage.error('操作失败') }
}

const disablePastDate = (date) => date < new Date(new Date().setHours(0, 0, 0, 0))
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : ''
const statusText = (s) => ({ 0: '已取消', 1: '可预约', 2: '已满员', 3: '已结束' }[s] || '未知')
const statusColor = (s) => ({ 0: 'red', 1: 'green', 2: 'amber', 3: 'cyan' }[s] || 'cyan')

onMounted(() => { loadSessions(); loadScripts(); loadHosts() })
</script>

<style scoped lang="scss">
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}
.table-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border); border-radius: var(--sk-radius-lg); padding: 20px;
}
.form-hint {
  font-size: 12px; color: var(--sk-text-muted); margin-top: 6px; line-height: 1.4;
}
</style>
