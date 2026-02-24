import { defineStore } from 'pinia'
import { login, register, getUserInfo } from '@/api/auth'
import { setToken, getToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,
    role: localStorage.getItem('userRole') || ''
  }),
  
  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      if (res.code === 200 && res.data && res.data.token) {
        this.token = res.data.token
        setToken(res.data.token)
        try {
          await this.getUserInfo()
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 即使获取用户信息失败，也允许登录，后续可以重试
        }
        return true
      }
      return false
    },
    
    async register(registerForm) {
      const res = await register(registerForm)
      return res.code === 200
    },
    
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        if (res.code === 200 && res.data) {
          this.userInfo = res.data
          this.role = res.data.role || ''
          if (res.data.role) {
            localStorage.setItem('userRole', res.data.role)
          }
        } else {
          console.error('获取用户信息失败:', res)
        }
      } catch (error) {
        console.error('获取用户信息异常:', error)
        throw error
      }
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      this.role = ''
      removeToken()
      localStorage.removeItem('userRole')
    }
  }
})
