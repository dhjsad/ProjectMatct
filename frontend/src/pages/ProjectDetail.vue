<template>
  <div v-if="detail.project" class="page-wrap">
    <p class="text-[13px] text-mute">
      <router-link to="/projects" class="link-more !text-[13px]">商城</router-link>
      <span class="mx-2">/</span>
      {{ detail.project.category }}
    </p>
    <div class="mt-6 grid lg:grid-cols-12 gap-10 items-start">
      <div class="lg:col-span-7">
        <h1 class="page-title">{{ detail.project.name }}</h1>
        <p class="mt-5 max-w-2xl text-[19px] leading-8 text-mute">{{ detail.project.description }}</p>
        <div class="mt-8 overflow-hidden rounded-[28px]">
          <div class="tile-visual !h-64 !rounded-none" :class="tileClass(detail.project.id)"></div>
        </div>
      </div>
      <aside class="lg:col-span-5">
        <div class="card buy-card p-7 text-[15px]">
          <span class="chip" :class="detail.sold ? 'chip-off' : 'chip-ok'">
            {{ detail.sold ? '已售出' : '可购买 · 仅此一件' }}
          </span>
          <p class="mt-4 text-[40px] font-semibold tracking-tight">{{ money(detail.project.salePrice) }}</p>
          <p class="mt-2 text-mute">技术指导另计 {{ money(detail.project.guidePrice) }}</p>

          <template v-if="detail.claimedByMe">
            <p class="mt-4 text-[#0071e3]">
              {{ detail.myClaim?.deliveryType === 'SERVICE' ? '客服将发送源码并安排指导' : '你已买下，可立即下载源码' }}
            </p>
            <el-button class="mt-5 w-full" type="primary" color="#1d1d1f" @click="dlSource">下载源码</el-button>
          </template>
          <template v-else>
            <el-button
              class="mt-5 w-full"
              type="primary"
              color="#0071e3"
              :disabled="detail.sold"
              @click="openBuy"
            >
              {{ detail.sold ? '已售出' : '立即购买' }}
            </el-button>
            <el-button class="mt-3 w-full" :disabled="detail.sold" @click="addCart">加入购物袋</el-button>
          </template>

          <div class="mt-4 space-y-2">
            <el-button class="w-full" @click="dlTutorial">下载介绍文档</el-button>
            <el-button
              v-if="detail.deployServiceEnabled && detail.claimedByMe"
              class="w-full"
              type="primary"
              color="#0071e3"
              @click="openPay"
            >
              追加远程部署 ¥{{ detail.project.deployPrice || 199 }}
            </el-button>
          </div>
          <p class="mt-4 text-[12px] leading-6 text-mute">
            购买后即时发货：自行下载，或请客服发送并附带付费技术指导。售出后本页将显示已售出。
          </p>
        </div>
      </aside>
    </div>

    <dl class="mt-12 grid md:grid-cols-2 gap-4 text-[15px]">
      <div class="card p-6"><dt class="text-mute text-[13px]">适合用户</dt><dd class="mt-2 tracking-tight">{{ detail.project.suitableFor }}</dd></div>
      <div class="card p-6"><dt class="text-mute text-[13px]">技术栈</dt><dd class="mt-2 tracking-tight">{{ detail.project.techStack }}</dd></div>
      <div class="card p-6"><dt class="text-mute text-[13px]">难度 / 周期</dt><dd class="mt-2 tracking-tight">{{ difficultyLabel[detail.project.difficulty] }} · 约 {{ detail.project.estimatedDuration }} 天</dd></div>
      <div class="card p-6"><dt class="text-mute text-[13px]">类型</dt><dd class="mt-2 tracking-tight">{{ detail.project.projectType }}</dd></div>
    </dl>

    <section class="mt-6 grid lg:grid-cols-2 gap-4">
      <article class="card p-7"><h2 class="text-[21px] font-semibold tracking-tight">功能模块</h2><p class="mt-3 whitespace-pre-wrap text-[15px] leading-7 text-mute">{{ detail.project.modules }}</p></article>
      <article class="card p-7"><h2 class="text-[21px] font-semibold tracking-tight">系统架构</h2><p class="mt-3 whitespace-pre-wrap text-[15px] leading-7 text-mute">{{ detail.project.architecture }}</p></article>
      <article class="card p-7"><h2 class="text-[21px] font-semibold tracking-tight">数据库设计</h2><p class="mt-3 whitespace-pre-wrap text-[15px] leading-7 text-mute">{{ detail.project.dbDesign }}</p></article>
      <article class="card p-7"><h2 class="text-[21px] font-semibold tracking-tight">部署说明</h2><p class="mt-3 whitespace-pre-wrap text-[15px] leading-7 text-mute">{{ detail.project.deployGuide }}</p></article>
    </section>

    <article class="card p-7 mt-4">
      <h2 class="text-[21px] font-semibold tracking-tight">示例代码思路</h2>
      <pre class="mt-3 text-[14px] whitespace-pre-wrap text-mute">{{ detail.project.sampleCode }}</pre>
    </article>
    <article class="card p-7 mt-4">
      <h2 class="text-[21px] font-semibold tracking-tight">开发教程</h2>
      <p class="mt-3 whitespace-pre-wrap text-[15px] leading-7 text-mute">{{ detail.project.tutorial }}</p>
    </article>

    <section class="mt-12">
      <h2 class="section-title">公开资料</h2>
      <div v-for="r in detail.publicResources" :key="r.id" class="card p-6 mt-4">
        <div class="flex justify-between gap-3">
          <h3 class="text-[17px] font-semibold tracking-tight">{{ r.title }}</h3>
          <button v-if="r.filePath || r.content" class="link-more !text-[13px]" @click="dlResource(r)">下载 ›</button>
        </div>
        <p class="mt-2 text-[15px] text-mute whitespace-pre-wrap leading-7">{{ r.content }}</p>
      </div>
    </section>

    <section v-if="detail.claimedResources?.length" class="mt-12">
      <h2 class="section-title">已购资料</h2>
      <div v-for="r in detail.claimedResources" :key="r.id" class="card p-6 mt-4">
        <div class="flex justify-between gap-3">
          <h3 class="text-[17px] font-semibold tracking-tight">{{ r.title }}{{ r.fileName ? ` · ${r.fileName}` : '' }}</h3>
          <button class="link-more !text-[13px]" @click="dlResource(r)">下载 ›</button>
        </div>
        <p class="mt-2 text-[15px] text-mute whitespace-pre-wrap leading-7">{{ r.content }}</p>
      </div>
    </section>

    <el-dialog v-model="buyVisible" title="确认购买并发货" width="480px">
      <p class="text-[15px] text-mute leading-7">
        「{{ detail.project.name }}」售价
        <span class="text-ink font-medium">{{ money(detail.project.salePrice) }}</span>
        。买下后立即下架，不再二次售卖。
      </p>
      <el-radio-group v-model="buyForm.deliveryType" class="mt-5 flex flex-col gap-3">
        <el-radio value="SELF_DOWNLOAD" label="SELF_DOWNLOAD">自己下载源码 · {{ money(detail.project.salePrice) }}</el-radio>
        <el-radio value="SERVICE" label="SERVICE">
          客服发送 + 技术指导 · {{ money((detail.project.salePrice || 0) + (detail.project.guidePrice || 0)) }}
        </el-radio>
      </el-radio-group>
      <el-input
        v-if="buyForm.deliveryType === 'SERVICE'"
        v-model="buyForm.contact"
        class="mt-4"
        placeholder="微信 / 手机号，方便客服发货"
      />
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" color="#0071e3" :loading="buying" @click="buy">确认支付并发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payVisible" title="付费远程部署" width="480px">
      <p class="text-[15px] text-mute leading-7">
        工作人员按你的环境远程协助把项目跑起来，费用
        <span class="text-ink font-medium">¥{{ detail.project.deployPrice || 199 }}</span>
        。
      </p>
      <el-input v-model="payForm.contact" class="mt-4" placeholder="微信 / 手机号" />
      <el-input v-model="payForm.environmentNote" class="mt-3" type="textarea" rows="4" placeholder="系统、JDK/Node 版本、要部署到本机还是云服务器" />
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" color="#0071e3" :loading="paying" @click="pay">确认支付</el-button>
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
import { useCartStore } from '../cart'
import { difficultyLabel, money } from '../labels'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const detail = reactive({
  project: null, publicResources: [], claimedResources: [],
  claimedByMe: false, sold: false, hasSourceFile: false, deployServiceEnabled: true, myClaim: null
})
const buyVisible = ref(false)
const buying = ref(false)
const buyForm = reactive({ deliveryType: 'SELF_DOWNLOAD', contact: '' })
const payVisible = ref(false)
const paying = ref(false)
const payForm = reactive({ contact: '', environmentNote: '' })

