<template>
  <div v-if="detail.project" class="page-wrap">
    <p class="text-sm text-ink/50">
      <router-link to="/projects">项目库</router-link> / {{ detail.project.category }}
    </p>
    <div class="mt-3 flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="font-serif text-3xl">{{ detail.project.name }}</h1>
        <p class="mt-3 max-w-3xl text-ink/75 leading-7">{{ detail.project.description }}</p>
      </div>
      <div class="card p-4 min-w-[260px] text-sm">
        <p :class="detail.claimable ? 'text-moss' : 'text-ink/50'">
          {{ detail.claimable ? '可领取' : '完整资源已领完' }}
        </p>
        <p class="mt-1">剩余名额 {{ detail.project.remainingCount }}</p>
        <p v-if="detail.claimedByMe" class="mt-2 text-copper">你已领取，可下载源码与教程</p>
        <el-button
          v-else
          class="mt-3"
          type="primary"
          color="#3f6b52"
          :disabled="!detail.claimable"
          @click="claim"
        >
          领取学习资源
        </el-button>
        <div class="mt-4 space-y-2">
          <el-button class="w-full" @click="dlTutorial">下载部署教程</el-button>
          <el-button class="w-full" type="primary" color="#b5693b" :disabled="!detail.claimedByMe" @click="dlSource">
            下载源码学习包
          </el-button>
          <el-button
            v-if="detail.deployServiceEnabled"
            class="w-full"
            type="primary"
            color="#1c1915"
            @click="openPay"
          >
            付费远程部署 ¥{{ detail.project.deployPrice || 199 }}
          </el-button>
        </div>
        <p class="mt-2 text-xs text-ink/50">教程公开可下。源码需先领取。远程部署为人工协助跑通环境，不代替你完成作业。</p>
      </div>
    </div>

    <dl class="mt-8 grid md:grid-cols-2 gap-4 text-sm">
      <div class="card p-4"><dt class="text-ink/50">适合用户</dt><dd class="mt-1">{{ detail.project.suitableFor }}</dd></div>
      <div class="card p-4"><dt class="text-ink/50">技术栈</dt><dd class="mt-1">{{ detail.project.techStack }}</dd></div>
      <div class="card p-4"><dt class="text-ink/50">难度 / 周期</dt><dd class="mt-1">{{ difficultyLabel[detail.project.difficulty] }} · 约 {{ detail.project.estimatedDuration }} 天</dd></div>
      <div class="card p-4"><dt class="text-ink/50">类型</dt><dd class="mt-1">{{ detail.project.projectType }}</dd></div>
    </dl>

    <section class="mt-8 grid lg:grid-cols-2 gap-4">
      <article class="card p-5"><h2 class="font-serif text-lg">功能模块</h2><p class="mt-2 whitespace-pre-wrap text-sm leading-7">{{ detail.project.modules }}</p></article>
      <article class="card p-5"><h2 class="font-serif text-lg">系统架构</h2><p class="mt-2 whitespace-pre-wrap text-sm leading-7">{{ detail.project.architecture }}</p></article>
      <article class="card p-5"><h2 class="font-serif text-lg">数据库设计</h2><p class="mt-2 whitespace-pre-wrap text-sm leading-7">{{ detail.project.dbDesign }}</p></article>
      <article class="card p-5"><h2 class="font-serif text-lg">部署说明</h2><p class="mt-2 whitespace-pre-wrap text-sm leading-7">{{ detail.project.deployGuide }}</p></article>
    </section>

    <article class="card p-5 mt-4">
      <h2 class="font-serif text-lg">示例代码思路</h2>
      <pre class="mt-2 text-sm whitespace-pre-wrap">{{ detail.project.sampleCode }}</pre>
    </article>
    <article class="card p-5 mt-4">
      <h2 class="font-serif text-lg">开发教程</h2>
      <p class="mt-2 whitespace-pre-wrap text-sm leading-7">{{ detail.project.tutorial }}</p>
    </article>

    <section class="mt-8">
      <h2 class="font-serif text-xl">公开学习资料</h2>
      <div v-for="r in detail.publicResources" :key="r.id" class="card p-4 mt-3">
        <div class="flex justify-between gap-3">
          <h3 class="text-sm font-medium">{{ r.title }}</h3>
          <button v-if="r.filePath || r.content" class="text-xs text-copper" @click="dlResource(r)">下载</button>
        </div>
        <p class="mt-1 text-sm text-ink/70 whitespace-pre-wrap">{{ r.content }}</p>
      </div>
    </section>

    <section v-if="detail.claimedResources?.length" class="mt-8">
      <h2 class="font-serif text-xl">已领取资料</h2>
      <div v-for="r in detail.claimedResources" :key="r.id" class="card p-4 mt-3 border-moss/30">
        <div class="flex justify-between gap-3">
          <h3 class="text-sm font-medium">{{ r.title }}{{ r.fileName ? ` · ${r.fileName}` : '' }}</h3>
          <button class="text-xs text-copper" @click="dlResource(r)">下载</button>
        </div>
        <p class="mt-1 text-sm text-ink/70 whitespace-pre-wrap">{{ r.content }}</p>
      </div>
    </section>

    <el-dialog v-model="payVisible" title="付费远程部署" width="480px">
      <p class="text-sm text-ink/70 leading-6">
        工作人员按你的环境远程协助把项目跑起来，费用
        <span class="text-copper font-medium">¥{{ detail.project.deployPrice || 199 }}</span>
        。本地演示支付确认后即进入待处理。这是部署协助，不是代写毕业设计。
      </p>
      <el-input v-model="payForm.contact" class="mt-4" placeholder="微信 / 手机号" />
      <el-input v-model="payForm.environmentNote" class="mt-3" type="textarea" rows="4" placeholder="系统、JDK/Node 版本、要部署到本机还是云服务器、卡在哪一步" />
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" color="#1c1915" :loading="paying" @click="pay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'
import { downloadFile } from '../download'
import { useAuthStore } from '../store'
import { difficultyLabel } from '../labels'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const detail = reactive({
  project: null, publicResources: [], claimedResources: [],
  claimedByMe: false, claimable: false, hasSourceFile: false, deployServiceEnabled: true
})
const payVisible = ref(false)
const paying = ref(false)
const payForm = reactive({ contact: '', environmentNote: '' })

async function load() {
  const res = await http.get(`/projects/${route.params.id}`)
  Object.assign(detail, res.data)
}

async function claim() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  await http.post(`/projects/${route.params.id}/claim`)
  ElMessage.success('领取成功，现在可以下载源码学习包')
  await load()
}

async function dlTutorial() {
  await downloadFile(`/projects/${route.params.id}/download/tutorial`, '部署教程.md')
}

async function dlSource() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  await downloadFile(`/projects/${route.params.id}/download/source`, '源码学习包.zip')
}

async function dlResource(r) {
  await downloadFile(`/projects/${route.params.id}/resources/${r.id}/download`, r.fileName || `${r.title}.txt`)
}

function openPay() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  payVisible.value = true
}

async function pay() {
  paying.value = true
  try {
    const created = await http.post(`/projects/${route.params.id}/deploy-orders`, payForm)
    await http.post(`/deploy-orders/${created.data.id}/pay`)
    payVisible.value = false
    ElMessage.success('支付成功，请在「我的工坊」查看订单，并留意站内消息')
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>
