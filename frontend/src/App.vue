<template>
  <div class="min-h-screen">
    <header class="border-b border-black/10 bg-white/70 backdrop-blur sticky top-0 z-20">
      <div class="page-wrap !py-3 flex items-center gap-6">
        <router-link to="/" class="font-serif text-xl tracking-tight">
          毕设工坊
          <span class="ml-2 text-xs font-sans text-copper">ProjectMatch AI</span>
        </router-link>
        <nav class="hidden md:flex items-center gap-5 text-sm text-ink/80">
          <router-link to="/match">AI 匹配</router-link>
          <router-link to="/projects">项目库</router-link>
          <router-link to="/articles">选题文章</router-link>
          <router-link v-if="auth.isAdmin" to="/admin">后台</router-link>
        </nav>
        <div class="ml-auto flex items-center gap-3 text-sm">
          <template v-if="auth.isLogin">
            <router-link to="/me" class="relative pr-2">
              消息
              <span
                v-if="inbox.unread"
                class="absolute -top-2 -right-2 min-w-[18px] h-[18px] px-1 rounded-full bg-copper text-white text-[10px] leading-[18px] text-center"
              >{{ inbox.unread > 9 ? '9+' : inbox.unread }}</span>
            </router-link>
            <router-link to="/me">{{ auth.user?.username }}</router-link>
            <button class="text-ink/60" @click="logout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register" class="px-3 py-1.5 rounded-full bg-ink text-paper">注册</router-link>
          </template>
        </div>
      </div>
    </header>
    <router-view />
    <footer class="mt-16 border-t border-black/10 py-10 text-sm text-ink/60">
      <div class="page-wrap !py-0 grid md:grid-cols-2 gap-6">
        <p>学习样本与开发辅助，不是“下载即可提交”的源码站。请理解架构后自行实现与修改。</p>
        <p class="md:text-right">面向计算机专业学生与初级开发者 · 资源须合法授权才开放领取</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store'
import { useInboxStore } from './inbox'
import { sendHeartbeat } from './presence'

const auth = useAuthStore()
const inbox = useInboxStore()
const router = useRouter()
const route = useRoute()
let timer = null
let heartbeatTimer = null

onMounted(() => {
  auth.refreshMe().catch(() => auth.logout())
  refreshInbox()
  ping()
  timer = setInterval(refreshInbox, 20000)
  heartbeatTimer = setInterval(ping, 25000)
  document.addEventListener('visibilitychange', onVisible)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  document.removeEventListener('visibilitychange', onVisible)
})

watch(() => auth.token, () => refreshInbox())
watch(() => route.fullPath, () => ping())

function ping() {
  sendHeartbeat(route.fullPath)
}

function onVisible() {
  if (document.visibilityState === 'visible') {
    ping()
  }
}

function refreshInbox() {
  if (!auth.isLogin) {
    inbox.unread = 0
    inbox.recommendations = []
    return
  }
  inbox.refresh().catch(() => {})
}

function logout() {
  auth.logout()
  inbox.unread = 0
  inbox.recommendations = []
  router.push('/')
}
</script>
