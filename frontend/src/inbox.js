import { defineStore } from 'pinia'
import http from './api'

export const useInboxStore = defineStore('inbox', {
  state: () => ({
    unread: 0,
    recommendations: []
  }),
  actions: {
    async refresh() {
      if (!localStorage.getItem('pm_token')) {
        this.unread = 0
        this.recommendations = []
        return
      }
      const [countRes, recRes] = await Promise.all([
        http.get('/messages/unread-count'),
        http.get('/recommendations')
      ])
      this.unread = countRes.data || 0
      this.recommendations = recRes.data || []
    }
  }
})
