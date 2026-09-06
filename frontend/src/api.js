import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('pm_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || err.message || '网络错误'
    const silent = err.config?.skipErrorMessage
    if (status === 401 && !silent) {
      localStorage.removeItem('pm_token')
      localStorage.removeItem('pm_user')
    }
    if (!silent) {
      ElMessage.error(msg)
    }
    return Promise.reject(new Error(msg))
  }
)

export default http
