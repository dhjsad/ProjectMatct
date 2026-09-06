import { defineStore } from 'pinia'
import http from './api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('pm_token') || '',
    user: JSON.parse(localStorage.getItem('pm_user') || 'null')
  }),
  getters: {
    isLogin: (s) => Boolean(s.token),
    isAdmin: (s) => s.user?.role === 'ADMIN'
  },
  actions: {
    setSession(payload) {
      this.token = payload.token
      this.user = { userId: payload.userId, username: payload.username, role: payload.role }
      localStorage.setItem('pm_token', payload.token)
      localStorage.setItem('pm_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('pm_token')
      localStorage.removeItem('pm_user')
    },
    async refreshMe() {
      if (!this.token) return
      const res = await http.get('/auth/me')
      this.user = { userId: res.data.id, username: res.data.username, role: res.data.role, profile: res.data.profile }
      localStorage.setItem('pm_user', JSON.stringify(this.user))
    }
  }
})
