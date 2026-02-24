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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const showUserMenu = ref(false)

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已安全退出')
  router.push('/login')
}
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
  background: linear-gradient(180deg, rgba(10, 14, 30, 0.98), rgba(6, 8, 18, 0.98));
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
    filter: drop-shadow(0 0 8px rgba(0, 229, 255, 0.5));

    svg { width: 100%; height: 100%; }
  }

  .brand-text {
    font-family: var(--sk-font-display);
    font-size: 15px;
    font-weight: 700;
    color: var(--sk-neon-cyan);
    letter-spacing: 2px;
    text-shadow: 0 0 12px rgba(0, 229, 255, 0.4);
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
    color: var(--sk-text-muted);
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
      box-shadow: 0 0 8px rgba(0, 229, 255, 0.6);
    }

    &:hover {
      color: var(--sk-text-secondary);
      background: rgba(0, 229, 255, 0.04);
    }

    &.active {
      color: var(--sk-neon-cyan);
      background: rgba(0, 229, 255, 0.08);

      .el-icon {
        filter: drop-shadow(0 0 6px rgba(0, 229, 255, 0.5));
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

  .user-card {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background: rgba(0, 229, 255, 0.06);
    }

    .el-avatar {
      border: 2px solid rgba(0, 229, 255, 0.3);
      background: var(--sk-bg-elevated);
    }

    .user-meta {
      display: flex;
      flex-direction: column;

      .user-name {
        font-size: 13px;
        color: var(--sk-text-primary);
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
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);

    .menu-item {
      padding: 10px 16px;
      color: var(--sk-text-secondary);
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(0, 229, 255, 0.08);
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
  background: radial-gradient(ellipse at 70% 10%, rgba(0, 229, 255, 0.03) 0%, transparent 50%),
              radial-gradient(ellipse at 30% 90%, rgba(180, 74, 255, 0.02) 0%, transparent 50%),
              var(--sk-bg-deepest);
}
</style>
