import axios from 'axios'
import { ElMessage } from 'element-plus'

export async function downloadFile(url, fallbackName) {
  const token = localStorage.getItem('pm_token')
  try {
    const res = await axios.get(url, {
      baseURL: '/api',
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    const type = res.headers['content-type'] || ''
    if (type.includes('application/json')) {
      const text = await res.data.text()
      const body = JSON.parse(text)
      throw new Error(body.message || '下载失败')
    }
    const name = filenameFrom(res.headers['content-disposition']) || fallbackName
    const blob = new Blob([res.data])
    const href = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = href
    a.download = name
    a.click()
    URL.revokeObjectURL(href)
  } catch (err) {
    const msg = await messageOf(err)
    ElMessage.error(msg)
    throw err
  }
}

function filenameFrom(header) {
  if (!header) return ''
  const star = /filename\*=UTF-8''([^;]+)/i.exec(header)
  if (star) return decodeURIComponent(star[1])
  const plain = /filename="?([^"]+)"?/i.exec(header)
  return plain ? plain[1] : ''
}

async function messageOf(err) {
  const data = err.response?.data
  if (data instanceof Blob) {
    try {
      const body = JSON.parse(await data.text())
      return body.message || '下载失败'
    } catch (e) {
      return '下载失败'
    }
  }
  return err.response?.data?.message || err.message || '下载失败'
}
