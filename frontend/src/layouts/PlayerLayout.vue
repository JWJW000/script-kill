<template>
  <div class="sk-layout">
    <aside class="sk-sidebar">
      <div class="sidebar-brand" @click="$router.push('/player')">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="14" stroke="currentColor" stroke-width="1.2" opacity="0.4"/><circle cx="16" cy="16" r="5" fill="currentColor" opacity="0.9"/><path d="M16 4 L19 12 L28 14 L22 20 L23 28 L16 24 L9 28 L10 20 L4 14 L13 12 Z" fill="currentColor" opacity="0.12" stroke="currentColor" stroke-width="0.8"/></svg>
        </div>
        <span class="brand-text">SCRIPT KILL</span>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/player" class="nav-item" :class="{ active: $route.path === '/player' }" exact>
          <el-icon><HomeFilled /></el-icon>
          <span>故事大厅</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/player/scripts" class="nav-item" :class="{ active: $route.path === '/player/scripts' }">
          <el-icon><Document /></el-icon>
          <span>剧本档案</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/player/sessions" class="nav-item" :class="{ active: $route.path === '/player/sessions' }">
          <el-icon><Calendar /></el-icon>
          <span>场次预约</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/player/team-up" class="nav-item" :class="{ active: $route.path === '/player/team-up' }">
          <el-icon><UserFilled /></el-icon>
          <span>组队拼场</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/player/orders" class="nav-item" :class="{ active: $route.path === '/player/orders' }">
          <el-icon><List /></el-icon>
          <span>任务记录</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/player/profile" class="nav-item" :class="{ active: $route.path === '/player/profile' }">
          <el-icon><User /></el-icon>
          <span>身份档案</span>
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
            <span class="user-name">{{ userInfo?.realName || userInfo?.username || '未知玩家' }}</span>
            <span class="user-role">PLAYER</span>
          </div>
        </div>
        <transition name="el-fade-in">
          <div v-if="showUserMenu" class="user-menu">
            <div class="menu-item" @click="$router.push('/player/profile'); showUserMenu = false">个人设置</div>
            <div class="menu-item danger" @click="handleLogout">退出剧本</div>
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
    
    <div class="ai-float-btn" @click="showAiChat = true">
      <el-icon :size="24"><MagicStick /></el-icon>
    </div>
    
    <el-dialog v-model="showAiChat" title="AI 小助手" width="480px" :close-on-click-modal="true">
      <div class="ai-chat-container">
        <div class="ai-messages" ref="aiMessagesRef">
          <div v-for="(msg, i) in aiMessages" :key="i" class="ai-message" :class="msg.role">
            <div class="msg-avatar">
              <el-icon v-if="msg.role === 'assistant'" :size="18"><MagicStick /></el-icon>
              <el-icon v-else :size="18"><User /></el-icon>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="aiLoading" class="ai-message assistant">
            <div class="msg-avatar"><el-icon :size="18"><MagicStick /></el-icon></div>
            <div class="msg-content typing">思考中...</div>
          </div>
        </div>
        <div class="ai-input-area">
          <el-input v-model="aiInput" placeholder="输入你的问题..." size="default" @keyup.enter="sendAiMessage" :disabled="aiLoading" />
          <el-button type="primary" @click="sendAiMessage" :disabled="aiLoading || !aiInput.trim()">发送</el-button>
        </div>
        <div class="ai-suggestions">
          <span class="suggestion-label">快捷问题：</span>
          <span v-for="s in aiSuggestions" :key="s" class="suggestion-chip" @click="aiInput = s; sendAiMessage()">{{ s }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Bell, Message, ShoppingCart, Calendar, Wallet, MagicStick } from '@element-plus/icons-vue'
import { getNotificationList, getUnreadCount, markAllAsRead, markAsRead } from '@/api/notification'
import { chatWithAI } from '@/api/ai'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const showUserMenu = ref(false)
const showNotification = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const showAiChat = ref(false)
const aiInput = ref('')
const aiLoading = ref(false)
const aiMessages = ref([
  { role: 'assistant', content: '你好！我是 ScriptKiller AI 小助手，有什么可以帮你的吗？' }
])
const aiSuggestions = ['推荐一个6人恐怖本', '新手适合玩什么剧本？', '如何成为一个好的主持人？']
const aiMessagesRef = ref(null)

const sendAiMessage = async () => {
  if (!aiInput.value.trim() || aiLoading.value) return
  const question = aiInput.value.trim()
  aiMessages.value.push({ role: 'user', content: question })
  aiInput.value = ''
  aiLoading.value = true
  scrollAiMessages()
  try {
    const res = await chatWithAI(question)
    if (res.code === 200) {
      aiMessages.value.push({ role: 'assistant', content: res.data })
    } else {
      aiMessages.value.push({ role: 'assistant', content: '抱歉，' + (res.message || 'AI服务暂时不可用') })
    }
  } catch (e) {
    aiMessages.value.push({ role: 'assistant', content: '抱歉，网络异常，请稍后再试' })
  } finally {
    aiLoading.value = false
    scrollAiMessages()
  }
}

