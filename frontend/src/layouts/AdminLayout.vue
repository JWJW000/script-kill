<template>
  <div class="sk-layout">
    <aside class="sk-sidebar">
      <div class="sidebar-brand" @click="$router.push('/admin')">
        <div class="brand-icon">
          <svg viewBox="0 0 32 32" fill="none"><path d="M16 2 L30 9 L30 23 L16 30 L2 23 L2 9 Z" stroke="currentColor" stroke-width="1.2" fill="currentColor" fill-opacity="0.08"/><circle cx="16" cy="16" r="5" fill="currentColor" opacity="0.8"/><path d="M16 6 L16 26 M6 11 L26 21 M6 21 L26 11" stroke="currentColor" stroke-width="0.6" opacity="0.3"/></svg>
        </div>
        <div class="brand-info">
          <span class="brand-text">ADMIN</span>
          <span class="brand-sub">系统管理中心</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item" :class="{ active: $route.path === '/admin' }">
          <el-icon><HomeFilled /></el-icon>
          <span>控制台</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path === '/admin/users' }">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/admin/scripts" class="nav-item" :class="{ active: $route.path === '/admin/scripts' }">
          <el-icon><Document /></el-icon>
          <span>剧本管理</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/admin/sessions" class="nav-item" :class="{ active: $route.path === '/admin/sessions' }">
          <el-icon><Calendar /></el-icon>
          <span>场次管理</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/admin/orders" class="nav-item" :class="{ active: $route.path === '/admin/orders' }">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
          <span class="nav-glow" />
        </router-link>
        <router-link to="/admin/statistics" class="nav-item" :class="{ active: $route.path === '/admin/statistics' }">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
          <span class="nav-glow" />
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card" @click="toggleUserMenu">
          <el-avatar :src="userInfo?.avatar" :size="36">
            <el-icon :size="18"><User /></el-icon>
          </el-avatar>
          <div class="user-meta">
            <span class="user-name">{{ userInfo?.realName || userInfo?.username || '管理员' }}</span>
            <span class="user-role">ADMIN</span>
          </div>
        </div>
        <transition name="el-fade-in">
          <div v-if="showUserMenu" class="user-menu">
            <div class="menu-item" @click="goProfile">个人中心</div>
            <div class="menu-item danger" @click="handleLogout">退出系统</div>
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
  router.push('/admin/profile')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出系统')
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
    color: var(--sk-neon-pink);
    svg { width: 100%; height: 100%; }
  }

  .brand-info {
    display: flex; flex-direction: column;
    .brand-text {
      font-family: var(--sk-font-display); font-size: 15px; font-weight: 700;
      color: #1F2937; letter-spacing: 3px;
    }
    .brand-sub { font-size: 10px; color: var(--sk-text-muted); letter-spacing: 1px; }
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;

  .nav-item {
    display: flex; align-items: center; gap: 12px;
    padding: 12px 16px; border-radius: 10px;
    color: #6B7280; text-decoration: none;
    font-size: 14px; font-weight: 500;
    transition: all 0.3s; position: relative; overflow: hidden;

    .el-icon { font-size: 18px; transition: all 0.3s; }

    .nav-glow {
      position: absolute; left: 0; top: 50%; transform: translateY(-50%);
      width: 3px; height: 0;
      background: var(--sk-neon-pink);
      border-radius: 0 3px 3px 0;
      transition: height 0.3s;
    }

    &:hover {
      color: #1F2937;
      background: rgba(204, 34, 102, 0.06);
    }

    &.active {
      color: var(--sk-neon-pink);
      background: rgba(204, 34, 102, 0.08);
      border: 1px solid rgba(204, 34, 102, 0.2);
      .el-icon { filter: none; }
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
    &:hover { background: rgba(204, 34, 102, 0.06); }

    .el-avatar { border: 2px solid rgba(204, 34, 102, 0.2); background: var(--sk-bg-elevated); }
    .user-meta {
      display: flex; flex-direction: column;
      .user-name { font-size: 13px; color: #1F2937; font-weight: 500; }
      .user-role { font-size: 10px; color: var(--sk-neon-pink); font-family: var(--sk-font-display); letter-spacing: 1px; opacity: 0.7; }
    }
  }

  .user-menu {
    position: absolute; bottom: 72px; left: 12px; right: 12px;
    background: var(--sk-bg-card); border: 1px solid var(--sk-border);
    border-radius: 10px; overflow: hidden; box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);

    .menu-item {
      padding: 10px 16px; color: #6B7280;
      font-size: 13px; cursor: pointer; transition: all 0.2s;
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
