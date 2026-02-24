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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const showUserMenu = ref(false)

const toggleUserMenu = () => { showUserMenu.value = !showUserMenu.value }

const goProfile = () => {
  showUserMenu.value = false
  router.push('/host/profile')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出工作台')
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
    filter: drop-shadow(0 0 8px rgba(180, 74, 255, 0.5));
    svg { width: 100%; height: 100%; }
  }

  .brand-info {
    display: flex;
    flex-direction: column;

    .brand-text {
      font-family: var(--sk-font-display);
      font-size: 14px;
      font-weight: 700;
      color: var(--sk-neon-purple);
      letter-spacing: 2px;
      text-shadow: 0 0 12px rgba(180, 74, 255, 0.4);
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
    color: var(--sk-text-muted);
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
      box-shadow: 0 0 8px rgba(180, 74, 255, 0.6);
    }

    &:hover {
      color: var(--sk-text-secondary);
      background: rgba(180, 74, 255, 0.04);
    }

    &.active {
      color: var(--sk-neon-purple);
      background: rgba(180, 74, 255, 0.08);
      .el-icon { filter: drop-shadow(0 0 6px rgba(180, 74, 255, 0.5)); }
      .nav-glow { height: 60%; }
    }
  }
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--sk-border);
  position: relative;

  .user-card {
    display: flex; align-items: center; gap: 10px;
    padding: 10px 12px; border-radius: 10px; cursor: pointer; transition: background 0.3s;
    &:hover { background: rgba(180, 74, 255, 0.06); }

    .el-avatar {
      border: 2px solid rgba(180, 74, 255, 0.3);
      background: var(--sk-bg-elevated);
    }

    .user-meta {
      display: flex; flex-direction: column;
      .user-name { font-size: 13px; color: var(--sk-text-primary); font-weight: 500; }
      .user-role { font-size: 10px; color: var(--sk-neon-purple); font-family: var(--sk-font-display); letter-spacing: 1px; opacity: 0.7; }
    }
  }

  .user-menu {
    position: absolute; bottom: 72px; left: 12px; right: 12px;
    background: var(--sk-bg-card); border: 1px solid var(--sk-border);
    border-radius: 10px; overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);

    .menu-item {
      padding: 10px 16px; color: var(--sk-text-secondary);
      font-size: 13px; cursor: pointer; transition: all 0.2s;
      &:hover { background: rgba(180, 74, 255, 0.08); color: var(--sk-neon-purple); }
      &.danger:hover { background: rgba(255, 23, 68, 0.08); color: var(--sk-neon-red); }
    }
  }
}

.sk-main {
  flex: 1;
  margin-left: 240px;
  padding: 28px;
  min-height: 100vh;
  background: radial-gradient(ellipse at 70% 10%, rgba(180, 74, 255, 0.03) 0%, transparent 50%),
              var(--sk-bg-deepest);
}
</style>
