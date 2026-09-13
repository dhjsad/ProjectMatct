<template>
  <div class="auth-stage">
    <div class="auth-card">
      <h1 class="text-[40px] font-semibold tracking-tight text-center">创建账号</h1>
      <p class="mt-2 text-center text-mute">用于购买作品、下载源码和提交定制</p>
      <form class="mt-8 space-y-4" @submit.prevent="submit">
        <el-input v-model="username" placeholder="用户名 3-32 位" />
        <el-input v-model="email" placeholder="邮箱（可选）" />
        <el-input v-model="password" type="password" placeholder="密码至少 6 位" show-password />
        <el-button native-type="submit" type="primary" color="#1d1d1f" class="w-full" :loading="loading">注册</el-button>
      </form>
      <p class="mt-6 text-center text-[14px] text-mute">
        已有账号？
        <router-link to="/login" class="link-more !text-[14px]">登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api'
import { useAuthStore } from '../store'

const username = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  loading.value = true
  try {
    const res = await http.post('/auth/register', { username: username.value, email: email.value, password: password.value })
    auth.setSession(res.data)
    router.push('/me')
  } finally {
    loading.value = false
  }
}
</script>
