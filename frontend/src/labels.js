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
  if (n > 0) return { text: '可领取', tone: 'ok', remaining: n }
  return { text: '已领完', tone: 'off', remaining: 0 }
}
