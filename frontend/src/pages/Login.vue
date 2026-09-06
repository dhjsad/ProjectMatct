<template>
  <div class="page-wrap max-w-md">
    <h1 class="font-serif text-3xl">登录</h1>
    <form class="card p-6 mt-6 space-y-4" @submit.prevent="submit">
      <el-input v-model="username" placeholder="用户名" />
      <el-input v-model="password" type="password" placeholder="密码" show-password />
      <el-button native-type="submit" type="primary" color="#1c1915" class="w-full" :loading="loading">进入工坊</el-button>
    </form>
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
