export const difficultyLabel = {
  easy: '入门',
  medium: '中等',
  hard: '较难'
}

export const skillLabel = {
  beginner: '入门',
  junior: '初级',
  intermediate: '中级',
  senior: '进阶'
}

export function claimStatus(project) {
  const n = project?.remainingCount ?? 0
  if (n > 0) return { text: '可购买', tone: 'ok', remaining: n }
  return { text: '已售出', tone: 'off', remaining: 0 }
}

export function sold(project) {
  return (project?.remainingCount ?? 0) <= 0
}

export function money(value) {
  return `¥${Number(value || 0)}`
}

export const deliveryLabel = {
  SELF_DOWNLOAD: '自行下载源码',
  SERVICE: '客服发送 + 技术指导'
}

export const customStatusLabel = {
  PENDING: '待接洽',
  IN_PROGRESS: '沟通中',
  DONE: '已完成',
  CANCELLED: '已取消'
}
