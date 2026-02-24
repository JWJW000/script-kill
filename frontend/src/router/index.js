import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', transition: 'page-fade' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', transition: 'page-fade' }
  },
  {
    path: '/',
    redirect: '/player'
  },
  {
    path: '/player',
    component: () => import('@/layouts/PlayerLayout.vue'),
    meta: { requiresAuth: true, role: 'PLAYER', transition: 'page-fade' },
    children: [
      {
        path: '',
        name: 'PlayerHome',
        component: () => import('@/views/player/Home.vue'),
        meta: { title: '故事大厅' }
      },
      {
        path: 'scripts',
        name: 'PlayerScripts',
        component: () => import('@/views/player/Scripts.vue'),
        meta: { title: '剧本档案' }
      },
      {
        path: 'sessions',
        name: 'PlayerSessions',
        component: () => import('@/views/player/Sessions.vue'),
        meta: { title: '场次预约' }
      },
      {
        path: 'team-up',
        name: 'PlayerTeamUp',
        component: () => import('@/views/player/TeamUp.vue'),
        meta: { title: '组队拼场' }
      },
      {
        path: 'orders',
        name: 'PlayerOrders',
        component: () => import('@/views/player/Orders.vue'),
        meta: { title: '任务记录' }
      },
      {
        path: 'profile',
        name: 'PlayerProfile',
        component: () => import('@/views/player/Profile.vue'),
        meta: { title: '身份档案' }
      }
    ]
  },
  {
    path: '/host',
    component: () => import('@/layouts/HostLayout.vue'),
    meta: { requiresAuth: true, role: 'HOST', transition: 'page-fade' },
    children: [
      {
        path: '',
        name: 'HostHome',
        component: () => import('@/views/host/Home.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'sessions',
        name: 'HostSessions',
        component: () => import('@/views/host/Sessions.vue'),
        meta: { title: '我的场次' }
      },
      {
        path: 'reviews',
        name: 'HostReviews',
        component: () => import('@/views/host/Reviews.vue'),
        meta: { title: '玩家评价' }
      },
      {
        path: 'statistics',
        name: 'HostStatistics',
        component: () => import('@/views/host/Statistics.vue'),
        meta: { title: '数据面板' }
      },
      {
        path: 'profile',
        name: 'HostProfile',
        component: () => import('@/views/host/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN', transition: 'page-fade' },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('@/views/admin/Home.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'scripts',
        name: 'AdminScripts',
        component: () => import('@/views/admin/Scripts.vue'),
        meta: { title: '剧本管理' }
      },
      {
        path: 'sessions',
        name: 'AdminSessions',
        component: () => import('@/views/admin/Sessions.vue'),
        meta: { title: '场次管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('@/views/admin/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + 'SCRIPT KILL'
  
  const token = getToken()
  const userRole = localStorage.getItem('userRole')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.role && userRole !== to.meta.role) {
    next('/login')
  } else {
    next()
  }
})

export default router
