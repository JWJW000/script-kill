<template>
  <div class="profile-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">身份档案</h2>
    </div>

    <div class="profile-content">
      <div class="profile-card fade-in-up stagger-1">
        <div class="avatar-section">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :before-upload="beforeUpload">
            <el-avatar :src="userForm.avatar" :size="100" class="user-avatar">
              <el-icon :size="40"><User /></el-icon>
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
            </div>
          </el-upload>
          <div class="user-identity">
            <h3>{{ userForm.username }}</h3>
            <span class="role-badge">PLAYER</span>
          </div>
        </div>

        <el-form :model="userForm" label-position="top" class="profile-form">
          <el-form-item label="真实姓名">
            <el-input v-model="userForm.realName" size="large" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="userForm.phone" size="large" />
          </el-form-item>
          <div class="form-actions">
            <el-button type="primary" size="large" @click="updateProfile">保存更改</el-button>
            <el-button size="large" @click="showPasswordDialog = true">修改密码</el-button>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-position="top">
        <el-form-item label="当前密码" required>
          <el-input v-model="passwordForm.oldPassword" type="password" show-password size="large" />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="passwordForm.newPassword" type="password" show-password size="large" />
        </el-form-item>
        <el-form-item label="确认新密码" required>
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, updatePassword, uploadAvatar } from '@/api/user'

const userStore = useUserStore()
const showPasswordDialog = ref(false)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const userForm = reactive({ username: '', realName: '', phone: '', avatar: '' })

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      Object.assign(userForm, res.data)
      userStore.userInfo = res.data
    }
  } catch (e) { console.error(e) }
}

const beforeUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) { ElMessage.error('只能上传图片'); return false }
  if (!isLt10M) { ElMessage.error('图片不能超过10MB'); return false }
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      userForm.avatar = res.data
      ElMessage.success('头像已更新')
      await loadUserInfo()
    }
  } catch (e) { ElMessage.error('上传失败') }
  return false
}

const updateProfile = async () => {
  try {
    const res = await updateUserInfo({ realName: userForm.realName, phone: userForm.phone })
    if (res.code === 200) {
      ElMessage.success('档案已更新')
      await loadUserInfo()
    }
  } catch (e) { ElMessage.error('更新失败') }
}

const handleUpdatePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  try {
    const res = await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    if (res.code === 200) {
      ElMessage.success('密码已修改')
      showPasswordDialog.value = false
      Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    }
  } catch (e) { ElMessage.error(e.message || '修改失败') }
}

onMounted(() => { loadUserInfo() })
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 28px;
  .page-title { font-size: 24px; font-weight: 800; color: var(--sk-text-bright); }
}

.profile-card {
  max-width: 520px;
  padding: 36px;
  background: linear-gradient(135deg, rgba(15, 19, 40, 0.9), rgba(20, 25, 53, 0.7));
  border: 1px solid var(--sk-border);
  border-radius: var(--sk-radius-lg);

  .avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 32px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--sk-border);

    .avatar-uploader {
      position: relative;
      cursor: pointer;

      :deep(.el-upload) {
        border: none !important;
        position: relative;
      }

      .user-avatar {
        border: 3px solid rgba(0, 229, 255, 0.3);
        background: var(--sk-bg-elevated);
      }

      .avatar-overlay {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s;
        color: white;
        font-size: 20px;
      }

      &:hover .avatar-overlay { opacity: 1; }
    }

    .user-identity {
      h3 { font-size: 20px; color: var(--sk-text-primary); margin-bottom: 6px; }
      .role-badge {
        font-family: var(--sk-font-display);
        font-size: 11px;
        color: var(--sk-neon-cyan);
        letter-spacing: 2px;
        padding: 2px 10px;
        border: 1px solid rgba(0, 229, 255, 0.3);
        border-radius: 4px;
        background: rgba(0, 229, 255, 0.06);
      }
    }
  }

  .form-actions {
    display: flex;
    gap: 12px;
    margin-top: 8px;
  }
}
</style>
