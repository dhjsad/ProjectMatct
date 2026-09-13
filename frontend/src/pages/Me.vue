<template>
  <div class="page-wrap">
    <p class="eyebrow">账户</p>
    <h1 class="page-title">我的工坊</h1>
    <p class="page-kicker">画像、消息、购买记录和定制咨询都在这里。</p>

    <div class="mt-12 grid lg:grid-cols-2 gap-6">
      <div class="space-y-6">
        <form class="card p-7 space-y-3" @submit.prevent="save">
          <h2 class="text-[21px] font-semibold tracking-tight">技术画像</h2>
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
          <el-button native-type="submit" type="primary" color="#0071e3">保存画像</el-button>
        </form>

        <form class="card p-7 space-y-3" @submit.prevent="changePassword">
          <h2 class="text-[21px] font-semibold tracking-tight">修改密码</h2>
          <el-input v-model="pwd.oldPassword" type="password" placeholder="原密码" show-password />
          <el-input v-model="pwd.newPassword" type="password" placeholder="新密码至少 6 位" show-password />
          <el-input v-model="pwd.confirmPassword" type="password" placeholder="确认新密码" show-password />
          <el-button native-type="submit" type="primary" color="#1d1d1f">更新密码</el-button>
        </form>
      </div>

      <div class="space-y-6">
        <section class="card p-7">
          <h2 class="text-[21px] font-semibold tracking-tight">主动推荐</h2>
          <ul class="mt-4 space-y-5 text-[15px]">
            <li v-for="item in recommendations" :key="item.id">
              <div class="flex justify-between gap-3">
                <router-link class="font-medium tracking-tight" :to="`/projects/${item.projectId}`">{{ item.projectName }}</router-link>
                <span class="chip chip-ok">{{ item.matchScore }}%</span>
              </div>
              <ul class="mt-2 text-[#248a3d] space-y-1 text-[13px]">
                <li v-for="reason in item.reasons" :key="reason">{{ reason }}</li>
              </ul>
            </li>
            <li v-if="!recommendations.length" class="text-mute">还没有推送。完善画像后，管理员发布新项目时会通知你。</li>
          </ul>
        </section>
        <section class="card p-7">
          <h2 class="text-[21px] font-semibold tracking-tight">远程部署订单</h2>
          <ul class="mt-4 text-[15px] space-y-3">
            <li v-for="o in orders" :key="o.id" class="flex justify-between gap-3">
              <span class="text-mute">项目 #{{ o.projectId }} · ¥{{ o.amount }} · {{ orderLabel[o.status] || o.status }}</span>
              <router-link class="link-more !text-[13px]" :to="`/projects/${o.projectId}`">查看 ›</router-link>
            </li>
            <li v-if="!orders.length" class="text-mute">还没有部署订单。项目页可申请付费远程协助。</li>
          </ul>
        </section>
        <section class="card p-7">
          <h2 class="text-[21px] font-semibold tracking-tight">购买与发货</h2>
          <ul class="mt-4 text-[15px] space-y-3">
            <li v-for="c in claims" :key="c.id" class="flex justify-between gap-3">
              <span class="text-mute">
                作品 #{{ c.projectId }} · {{ deliveryLabel[c.deliveryType] || '自行下载源码' }} · ¥{{ c.amount || 0 }}
              </span>
              <router-link class="link-more !text-[13px]" :to="`/projects/${c.projectId}`">查看 ›</router-link>
            </li>
            <li v-if="!claims.length" class="text-mute">还没有购买记录</li>
          </ul>
        </section>
        <section class="card p-7">
          <h2 class="text-[21px] font-semibold tracking-tight">定制咨询</h2>
          <ul class="mt-4 text-[15px] space-y-3">
            <li v-for="o in customs" :key="o.id">
              <p class="font-medium tracking-tight">{{ o.title }}</p>
              <p class="text-mute mt-1">{{ customStatusLabel[o.status] || o.status }} · {{ o.budget || '预算待议' }}</p>
            </li>
            <li v-if="!customs.length" class="text-mute">还没有定制咨询。<router-link class="link-more !text-[15px]" to="/custom">去提交 ›</router-link></li>
          </ul>
        </section>
        <section class="card p-7">
          <h2 class="text-[21px] font-semibold tracking-tight">站内消息</h2>
          <ul class="mt-4 text-[15px] space-y-4">
            <li v-for="m in messages" :key="m.id">
              <button class="text-left w-full" @click="read(m)">
                <span :class="m.readFlag ? 'text-mute' : 'font-semibold tracking-tight'">{{ m.title }}</span>
                <p class="text-mute mt-1 whitespace-pre-wrap leading-7">{{ m.content }}</p>
              </button>
              <router-link v-if="m.projectId" class="link-more !text-[13px]" :to="`/projects/${m.projectId}`">查看项目 ›</router-link>
            </li>
            <li v-if="!messages.length" class="text-mute">暂无消息</li>
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
import { customStatusLabel, deliveryLabel } from '../labels'

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
const customs = ref([])
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
    http.get('/deploy-orders/mine'),
    http.get('/custom-orders/mine')
  ])
  if (settled[0].status === 'fulfilled') claims.value = settled[0].value.data || []
  if (settled[1].status === 'fulfilled') messages.value = settled[1].value.data || []
  if (settled[2].status === 'fulfilled') recommendations.value = settled[2].value.data || []
  if (settled[3].status === 'fulfilled') orders.value = settled[3].value.data || []
  if (settled[4].status === 'fulfilled') customs.value = settled[4].value.data || []
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
