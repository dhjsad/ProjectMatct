import { defineStore } from 'pinia'
import http from './api'

export const useSiteStore = defineStore('site', {
  state: () => ({
    banners: []
  }),
  getters: {
    top: (s) => s.banners.find((b) => b.slotKey === 'TOP') || {
      slotKey: 'TOP',
      content: '该作品只售卖一次，保证独一无二',
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif',
      fontSize: '13px',
      color: '#f5f5f7',
      bgColor: '#1d1d1f'
    },
    side: (s) => s.banners.find((b) => b.slotKey === 'SIDE') || {
      slotKey: 'SIDE',
      content: '独一无二 · 售出即下架',
      fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif',
      fontSize: '13px',
      color: '#1d1d1f',
      bgColor: '#f5f5f7'
    }
  },
  actions: {
    async refresh() {
      try {
        const res = await http.get('/site/banners', { skipErrorMessage: true })
        this.banners = res.data || []
      } catch {
        this.banners = []
      }
    }
  }
})
