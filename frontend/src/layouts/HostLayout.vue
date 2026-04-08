<template>
  <div class="sk-layout">
    <aside class="sk-sidebar">
      <div class="sidebar-brand" @click="$router.push('/host')">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" fill="none"><rect x="4" y="4" width="24" height="24" rx="4" stroke="currentColor" stroke-width="1.2" opacity="0.4"/><circle cx="16" cy="14" r="5" fill="currentColor" opacity="0.8"/><path d="M8 26 C8 21 12 18 16 18 C20 18 24 21 24 26" stroke="currentColor" stroke-width="1.5" fill="currentColor" opacity="0.2"/></svg>
        </div>
        <div class="brand-info">
          <span class="brand-text">HOST DESK</span>
          <span class="brand-sub">主持人工作台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/host" class="nav-item" :class="{ active: $route.path === '/host' }">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/host/sessions" class="nav-item" :class="{ active: $route.path === '/host/sessions' }">
          <el-icon><Calendar /></el-icon>
          <span>我的场次</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/host/reviews" class="nav-item" :class="{ active: $route.path === '/host/reviews' }">
          <el-icon><Star /></el-icon>
          <span>玩家评价</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/host/statistics" class="nav-item" :class="{ active: $route.path === '/host/statistics' }">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据面板</span>
          <span class="nav-glow" />
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="notification-trigger" @click="toggleNotification">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notif-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <span class="notif-label">通知</span>
        </div>
        
        <transition name="el-fade-in">
          <div v-if="showNotification" class="notification-panel">
            <div class="notif-header">
              <span class="notif-title">我的通知</span>
              <span v-if="unreadCount > 0" class="notif-action" @click="markAllRead">全部已读</span>
            </div>
            <div class="notif-list">
              <div v-for="notif in notifications" :key="notif.id" class="notif-item" :class="{ unread: notif.isRead === 0 }" @click="handleNotifClick(notif)">
                <div class="notif-item-icon">
                  <el-icon><component :is="getNotifIcon(notif.type)" /></el-icon>
                </div>
                <div class="notif-item-content">
                  <div class="notif-item-title">{{ notif.title }}</div>
                  <div class="notif-item-desc">{{ notif.content }}</div>
                  <div class="notif-item-time">{{ formatTime(notif.createTime) }}</div>
                </div>
              </div>
              <div v-if="notifications.length === 0" class="notif-empty">暂无通知</div>
            </div>
          </div>
        </transition>
        
        <div class="user-card" @click="toggleUserMenu">
          <el-avatar :src="userInfo?.avatar" :size="36">
            <el-icon :size="18"><User /></el-icon>
          </el-avatar>
          <div class="user-meta">
            <span class="user-name">{{ userInfo?.realName || userInfo?.username || '主持人' }}</span>
            <span class="user-role">HOST</span>
          </div>
        </div>
        <transition name="el-fade-in">
          <div v-if="showUserMenu" class="user-menu">
            <div class="menu-item" @click="goProfile">个人中心</div>
            <div class="menu-item danger" @click="handleLogout">退出工作台</div>
          </div>
        </transition>
      </div>
    </aside>

    <main class="sk-main">
      <router-view v-slot="{ Component, route }">
        <transition name="page-slide" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getNotificationList, getUnreadCount, markAllAsRead, markAsRead } from '@/api/notification'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const showUserMenu = ref(false)
const showNotification = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const toggleUserMenu = () => { showUserMenu.value = !showUserMenu.value }

const toggleNotification = async () => {
  showNotification.value = !showNotification.value
  if (showNotification.value) await loadNotifications()
}

const loadNotifications = async () => {
  try {
    const res = await getNotificationList()
    if (res.code === 200) notifications.value = res.data || []
    const countRes = await getUnreadCount()
    if (countRes.code === 200) unreadCount.value = countRes.data || 0
  } catch (e) { console.error(e) }
}

const markAllRead = async () => {
  try {
    const res = await markAllAsRead()
    if (res.code === 200) {
      unreadCount.value = 0
      notifications.value.forEach(n => n.isRead = 1)
    }
  } catch (e) { console.error(e) }
}

