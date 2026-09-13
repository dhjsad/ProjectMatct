<template>
  <div class="auth-stage">
    <div class="auth-card">
      <h1 class="text-[40px] font-semibold tracking-tight text-center">登录</h1>
      <p class="mt-2 text-center text-mute">使用你的工坊账号继续</p>
      <form class="mt-8 space-y-4" @submit.prevent="submit">
        <el-input v-model="username" placeholder="用户名" />
        <el-input v-model="password" type="password" placeholder="密码" show-password />
        <el-button native-type="submit" type="primary" color="#0071e3" class="w-full" :loading="loading">继续</el-button>
      </form>
      <p class="mt-6 text-center text-[14px] text-mute">
        还没有账号？
        <router-link to="/register" class="link-more !text-[14px]">创建账号</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api'
import { useAuthStore } from '../store'

const username = ref('')
const password = ref('')
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function submit() {
  loading.value = true
  try {
    const res = await http.post('/auth/login', { username: username.value, password: password.value })
    auth.setSession(res.data)
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>
