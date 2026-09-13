import { defineStore } from 'pinia'

const KEY = 'pm_cart'

function read() {
  try {
    const raw = JSON.parse(localStorage.getItem(KEY) || '[]')
    return Array.isArray(raw) ? raw : []
  } catch {
    return []
  }
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: read()
  }),
  getters: {
    count: (s) => s.items.length,
    total: (s) => s.items.reduce((sum, item) => {
      const sale = Number(item.salePrice) || 0
      const guide = item.deliveryType === 'SERVICE' ? (Number(item.guidePrice) || 0) : 0
      return sum + sale + guide
    }, 0)
  },
  actions: {
    persist() {
      localStorage.setItem(KEY, JSON.stringify(this.items))
    },
    add(project, deliveryType = 'SELF_DOWNLOAD') {
      if (!project?.id) return false
      if ((project.remainingCount ?? 0) <= 0) return false
      if (this.items.some((item) => item.id === project.id)) return false
      this.items.push({
        id: project.id,
        name: project.name,
        description: project.description,
        techStack: project.techStack,
        salePrice: project.salePrice ?? 399,
        guidePrice: project.guidePrice ?? 299,
        deliveryType,
        contact: ''
      })
      this.persist()
      return true
    },
    setDelivery(id, deliveryType) {
      const item = this.items.find((row) => row.id === id)
      if (item) {
        item.deliveryType = deliveryType
        this.persist()
      }
    },
    setContact(id, contact) {
      const item = this.items.find((row) => row.id === id)
      if (item) {
        item.contact = contact
        this.persist()
      }
    },
    remove(id) {
      this.items = this.items.filter((item) => item.id !== id)
      this.persist()
    },
    clear() {
      this.items = []
      this.persist()
    }
  }
})
