<template>
  <div class="page-wrap">
    <section class="grid lg:grid-cols-12 gap-10 items-start">
      <div class="lg:col-span-7">
        <p class="text-copper tracking-widest text-xs mb-3">AI 驱动的计算机项目推荐</p>
        <h1 class="font-serif text-4xl md:text-5xl leading-tight">
          先看你会什么、<br />再决定做什么项目。
        </h1>
        <p class="mt-5 text-ink/75 leading-7 max-w-xl">
          把技术栈、难度、周期和兴趣说清楚。Agent 会分析能力画像、给项目打分，并解释为什么推荐——而不是丢给你一份无法判断的源码列表。
        </p>
        <div class="mt-8 flex flex-wrap gap-3">
          <router-link to="/match" class="px-5 py-2.5 rounded-full bg-copper text-white">开始 AI 匹配</router-link>
          <router-link to="/projects" class="px-5 py-2.5 rounded-full border border-ink/20">浏览项目库</router-link>
        </div>
        <p class="mt-6 text-xs text-ink/55 max-w-xl">
          平台定位为学习与二次开发辅助。鼓励理解代码、自行实现，不鼓励把他人作品直接作为毕业设计提交。
        </p>
      </div>
      <aside class="lg:col-span-5 card p-6">
        <p class="text-sm text-ink/60 mb-3">典型提问</p>
        <ul class="space-y-3 text-sm">
          <li v-for="q in samples" :key="q" class="border-b border-black/5 pb-3 last:border-0">
            “{{ q }}”
          </li>
        </ul>
      </aside>
    </section>

    <section class="mt-16 grid md:grid-cols-3 gap-4">
      <div v-for="item in values" :key="item.title" class="card p-5">
        <h3 class="font-serif text-lg">{{ item.title }}</h3>
        <p class="mt-2 text-sm text-ink/70 leading-6">{{ item.desc }}</p>
      </div>
    </section>

    <section v-if="inbox.recommendations.length" class="mt-16">
      <div class="flex items-end justify-between mb-4">
        <h2 class="font-serif text-2xl">发现可能适合你的项目</h2>
        <router-link to="/me" class="text-sm text-copper">全部推送</router-link>
      </div>
      <div class="grid md:grid-cols-2 gap-4">
        <router-link
          v-for="item in inbox.recommendations.slice(0, 4)"
          :key="item.id"
          :to="`/projects/${item.projectId}`"
          class="card p-5"
        >
          <div class="flex justify-between gap-3">
            <h3 class="font-medium">{{ item.projectName }}</h3>
            <span class="text-xs text-copper shrink-0">匹配度 {{ item.matchScore }}%</span>
          </div>
          <p class="mt-2 text-sm text-ink/70 line-clamp-2">{{ item.description }}</p>
          <ul class="mt-3 text-xs text-moss space-y-1">
            <li v-for="reason in (item.reasons || []).slice(0, 2)" :key="reason">✓ {{ reason }}</li>
          </ul>
        </router-link>
      </div>
    </section>

    <section class="mt-16">
      <div class="flex items-end justify-between mb-4">
        <h2 class="font-serif text-2xl">项目库一览</h2>
        <router-link to="/projects" class="text-sm text-copper">全部项目</router-link>
      </div>
      <div class="grid md:grid-cols-2 gap-4">
        <router-link
          v-for="p in featured"
          :key="p.id"
          :to="`/projects/${p.id}`"
          class="card p-5 hover:border-copper/40"
        >
          <div class="flex justify-between gap-3">
            <h3 class="font-medium">{{ p.name }}</h3>
            <span class="text-xs shrink-0" :class="p.remainingCount > 0 ? 'text-moss' : 'text-ink/40'">
              {{ p.remainingCount > 0 ? `可领取 · 余 ${p.remainingCount}` : '已领完' }}
            </span>
          </div>
          <p class="mt-2 text-sm text-ink/70 line-clamp-2">{{ p.description }}</p>
          <p class="mt-3 text-xs text-ink/50">{{ p.techStack }} · 约 {{ p.estimatedDuration }} 天</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api'
import { useInboxStore } from '../inbox'

const featured = ref([])
const inbox = useInboxStore()
const samples = [
  '我会 Java，但不知道做什么毕业设计。',
  '只会 Spring Boot CRUD，可以完成什么项目？',
  'Vue 一般，还有两个月，现在开始做什么合适？'
]
const values = [
  { title: '理解能力', desc: '从自然语言里抽出语言、框架、难度和周期，形成技术画像。' },
  { title: '匹配打分', desc: '按技术栈、难度、类型、兴趣、周期加权，给出 Top 3～5 与分项分数。' },
  { title: '解释原因', desc: '说明为什么适合、哪里不适合，并公开架构与教程，领取名额单独限量。' }
]

onMounted(async () => {
  const res = await http.get('/projects', { params: { size: 4 } })
  featured.value = res.data?.records || []
  inbox.refresh().catch(() => {})
})
</script>
