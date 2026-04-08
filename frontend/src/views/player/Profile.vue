<template>
  <div class="profile-page">
    <div class="page-header fade-in-up">
      <h2 class="page-title">个人中心</h2>
    </div>

    <div class="profile-grid fade-in-up stagger-1">
      <div class="profile-card" @click="router.push('/player/orders')">
        <div class="card-icon orders-icon">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">我的订单</div>
          <div class="card-desc">查看所有订单记录</div>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="profile-card" @click="router.push('/player/my-teamups')">
        <div class="card-icon teamups-icon">
          <el-icon :size="32"><UserFilled /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">我的拼场</div>
          <div class="card-desc">查看参与的拼场</div>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="profile-card" @click="router.push('/player/my-reviews')">
        <div class="card-icon reviews-icon">
          <el-icon :size="32"><Star /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">我的评价</div>
          <div class="card-desc">查看发表的评价</div>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="profile-card" @click="router.push('/player/my-favorites')">
        <div class="card-icon favorites-icon">
          <el-icon :size="32"><Collection /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-title">我的收藏</div>
          <div class="card-desc">管理收藏的剧本</div>
        </div>
        <div class="card-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 基本信息卡片 -->
    <div class="info-card fade-in-up stagger-2">
      <div class="info-header">
        <h3>基本信息</h3>
        <el-button text type="primary" @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
      </div>
      <div class="info-content">
        <div class="avatar-section">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :before-upload="beforeUpload">
            <el-avatar :src="userForm.avatar" :size="80" class="user-avatar">
              <el-icon :size="30"><User /></el-icon>
            </el-avatar>
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
            </div>
          </el-upload>
        </div>
        <div class="info-fields">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ userForm.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">真实姓名</span>
            <span class="value">{{ userForm.realName || '未填写' }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系方式</span>
            <span class="value">{{ userForm.phone || '未填写' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑信息弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑基本信息" width="450px">
      <el-form :model="userForm" label-position="top">
        <el-form-item label="真实姓名">
          <el-input v-model="userForm.realName" size="large" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="userForm.phone" size="large" placeholder="请输入联系方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'

const router = useRouter()
const showEditDialog = ref(false)
const userForm = reactive({ username: '', realName: '', phone: '', avatar: '' })

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      Object.assign(userForm, res.data)
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
      showEditDialog.value = false
      await loadUserInfo()
    }
  } catch (e) { ElMessage.error('更新失败') }
}

onMounted(() => { loadUserInfo() })
</script>

<style scoped lang="scss">
.profile-page {
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

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--sk-bg-card);
  border: 1px solid var(--sk-border);
  border-radius: var(--sk-radius-lg);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: var(--sk-border-glow);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }

  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &.orders-icon {
      background: rgba(0, 153, 204, 0.1);
      color: var(--sk-neon-cyan);
    }
    &.teamups-icon {
      background: rgba(119, 68, 204, 0.1);
      color: var(--sk-neon-purple);
    }
    &.reviews-icon {
      background: rgba(255, 136, 0, 0.1);
      color: var(--sk-neon-amber);
    }
    &.favorites-icon {
      background: rgba(204, 34, 102, 0.1);
      color: var(--sk-neon-pink);
    }
  }

  .card-content {
    flex: 1;
    .card-title {
      font-size: 16px;
      font-weight: 700;
      color: var(--sk-text-primary);
      margin-bottom: 4px;
    }
    .card-desc {
      font-size: 13px;
      color: var(--sk-text-muted);
    }
  }

  .card-arrow {
    color: var(--sk-text-muted);
    font-size: 18px;
  }
}

.info-card {
  padding: 24px;
  background: var(--sk-bg-card);
  border: 1px solid var(--sk-border);
  border-radius: var(--sk-radius-lg);

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--sk-border);

    h3 {
      font-size: 16px;
      font-weight: 700;
      color: var(--sk-text-primary);
    }
  }

  .info-content {
    display: flex;
    gap: 24px;

    .avatar-section {
      .avatar-uploader {
        position: relative;
        cursor: pointer;

        :deep(.el-upload) { border: none !important; position: relative; }
        .user-avatar { border: 3px solid var(--sk-border); background: var(--sk-bg-elevated); }
        .avatar-overlay {
          position: absolute; inset: 0; background: rgba(0, 0, 0, 0.5); border-radius: 50%;
          display: flex; align-items: center; justify-content: center;
          opacity: 0; transition: opacity 0.3s; color: white; font-size: 20px;
        }
        &:hover .avatar-overlay { opacity: 1; }
      }
    }

    .info-fields {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 12px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 12px;

        .label {
          width: 80px;
          font-size: 13px;
          color: var(--sk-text-muted);
        }
        .value {
          font-size: 14px;
          color: var(--sk-text-primary);
        }
      }
    }
  }
}

@media (max-width: 600px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
  .info-card .info-content {
    flex-direction: column;
    align-items: center;
  }
}
</style>