function tileClass(id) {
  return ['tile-a', 'tile-b', 'tile-c', 'tile-d'][(Number(id) || 0) % 4]
}

async function load() {
  const res = await http.get(`/projects/${route.params.id}`)
  Object.assign(detail, res.data)
}

function needLogin() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return true
  }
  return false
}

function addCart() {
  if (detail.sold) return
  const ok = cart.add(detail.project)
  ElMessage.success(ok ? '已加入购物袋' : '购物袋里已有这件作品')
}

function openBuy() {
  if (needLogin()) return
  buyVisible.value = true
}

async function buy() {
  if (buyForm.deliveryType === 'SERVICE' && !buyForm.contact.trim()) {
    ElMessage.error('请填写联系方式')
    return
  }
  buying.value = true
  try {
    await http.post(`/projects/${route.params.id}/purchase`, buyForm)
    cart.remove(Number(route.params.id))
    buyVisible.value = false
    ElMessage.success(buyForm.deliveryType === 'SERVICE' ? '已支付，客服将发货' : '已支付，可立即下载')
    await load()
  } finally {
    buying.value = false
  }
}

async function dlTutorial() {
  await downloadFile(`/projects/${route.params.id}/download/tutorial`, '部署教程.md')
}

async function dlSource() {
  if (needLogin()) return
  await downloadFile(`/projects/${route.params.id}/download/source`, '源码学习包.zip')
}

async function dlResource(r) {
  await downloadFile(`/projects/${route.params.id}/resources/${r.id}/download`, r.fileName || `${r.title}.txt`)
}

function openPay() {
  if (needLogin()) return
  payVisible.value = true
}

async function pay() {
  paying.value = true
  try {
    const created = await http.post(`/projects/${route.params.id}/deploy-orders`, payForm)
    await http.post(`/deploy-orders/${created.data.id}/pay`)
    payVisible.value = false
    ElMessage.success('支付成功，请在「我的工坊」查看订单')
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>