const scrollAiMessages = () => {
  setTimeout(() => {
    if (aiMessagesRef.value) {
      aiMessagesRef.value.scrollTop = aiMessagesRef.value.scrollHeight
    }
  }, 50)
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const toggleNotification = async () => {
  showNotification.value = !showNotification.value
  if (showNotification.value) {
    await loadNotifications()
  }
}

const loadNotifications = async () => {
  try {
    const res = await getNotificationList()
    if (res.code === 200) {
      notifications.value = res.data || []
    }
    const countRes = await getUnreadCount()
    if (countRes.code === 200) {
      unreadCount.value = countRes.data || 0
    }
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
  if (notif.orderId) {
    router.push('/player/orders')
    showNotification.value = false
  } else if (notif.teamUpId) {
    router.push('/player/team-up')
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

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已安全退出')
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
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
}

.sidebar-brand {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--sk-border);

  .brand-icon {
    width: 32px;
    height: 32px;
    color: var(--sk-neon-cyan);

    svg { width: 100%; height: 100%; }
  }

  .brand-text {
    font-family: var(--sk-font-display);
    font-size: 15px;
    font-weight: 700;
    color: #1F2937;
    letter-spacing: 2px;
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
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    .el-icon { font-size: 18px; transition: all 0.3s; }

    .nav-glow {
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 0;
      background: var(--sk-neon-cyan);
      border-radius: 0 3px 3px 0;
      transition: height 0.3s;
    }

    &:hover {
      color: #1F2937;
      background: rgba(0, 153, 204, 0.06);
    }

    &.active {
      color: #FFFFFF;
      background: var(--sk-neon-cyan);

      .el-icon {
        filter: none;
      }

      .nav-glow {
        height: 60%;
      }
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

    &:hover {
      background: rgba(0, 153, 204, 0.06);
    }

    .notif-badge {
      :deep(.el-badge__content) {
        background: var(--sk-neon-pink);
        border: none;
      }
    }

    .notif-label {
      font-size: 13px;
      color: #6B7280;
    }
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

      .notif-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--sk-text-primary);
      }

      .notif-action {
        font-size: 12px;
        color: var(--sk-neon-cyan);
        cursor: pointer;
        &:hover { opacity: 0.8; }
      }
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
        &:hover { background: rgba(0, 153, 204, 0.04); }

        &.unread {
          background: rgba(0, 153, 204, 0.04);
          .notif-item-title { color: var(--sk-neon-cyan); }
        }

        .notif-item-icon {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          background: rgba(0, 153, 204, 0.1);
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--sk-neon-cyan);
          flex-shrink: 0;
        }

        .notif-item-content {
          flex: 1;
          min-width: 0;

          .notif-item-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--sk-text-primary);
            margin-bottom: 2px;
          }

          .notif-item-desc {
            font-size: 12px;
            color: var(--sk-text-muted);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .notif-item-time {
            font-size: 11px;
            color: var(--sk-text-muted);
            margin-top: 4px;
          }
        }
      }

      .notif-empty {
        text-align: center;
        padding: 30px;
        color: var(--sk-text-muted);
        font-size: 13px;
      }
    }
  }

  .user-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 153, 204, 0.06);
    }

    .el-avatar {
      border: 2px solid rgba(0, 153, 204, 0.2);
      background: var(--sk-bg-elevated);
    }

    .user-meta {
      display: flex;
      flex-direction: column;

      .user-name {
        font-size: 13px;
        color: #1F2937;
        font-weight: 500;
      }
      .user-role {
        font-size: 10px;
        color: var(--sk-neon-cyan);
        font-family: var(--sk-font-display);
        letter-spacing: 1px;
        opacity: 0.7;
      }
    }
  }

  .user-menu {
    position: absolute;
    bottom: 72px;
    left: 12px;
    right: 12px;
    background: var(--sk-bg-card);
    border: 1px solid var(--sk-border);
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);

    .menu-item {
      padding: 10px 16px;
      color: #6B7280;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(0, 153, 204, 0.08);
        color: var(--sk-neon-cyan);
      }

      &.danger:hover {
        background: rgba(255, 23, 68, 0.08);
        color: var(--sk-neon-red);
      }
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

// AI 小助手
.ai-float-btn {
  position: fixed;
  right: 28px;
  bottom: 28px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--sk-bg-card);
  border: 1px solid var(--sk-border);
  color: var(--sk-neon-cyan);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }
}

.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;

  .ai-messages {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 12px;

    .ai-message {
      display: flex;
      gap: 10px;
      max-width: 85%;

      &.user {
        align-self: flex-end;
        flex-direction: row-reverse;

        .msg-content {
          background: rgba(0, 153, 204, 0.1);
          color: #1F2937;
          border-radius: 12px 12px 4px 12px;
        }
      }

      &.assistant {
        align-self: flex-start;

        .msg-content {
          background: rgba(119, 68, 204, 0.1);
          color: #1F2937;
          border-radius: 12px 12px 12px 4px;
        }
      }

      .msg-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: rgba(0, 153, 204, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--sk-neon-cyan);
        flex-shrink: 0;
      }

      .msg-content {
        padding: 10px 14px;
        font-size: 14px;
        line-height: 1.5;
        white-space: pre-wrap;
        word-break: break-word;

        &.typing {
          color: var(--sk-text-muted);
          font-style: italic;
        }
      }
    }
  }

  .ai-input-area {
    display: flex;
    gap: 10px;
    padding: 12px;
    border-top: 1px solid var(--sk-border);
  }

  .ai-suggestions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0 12px 12px;
    align-items: center;

    .suggestion-label {
      font-size: 12px;
      color: var(--sk-text-muted);
    }

    .suggestion-chip {
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 6px;
      background: rgba(0, 153, 204, 0.08);
      border: 1px solid rgba(0, 153, 204, 0.2);
      color: var(--sk-neon-cyan);
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(0, 153, 204, 0.15);
      }
    }
  }
}
</style>
