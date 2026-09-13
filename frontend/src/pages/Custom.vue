<template>
  <div>
    <section class="hero-stage !min-h-[70vh]">
      <p class="eyebrow fade-up">Enterprise</p>
      <h1 class="fade-up" style="animation-delay: 80ms">
        高端定制。<br />为商业项目而做。
      </h1>
      <p class="lede fade-up" style="animation-delay: 160ms">
        不是现成源码。顾问先听你的业务，再给方案与报价。适合公司、工作室和要上线的产品。
      </p>
    </section>

    <section class="page-wrap !pt-16">
      <div class="grid md:grid-cols-3 gap-5 mb-12">
        <article v-for="item in points" :key="item.title" class="card p-8">
          <h3 class="text-[24px] font-semibold tracking-tight">{{ item.title }}</h3>
          <p class="mt-3 text-[15px] leading-7 text-mute">{{ item.desc }}</p>
        </article>
      </div>

      <div class="card p-8 md:p-12 max-w-3xl mx-auto">
        <p class="eyebrow">开始沟通</p>
        <h2 class="section-title">留下需求，顾问会联系你。</h2>
        <form class="mt-8 space-y-4" @submit.prevent="submit">
          <el-input v-model="form.contactName" placeholder="联系人" />
          <el-input v-model="form.contact" placeholder="微信 / 手机号" />
          <el-input v-model="form.company" placeholder="公司或团队（选填）" />
          <el-input v-model="form.title" placeholder="项目主题，例如：连锁门店预约系统" />
          <el-input v-model="form.budget" placeholder="预算区间，例如：2–5 万" />
          <el-input v-model="form.requirement" type="textarea" rows="6" placeholder="业务目标、现有系统、上线时间、必须具备的能力" />
          <el-button native-type="submit" type="primary" color="#1d1d1f" :loading="loading">提交咨询</el-button>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'
import { useAuthStore } from '../store'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({
  contactName: '',
  contact: '',
  company: '',
  title: '',
  budget: '',
  requirement: ''
})
const points = [
  { title: '独立交付', desc: '按你的品牌和流程做，不复用已售作品。代码、文档和部署归你。' },
  { title: '顾问对接', desc: '先谈范围和排期，再开工。过程可看阶段成果，避免做到一半才发现不对。' },
  { title: '可上线', desc: '面向真实业务：权限、支付、运维和后续迭代都可以写进合同。' }
]

async function submit() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (!form.contact.trim() || !form.requirement.trim()) {
    ElMessage.error('请填写联系方式和需求说明')
    return
  }
  loading.value = true
  try {
    await http.post('/custom-orders', form)
    ElMessage.success('已提交，顾问会尽快联系你')
    form.title = ''
    form.requirement = ''
    form.budget = ''
  } finally {
    loading.value = false
  }
}
</script>