const handleNotifClick = async (notif) => {
  if (notif.isRead === 0) {
    try {
      await markAsRead(notif.id)
      notif.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { console.error(e) }
  }
  if (notif.sessionId) {
    router.push('/host/sessions')
    showNotification.value = false
  }
}

const getNotifIcon = (type) => {
  const icons = { 1: 'Bell', 2: 'UserFilled', 3: 'ShoppingCart', 4: 'Calendar', 5: 'Wallet' }
  return icons[type] || 'Bell'
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const goProfile = () => {
  showUserMenu.value = false
  router.push('/host/profile')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出工作台')
  router.push('/login')
}

onMounted(() => {
  loadNotifications()
  setInterval(loadNotifications, 60000)
})
</script>

<style scoped lang="scss">
.sk-layout {
  display: flex;
  min-height: 100vh;
  background: var(--sk-bg-deepest);
}

.sk-sidebar {
  width: 240px;
  min-height: 100vh;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid var(--sk-border);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 100;
}

.sidebar-brand {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--sk-border);

  .brand-icon {
    width: 32px; height: 32px;
    color: var(--sk-neon-purple);
    svg { width: 100%; height: 100%; }
  }

  .brand-info {
    display: flex;
    flex-direction: column;

    .brand-text {
      font-family: var(--sk-font-display);
      font-size: 14px;
      font-weight: 700;
      color: #1F2937;
      letter-spacing: 2px;
    }
    .brand-sub {
      font-size: 10px;
      color: var(--sk-text-muted);
      letter-spacing: 1px;
    }
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;

  .nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 10px;
    color: #6B7280;
    text-decoration: none;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;

    .el-icon { font-size: 18px; transition: all 0.3s; }

    .nav-glow {
      position: absolute; left: 0; top: 50%; transform: translateY(-50%);
      width: 3px; height: 0;
      background: var(--sk-neon-purple);
      border-radius: 0 3px 3px 0;
      transition: height 0.3s;
    }

    &:hover {
      color: #1F2937;
      background: rgba(119, 68, 204, 0.06);
    }

    &.active {
      color: #FFFFFF;
      background: var(--sk-neon-purple);
      .el-icon { filter: none; }
      .nav-glow { height: 60%; }
    }
  }
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--sk-border);
  position: relative;

  .notification-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.3s;
    margin-bottom: 8px;

    &:hover { background: rgba(119, 68, 204, 0.06); }

    .notif-badge {
      :deep(.el-badge__content) {
        background: var(--sk-neon-pink);
        border: none;
      }
    }

    .notif-label { font-size: 13px; color: #6B7280; }
  }

  .notification-panel {
    position: absolute;
    bottom: 140px;
    left: 12px;
    right: 12px;
    width: calc(100% - 24px);
    background: var(--sk-bg-card);
    border: 1px solid var(--sk-border);
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    z-index: 200;

    .notif-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid var(--sk-border);

      .notif-title { font-size: 14px; font-weight: 600; color: var(--sk-text-primary); }
      .notif-action { font-size: 12px; color: var(--sk-neon-purple); cursor: pointer; &:hover { opacity: 0.8; } }
    }

    .notif-list {
      max-height: 300px;
      overflow-y: auto;

      .notif-item {
        display: flex;
        gap: 10px;
        padding: 12px 16px;
        cursor: pointer;
        transition: background 0.2s;
        border-bottom: 1px solid var(--sk-border);

        &:last-child { border-bottom: none; }
        &:hover { background: rgba(119, 68, 204, 0.04); }
        &.unread { background: rgba(119, 68, 204, 0.04); .notif-item-title { color: var(--sk-neon-purple); } }

        .notif-item-icon {
          width: 32px; height: 32px; border-radius: 8px;
          background: rgba(119, 68, 204, 0.1);
          display: flex; align-items: center; justify-content: center;
          color: var(--sk-neon-purple); flex-shrink: 0;
        }

        .notif-item-content {
          flex: 1; min-width: 0;
          .notif-item-title { font-size: 13px; font-weight: 600; color: var(--sk-text-primary); margin-bottom: 2px; }
          .notif-item-desc { font-size: 12px; color: var(--sk-text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
          .notif-item-time { font-size: 11px; color: var(--sk-text-muted); margin-top: 4px; }
        }
      }

      .notif-empty { text-align: center; padding: 30px; color: var(--sk-text-muted); font-size: 13px; }
    }
  }

  .user-card {
    display: flex; align-items: center; gap: 10px;
    padding: 10px 12px; border-radius: 10px; cursor: pointer; transition: background 0.3s;
    &:hover { background: rgba(119, 68, 204, 0.06); }

    .el-avatar {
      border: 2px solid rgba(119, 68, 204, 0.2);
      background: var(--sk-bg-elevated);
    }

    .user-meta {
      display: flex; flex-direction: column;
      .user-name { font-size: 13px; color: #1F2937; font-weight: 500; }
      .user-role { font-size: 10px; color: var(--sk-neon-purple); font-family: var(--sk-font-display); letter-spacing: 1px; opacity: 0.7; }
    }
  }

  .user-menu {
    position: absolute; bottom: 72px; left: 12px; right: 12px;
    background: var(--sk-bg-card); border: 1px solid var(--sk-border);
    border-radius: 10px; overflow: hidden;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);

    .menu-item {
      padding: 10px 16px; color: #6B7280;
      font-size: 13px; cursor: pointer; transition: all 0.2s;
      &:hover { background: rgba(119, 68, 204, 0.08); color: var(--sk-neon-purple); }
      &.danger:hover { background: rgba(255, 23, 68, 0.08); color: var(--sk-neon-red); }
    }
  }
}

.sk-main {
  flex: 1;
  margin-left: 240px;
  padding: 28px;
  min-height: 100vh;
  background: var(--sk-bg-deepest);
}
</style>
