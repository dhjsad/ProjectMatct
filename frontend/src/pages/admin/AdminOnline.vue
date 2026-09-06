<template>
  <div class="page-wrap">
    <AdminNav />
    <div class="flex items-end justify-between gap-4">
      <div>
        <h1 class="font-serif text-2xl">访问统计</h1>
        <p class="mt-1 text-sm text-ink/60">2 分钟内有心跳视为在线，切页会计入今日浏览次数。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="mt-6 grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <article class="card p-5">
        <p class="text-sm text-ink/60">当前在线</p>
        <p class="font-serif text-3xl mt-1">{{ stats.onlineCount || 0 }}</p>
        <p class="mt-2 text-xs text-moss">用户 {{ stats.onlineUserCount || 0 }} · 访客 {{ stats.onlineGuestCount || 0 }}</p>
      </article>
      <article class="card p-5">
        <p class="text-sm text-ink/60">今日访问人数</p>
        <p class="font-serif text-3xl mt-1">{{ stats.todayVisitors || 0 }}</p>
        <p class="mt-2 text-xs text-ink/50">独立浏览器 / 设备</p>
      </article>
      <article class="card p-5">
        <p class="text-sm text-ink/60">累计访问人数</p>
        <p class="font-serif text-3xl mt-1">{{ stats.totalVisitors || 0 }}</p>
        <p class="mt-2 text-xs text-ink/50">浏览 {{ stats.todayPageViews || 0 }} 次今日 · {{ stats.totalPageViews || 0 }} 次累计</p>
      </article>
    </div>

    <h2 class="font-serif text-xl mt-10">当前在线</h2>
    <el-table :data="stats.onlineSessions || []" class="mt-4" stripe empty-text="当前没有在线会话">
      <el-table-column label="身份" width="140">
        <template #default="{ row }">{{ row.guest ? '访客' : (row.username || '用户') }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="path" label="当前页面" />
      <el-table-column prop="ip" label="IP" width="150" />
      <el-table-column label="最近心跳" width="180">
        <template #default="{ row }">{{ formatTime(row.lastSeen) }}</template>
      </el-table-column>
    </el-table>

    <h2 class="font-serif text-xl mt-10">今日访问</h2>
    <el-table :data="stats.todayVisits || []" class="mt-4" stripe empty-text="今天还没有访问记录">
      <el-table-column label="身份" width="140">
        <template #default="{ row }">{{ row.username || '访客' }}</template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="path" label="最近页面" />
      <el-table-column prop="pageViews" label="浏览次数" width="100" />
      <el-table-column prop="ip" label="IP" width="150" />
      <el-table-column label="最近访问" width="180">
        <template #default="{ row }">{{ formatTime(row.lastSeen) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const stats = ref({})
let timer = null

onMounted(() => {
  load()
  timer = setInterval(load, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

async function load() {
  stats.value = (await http.get('/admin/online')).data || {}
}

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}
</script>
