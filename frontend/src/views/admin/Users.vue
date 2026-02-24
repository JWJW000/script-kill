<template>
  <div class="admin-users">
    <div class="page-header fade-in-up">
      <h2 class="page-title">用户管理</h2>
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>

    <div class="table-card fade-in-up stagger-1">
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <span class="neon-tag" :class="'neon-tag--' + roleColor(scope.row.role)">{{ roleText(scope.row.role) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <span class="neon-tag" :class="scope.row.status === 1 ? 'neon-tag--green' : 'neon-tag--red'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button size="small" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button size="small" @click="handleAssignRole(scope.row)">分配角色</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="玩家" value="PLAYER" />
            <el-option label="主持人" value="HOST" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
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
import { getUserList, createUser, updateUser, deleteUser, resetPassword, assignRole } from '@/api/user'

const users = ref([])
const showDialog = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', role: 'PLAYER', status: 1 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadUsers = async () => {
  try {
    const res = await getUserList()
    if (res.code === 200) users.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 'PLAYER', status: 1 })
  showDialog.value = true
}

const editUser = (user) => {
  isEdit.value = true
  Object.assign(form, { ...user, password: '' })
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const res = await updateUser(form.id, form)
          if (res.code === 200) { ElMessage.success('更新成功'); showDialog.value = false; loadUsers() }
        } else {
          if (!form.password) { ElMessage.warning('请输入密码'); return }
          const res = await createUser(form)
          if (res.code === 200) { ElMessage.success('创建成功'); showDialog.value = false; loadUsers() }
        }
      } catch (e) { ElMessage.error('操作失败') }
    }
  })
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm('确定删除此用户？', '删除确认', { type: 'warning' })
    const res = await deleteUser(user.id)
    if (res.code === 200) { ElMessage.success('已删除'); loadUsers() }
  } catch (e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}

const handleResetPassword = async (user) => {
  try {
    const { value } = await ElMessageBox.prompt('输入新密码', '重置密码', { inputType: 'password' })
    if (value) {
      const res = await resetPassword(user.id, value)
      if (res.code === 200) ElMessage.success('密码已重置')
    }
  } catch (e) { if (e !== 'cancel') ElMessage.error('重置失败') }
}

const handleAssignRole = async (user) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入角色 (PLAYER / HOST / ADMIN)', '分配角色')
    if (value && ['PLAYER', 'HOST', 'ADMIN'].includes(value.toUpperCase())) {
      const res = await assignRole(user.id, value.toUpperCase())
      if (res.code === 200) { ElMessage.success('角色已分配'); loadUsers() }
    } else if (value) { ElMessage.warning('无效角色') }
  } catch (e) { if (e !== 'cancel') ElMessage.error('分配失败') }
}

const roleText = (r) => ({ PLAYER: '玩家', HOST: '主持人', ADMIN: '管理员' }[r] || r)
const roleColor = (r) => ({ PLAYER: 'cyan', HOST: 'purple', ADMIN: 'pink' }[r] || 'cyan')

onMounted(() => { loadUsers() })
</script>

<style scoped lang="scss">
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}

.table-card {
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border);
  border-radius: var(--sk-radius-lg);
  padding: 20px;
}
</style>
