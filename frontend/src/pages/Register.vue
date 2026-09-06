<template>
  <div class="page-wrap max-w-md">
    <h1 class="font-serif text-3xl">注册</h1>
    <form class="card p-6 mt-6 space-y-4" @submit.prevent="submit">
      <el-input v-model="username" placeholder="用户名 3-32 位" />
      <el-input v-model="email" placeholder="邮箱（可选）" />
      <el-input v-model="password" type="password" placeholder="密码至少 6 位" show-password />
      <el-button native-type="submit" type="primary" color="#1c1915" class="w-full" :loading="loading">创建账号</el-button>
    </form>
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
