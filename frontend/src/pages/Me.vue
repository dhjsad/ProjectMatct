<template>
  <div class="page-wrap">
    <h1 class="font-serif text-3xl">我的工坊</h1>
    <div class="mt-8 grid lg:grid-cols-2 gap-6">
      <div class="space-y-6">
      <form class="card p-6 space-y-3" @submit.prevent="save">
        <h2 class="font-serif text-xl">技术画像</h2>
        <el-input v-model="profile.major" placeholder="专业" />
        <el-select v-model="profile.skillLevel" placeholder="水平" class="w-full">
          <el-option label="入门" value="beginner" />
          <el-option label="初级" value="junior" />
          <el-option label="中级" value="intermediate" />
        </el-select>
        <el-input v-model="profile.techStack" placeholder="技术栈，逗号分隔" />
        <el-input v-model="profile.interests" placeholder="兴趣方向" />
        <el-select v-model="profile.expectedDifficulty" placeholder="希望难度" class="w-full">
          <el-option label="入门" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="较难" value="hard" />
        </el-select>
        <el-input-number v-model="profile.expectedDuration" :min="7" :max="120" class="!w-full" />
        <el-input v-model="profile.bio" type="textarea" rows="3" placeholder="补充说明" />
        <el-button native-type="submit" type="primary" color="#b5693b">保存画像</el-button>
      </form>

      <form class="card p-6 space-y-3" @submit.prevent="changePassword">
        <h2 class="font-serif text-xl">修改密码</h2>
        <el-input v-model="pwd.oldPassword" type="password" placeholder="原密码" show-password />
        <el-input v-model="pwd.newPassword" type="password" placeholder="新密码至少 6 位" show-password />
        <el-input v-model="pwd.confirmPassword" type="password" placeholder="确认新密码" show-password />
        <el-button native-type="submit" type="primary" color="#1c1915">更新密码</el-button>
      </form>
      </div>

      <div class="space-y-6">
        <section class="card p-6">
          <h2 class="font-serif text-xl">主动推荐</h2>
          <ul class="mt-3 space-y-4 text-sm">
            <li v-for="item in recommendations" :key="item.id">
              <div class="flex justify-between gap-3">
                <router-link class="font-medium" :to="`/projects/${item.projectId}`">{{ item.projectName }}</router-link>
                <span class="text-copper">{{ item.matchScore }}%</span>
              </div>
              <ul class="mt-2 text-moss space-y-1">
                <li v-for="reason in item.reasons" :key="reason">✓ {{ reason }}</li>
              </ul>
            </li>
            <li v-if="!recommendations.length" class="text-ink/50">还没有推送。完善画像后，管理员发布新项目时会通知你。</li>
          </ul>
        </section>
        <section class="card p-6">
          <h2 class="font-serif text-xl">远程部署订单</h2>
          <ul class="mt-3 text-sm space-y-2">
            <li v-for="o in orders" :key="o.id">
              项目 #{{ o.projectId }} · ¥{{ o.amount }} · {{ orderLabel[o.status] || o.status }}
              <router-link class="ml-2 text-copper" :to="`/projects/${o.projectId}`">查看</router-link>
            </li>
            <li v-if="!orders.length" class="text-ink/50">还没有部署订单。项目页可申请付费远程协助。</li>
          </ul>
        </section>
        <section class="card p-6">
          <h2 class="font-serif text-xl">领取记录</h2>
          <ul class="mt-3 text-sm space-y-2">
            <li v-for="c in claims" :key="c.id">
              项目 #{{ c.projectId }} · {{ c.claimTime }}
              <router-link class="ml-2 text-copper" :to="`/projects/${c.projectId}`">查看</router-link>
            </li>
            <li v-if="!claims.length" class="text-ink/50">还没有领取记录</li>
          </ul>
        </section>
        <section class="card p-6">
          <h2 class="font-serif text-xl">站内消息</h2>
          <ul class="mt-3 text-sm space-y-3">
            <li v-for="m in messages" :key="m.id">
              <button class="text-left w-full" @click="read(m)">
                <span :class="m.readFlag ? 'text-ink/50' : 'font-medium'">{{ m.title }}</span>
                <p class="text-ink/70 mt-1 whitespace-pre-wrap">{{ m.content }}</p>
              </button>
              <router-link v-if="m.projectId" class="text-copper text-xs" :to="`/projects/${m.projectId}`">查看项目</router-link>
            </li>
            <li v-if="!messages.length" class="text-ink/50">暂无消息</li>
          </ul>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api'
import { useAuthStore } from '../store'
import { useInboxStore } from '../inbox'

const auth = useAuthStore()
const profile = reactive({
  major: '',
  skillLevel: 'junior',
  techStack: '',
  interests: '',
  expectedDifficulty: 'medium',
  expectedDuration: 40,
  bio: ''
})
const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const claims = ref([])
const messages = ref([])
const recommendations = ref([])
const orders = ref([])
const inbox = useInboxStore()
const orderLabel = {
  PENDING: '待支付',
  PAID: '已支付待处理',
  IN_PROGRESS: '部署中',
  DONE: '已完成',
  CANCELLED: '已取消'
}

onMounted(async () => {
  const me = await http.get('/auth/me')
  Object.assign(profile, me.data.profile || {})
  auth.user = { ...auth.user, profile: me.data.profile }
  const settled = await Promise.allSettled([
    http.get('/projects/mine/claims'),
    http.get('/messages'),
    http.get('/recommendations'),
    http.get('/deploy-orders/mine')
  ])
  if (settled[0].status === 'fulfilled') claims.value = settled[0].value.data || []
  if (settled[1].status === 'fulfilled') messages.value = settled[1].value.data || []
  if (settled[2].status === 'fulfilled') recommendations.value = settled[2].value.data || []
  if (settled[3].status === 'fulfilled') orders.value = settled[3].value.data || []
  inbox.refresh().catch(() => {})
})

async function save() {
  await http.put('/auth/profile', profile)
  ElMessage.success('画像已保存')
}

async function changePassword() {
  if (!pwd.newPassword || pwd.newPassword.length < 6) {
    ElMessage.error('新密码至少 6 位')
    return
  }
  if (pwd.newPassword !== pwd.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  await http.put('/auth/password', {
    oldPassword: pwd.oldPassword,
    newPassword: pwd.newPassword
  })
  pwd.oldPassword = ''
  pwd.newPassword = ''
  pwd.confirmPassword = ''
  ElMessage.success('密码已更新')
}

async function read(m) {
  if (!m.readFlag) {
    await http.post(`/messages/${m.id}/read`)
    m.readFlag = 1
    inbox.refresh().catch(() => {})
  }
}
</script>
