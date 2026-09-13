<template>
  <div class="page-wrap">
    <p class="eyebrow">购物袋</p>
    <h1 class="page-title">核对并发货。</h1>
    <p class="page-kicker">每件作品只售一次。结算时选择自行下载，或请客服发送并附带技术指导。</p>

    <p v-if="!cart.items.length" class="mt-20 text-center text-[17px] text-mute">
      购物袋是空的。
      <router-link to="/projects" class="link-more !text-[17px]">去商城看看 ›</router-link>
    </p>

    <div v-else class="mt-12 grid lg:grid-cols-12 gap-8 items-start">
      <div class="lg:col-span-8 space-y-4">
        <article v-for="item in cart.items" :key="item.id" class="card p-6">
          <div class="flex justify-between gap-4">
            <div>
              <router-link :to="`/projects/${item.id}`" class="text-[21px] font-semibold tracking-tight">{{ item.name }}</router-link>
              <p class="mt-2 text-[14px] text-mute line-clamp-2">{{ item.description }}</p>
            </div>
            <button class="text-[13px] text-mute" type="button" @click="cart.remove(item.id)">移除</button>
          </div>
          <el-radio-group :model-value="item.deliveryType" class="mt-5 flex flex-col gap-2" @change="(v) => cart.setDelivery(item.id, v)">
            <el-radio value="SELF_DOWNLOAD" label="SELF_DOWNLOAD">自己下载 · {{ money(item.salePrice) }}</el-radio>
            <el-radio value="SERVICE" label="SERVICE">客服发送 + 技术指导 · {{ money(item.salePrice + item.guidePrice) }}</el-radio>
          </el-radio-group>
          <el-input
            v-if="item.deliveryType === 'SERVICE'"
            :model-value="item.contact"
            class="mt-3"
            placeholder="微信 / 手机号"
            @input="(v) => cart.setContact(item.id, v)"
          />
        </article>
      </div>
      <aside class="lg:col-span-4">
        <div class="card buy-card p-7">
          <p class="text-mute text-[13px]">合计</p>
          <p class="mt-2 text-[40px] font-semibold tracking-tight">{{ money(cart.total) }}</p>
          <el-button class="mt-6 w-full" type="primary" color="#0071e3" :loading="paying" @click="checkout">结算并发货</el-button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'
import { useAuthStore } from '../store'
import { useCartStore } from '../cart'
import { money } from '../labels'

const auth = useAuthStore()
const cart = useCartStore()
const router = useRouter()
const route = useRoute()
const paying = ref(false)

async function checkout() {
  if (!auth.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  const service = cart.items.find((item) => item.deliveryType === 'SERVICE' && !String(item.contact || '').trim())
  if (service) {
    ElMessage.error(`「${service.name}」请客服发货，请填写联系方式`)
    return
  }
  paying.value = true
  try {
    await http.post('/projects/checkout', {
      items: cart.items.map((item) => ({
        projectId: item.id,
        deliveryType: item.deliveryType,
        contact: item.contact
      }))
    })
    cart.clear()
    ElMessage.success('已支付并发货，可在「我的工坊」查看')
    router.push('/me')
  } finally {
    paying.value = false
  }
}
</script>
