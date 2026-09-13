<template>
  <div>
    <section class="hero-stage">
      <p class="eyebrow fade-up">Store</p>
      <h1 class="fade-up" style="animation-delay: 80ms">
        只卖一次。<br />你的，就是唯一。
      </h1>
      <p class="lede fade-up" style="animation-delay: 160ms">
        每件作品全球仅售一份。买下即可自行下载，或由客服发送并附带付费技术指导。
      </p>
      <div class="mt-10 flex flex-wrap items-center justify-center gap-5 fade-up" style="animation-delay: 240ms">
        <router-link to="/projects" class="btn-primary">浏览商城</router-link>
        <router-link to="/custom" class="link-more">高端定制 ›</router-link>
      </div>
    </section>

    <section class="page-wrap !pt-20 !pb-8">
      <p class="eyebrow">为什么选我们</p>
      <h2 class="section-title">把作品当作商品，而不是复制品。</h2>
      <div class="mt-10 grid md:grid-cols-3 gap-5">
        <article v-for="item in values" :key="item.title" class="card p-8">
          <p class="text-[12px] tracking-[0.16em] text-mute uppercase">{{ item.step }}</p>
          <h3 class="mt-4 text-[28px] font-semibold tracking-tight leading-tight">{{ item.title }}</h3>
          <p class="mt-3 text-[15px] leading-7 text-mute">{{ item.desc }}</p>
        </article>
      </div>
    </section>

    <section v-if="featured.length" class="page-wrap !pt-10 !pb-20">
      <div class="flex items-end justify-between gap-4 mb-8">
        <div>
          <p class="eyebrow">商店</p>
          <h2 class="section-title">精选作品</h2>
        </div>
        <router-link to="/projects" class="link-more shrink-0">全部商品 ›</router-link>
      </div>
      <div class="grid md:grid-cols-2 gap-5">
        <router-link
          v-for="p in featured"
          :key="p.id"
          :to="`/projects/${p.id}`"
          class="card card-hover overflow-hidden"
        >
          <div class="tile-visual" :class="tileClass(p.id)"></div>
          <div class="p-6">
            <div class="flex justify-between gap-3 items-start">
              <h3 class="text-[21px] font-semibold tracking-tight">{{ p.name }}</h3>
              <span class="chip shrink-0" :class="sold(p) ? 'chip-off' : 'chip-ok'">
                {{ sold(p) ? '已售出' : '可购买' }}
              </span>
            </div>
            <p class="mt-3 text-[15px] text-mute line-clamp-2 leading-6">{{ p.description }}</p>
            <p class="mt-4 text-[17px] font-semibold tracking-tight">{{ money(p.salePrice) }}</p>
          </div>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api'
import { money, sold } from '../labels'

const featured = ref([])
const values = [
  { step: '01', title: '独一无二', desc: '每件作品只售一次。有人买下，立刻显示已售出，不再重复售卖。' },
  { step: '02', title: '即时发货', desc: '购买后可自行下载源码，或选择客服发送，并叠加付费技术指导。' },
  { step: '03', title: '商业定制', desc: '需要完整商业项目时，走高端定制：顾问沟通、报价、独立交付。' }
]

function tileClass(id) {
  return ['tile-a', 'tile-b', 'tile-c', 'tile-d'][(Number(id) || 0) % 4]
}

onMounted(async () => {
  try {
    const res = await http.get('/projects', { params: { size: 6 }, skipErrorMessage: true })
    featured.value = res.data?.records || []
  } catch {
    featured.value = []
  }
})
</script>
