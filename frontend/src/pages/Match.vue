<template>
  <div class="page-wrap">
    <h1 class="font-serif text-3xl">AI 项目匹配</h1>
    <p class="mt-2 text-ink/70 max-w-2xl">用自然语言描述你会什么、想做什么、还剩多少时间。系统会先提取画像，再对项目库打分并解释推荐。</p>

    <div class="mt-8 grid lg:grid-cols-12 gap-8">
      <form class="lg:col-span-7 card p-6 space-y-4" @submit.prevent="runMatch">
        <label class="block text-sm">
          需求描述
          <textarea v-model="form.query" rows="6" class="mt-1 w-full border border-black/10 rounded-xl p-3 bg-white" placeholder="例如：我是计算机本科生，Java 一般，会 Spring Boot CRUD 和 MyBatis，Vue 只会基础，希望功能丰富但不要太难，大概 40 天。" />
        </label>
        <div class="grid md:grid-cols-2 gap-3">
          <el-select v-model="form.language" clearable placeholder="语言">
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
          </el-select>
          <el-select v-model="form.skillLevel" clearable placeholder="水平">
            <el-option label="入门" value="beginner" />
            <el-option label="初级" value="junior" />
            <el-option label="中级" value="intermediate" />
          </el-select>
          <el-input v-model="form.backend" placeholder="后端：Spring Boot, MyBatis" />
          <el-input v-model="form.frontend" placeholder="前端：Vue" />
          <el-select v-model="form.difficulty" clearable placeholder="希望难度">
            <el-option label="入门" value="easy" />
            <el-option label="中等" value="medium" />
            <el-option label="较难" value="hard" />
          </el-select>
          <el-select v-model="form.projectType" clearable placeholder="项目类型">
            <el-option label="毕业设计" value="毕业设计" />
            <el-option label="课程设计" value="课程设计" />
            <el-option label="求职项目" value="求职项目" />
          </el-select>
          <el-input v-model="form.interests" placeholder="兴趣：医疗、校园、宠物" />
          <el-input-number v-model="form.durationDays" :min="7" :max="120" placeholder="天数" class="!w-full" />
        </div>
        <el-button type="primary" :loading="loading" native-type="submit" color="#b5693b">开始匹配</el-button>
      </form>

      <aside class="lg:col-span-5 space-y-4">
        <div v-if="result?.profile" class="card p-5 text-sm">
          <h2 class="font-serif text-lg mb-3">技术画像</h2>
          <p>语言 {{ result.profile.language || '—' }} · 水平 {{ skillLabel[result.profile.skillLevel] || result.profile.skillLevel || '—' }}</p>
          <p class="mt-1">后端 {{ (result.profile.backend || []).join(', ') || '—' }}</p>
          <p class="mt-1">前端 {{ (result.profile.frontend || []).join(', ') || '—' }}</p>
          <p class="mt-1">难度 {{ difficultyLabel[result.profile.difficulty] || result.profile.difficulty || '—' }} · 周期 {{ result.profile.durationDays || '—' }} 天</p>
          <p class="mt-1">兴趣 {{ result.profile.interests || '—' }}</p>
        </div>
        <div v-if="result?.summary" class="card p-5 text-sm leading-6">{{ result.summary }}</div>
      </aside>
    </div>

    <section v-if="result?.recommendations?.length" class="mt-10 space-y-4">
      <h2 class="font-serif text-2xl">推荐结果</h2>
      <article v-for="item in result.recommendations" :key="item.project.id" class="card p-6">
        <div class="flex flex-wrap justify-between gap-3">
          <div>
            <h3 class="font-serif text-xl">{{ item.project.name }}</h3>
            <p class="text-sm text-ink/60 mt-1">匹配度 {{ item.matchScore }}%</p>
          </div>
          <router-link :to="`/projects/${item.project.id}`" class="text-sm text-copper">查看详情</router-link>
        </div>
        <div class="mt-4 grid md:grid-cols-5 gap-2 text-xs">
          <span>技术栈 {{ item.techScore }}%</span>
          <span>难度 {{ item.difficultyScore }}%</span>
          <span>类型 {{ item.typeScore }}%</span>
          <span>兴趣 {{ item.interestScore }}%</span>
          <span>周期 {{ item.durationScore }}%</span>
        </div>
        <ul class="mt-4 text-sm space-y-1 text-moss">
          <li v-for="reason in item.reasons" :key="reason">✓ {{ reason }}</li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import http from '../api'
import { difficultyLabel, skillLabel } from '../labels'

const loading = ref(false)
const result = ref(null)
const form = reactive({
  query: '我是计算机专业本科生，Java 学得一般，会 Spring Boot CRUD 和 MyBatis，Vue 只会基础，希望做一个功能比较丰富但是不要太难的毕业设计，大概四十天。',
  language: '',
  skillLevel: '',
  backend: '',
  frontend: '',
  difficulty: '',
  projectType: '',
  interests: '',
  durationDays: 40
})

async function runMatch() {
  loading.value = true
  try {
    const res = await http.post('/ai/match', form)
    result.value = res.data
  } finally {
    loading.value = false
  }
}
</script>
