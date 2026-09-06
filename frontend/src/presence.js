import http from './api'

const KEY = 'pm_visitor_id'

export function visitorId() {
  let id = localStorage.getItem(KEY)
  if (!id) {
    id = (typeof crypto !== 'undefined' && crypto.randomUUID)
      ? crypto.randomUUID()
      : `v-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
    localStorage.setItem(KEY, id)
  }
  return id
}

export function sendHeartbeat(path) {
  return http.post('/presence/heartbeat', {
    visitorId: visitorId(),
    path: path || '/'
  }, { skipErrorMessage: true }).catch(() => {})
}
