<template>
  <div class="min-h-screen bg-paper">
    <div
      v-if="site.top"
      class="announce-bar"
      :style="bannerStyle(site.top)"
    >
      {{ site.top.content }}
    </div>

    <header class="site-header">
      <div class="mx-auto flex h-full max-w-[1120px] items-center gap-7 px-5 md:px-8">
        <router-link to="/" class="!text-white text-[15px] font-semibold tracking-tight">
          毕设工坊
        </router-link>
        <nav class="hidden md:flex items-center gap-6 text-[12px] tracking-wide">
          <router-link to="/">商店</router-link>
          <router-link to="/projects">商城</router-link>
          <router-link to="/custom">定制</router-link>
          <router-link to="/match">AI 匹配</router-link>
          <router-link v-if="auth.isAdmin" to="/admin">后台</router-link>
        </nav>
        <div class="ml-auto hidden md:flex items-center gap-4 text-[12px] tracking-wide">
          <router-link to="/cart" class="relative pr-1">
            购物袋
            <span
              v-if="cart.count"
              class="absolute -top-2 -right-3 min-w-[16px] h-[16px] px-1 rounded-full bg-[#0071e3] text-white text-[10px] leading-[16px] text-center"
            >{{ cart.count > 9 ? '9+' : cart.count }}</span>
          </router-link>
          <template v-if="auth.isLogin">
            <router-link to="/me" class="relative pr-1">
              消息
              <span
                v-if="inbox.unread"
                class="absolute -top-2 -right-3 min-w-[16px] h-[16px] px-1 rounded-full bg-[#0071e3] text-white text-[10px] leading-[16px] text-center"
              >{{ inbox.unread > 9 ? '9+' : inbox.unread }}</span>
            </router-link>
            <router-link to="/me">{{ auth.user?.username }}</router-link>
            <button type="button" @click="logout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register" class="!text-white">注册</router-link>
          </template>
        </div>
        <button
          type="button"
          class="nav-toggle"
          :class="{ open: menuOpen }"
          :aria-expanded="menuOpen"
          aria-label="打开菜单"
          @click="menuOpen = !menuOpen"
        >
          <i></i>
          <i></i>
        </button>
      </div>
    </header>

    <Transition name="menu">
      <nav v-if="menuOpen" class="mobile-panel md:hidden">
        <router-link to="/">商店</router-link>
        <router-link to="/projects">商城</router-link>
        <router-link to="/custom">定制</router-link>
        <router-link to="/match">AI 匹配</router-link>
        <router-link to="/cart">购物袋{{ cart.count ? ` · ${cart.count}` : '' }}</router-link>
        <router-link v-if="auth.isAdmin" to="/admin">后台</router-link>
        <template v-if="auth.isLogin">
          <router-link to="/me">消息{{ inbox.unread ? ` · ${inbox.unread}` : '' }}</router-link>
          <router-link to="/me">{{ auth.user?.username }}</router-link>
          <button type="button" class="mt-8 text-[#86868b]" @click="logout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </Transition>

    <div class="app-shell">
      <aside v-if="site.side" class="side-rail" :style="bannerStyle(site.side)">
        <p>{{ site.side.content }}</p>
      </aside>
      <div class="app-main">
        <router-view />
      </div>
    </div>

    <footer class="site-footer">
      <div class="mx-auto max-w-[1120px] px-5 md:px-8 py-12">
        <p class="text-[12px] text-[#6e6e73] pb-6 border-b border-[#d2d2d7]">
          每件作品只售一次。买下即可自行下载，或由客服发送并提供付费技术指导。
        </p>
        <div class="grid sm:grid-cols-3 gap-8 pt-8">
          <div>
            <p class="text-[#1d1d1f] font-semibold mb-3">毕设工坊</p>
            <p>独一无二的源码作品。售出即下架，保证你拿到的是唯一一份。</p>
          </div>
          <div class="space-y-2">
            <p class="text-[#1d1d1f] font-semibold mb-3">探索</p>
            <p><router-link class="hover:text-[#1d1d1f]" to="/projects">商城</router-link></p>
            <p><router-link class="hover:text-[#1d1d1f]" to="/custom">高端定制</router-link></p>
            <p><router-link class="hover:text-[#1d1d1f]" to="/match">AI 匹配</router-link></p>
          </div>
          <div class="space-y-2">
            <p class="text-[#1d1d1f] font-semibold mb-3">账户</p>
            <template v-if="auth.isLogin">
              <p><router-link class="hover:text-[#1d1d1f]" to="/me">我的工坊</router-link></p>
              <p><router-link class="hover:text-[#1d1d1f]" to="/cart">购物袋</router-link></p>
            </template>
            <template v-else>
              <p><router-link class="hover:text-[#1d1d1f]" to="/login">登录</router-link></p>
              <p><router-link class="hover:text-[#1d1d1f]" to="/register">创建账号</router-link></p>
            </template>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store'
import { useInboxStore } from './inbox'
import { useCartStore } from './cart'
import { useSiteStore } from './site'
import { sendHeartbeat } from './presence'

const auth = useAuthStore()
const inbox = useInboxStore()
const cart = useCartStore()
const site = useSiteStore()
const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)
let timer = null
let heartbeatTimer = null

onMounted(() => {
  auth.refreshMe().catch(() => auth.logout())
  site.refresh()
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
  document.body.style.overflow = ''
})

watch(() => auth.token, () => refreshInbox())
watch(() => route.fullPath, () => {
  menuOpen.value = false
  ping()
})
watch(menuOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

function bannerStyle(banner) {
  return {
    color: banner.color || '#1d1d1f',
    background: banner.bgColor || '#f5f5f7',
    fontFamily: banner.fontFamily || 'inherit',
    fontSize: banner.fontSize || '13px'
  }
}

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
  menuOpen.value = false
  router.push('/')
}
</script>
