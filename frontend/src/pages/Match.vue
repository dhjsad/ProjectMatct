<template>
  <div class="page-wrap">
    <p class="eyebrow">AI 匹配</p>
    <h1 class="page-title">用自然语言，找到合适的项目。</h1>
    <p class="page-kicker">描述你会什么、想做什么、还剩多少时间。系统会先提取画像，再对项目库打分并解释推荐。</p>

    <div class="mt-12 grid lg:grid-cols-12 gap-8">
      <form class="lg:col-span-7 card p-6 md:p-8 space-y-5" @submit.prevent="runMatch">
        <label class="block text-[13px] text-mute">
          需求描述
          <textarea
            v-model="form.query"
            rows="6"
            class="field mt-2"
            placeholder="例如：我是计算机本科生，Java 一般，会 Spring Boot CRUD 和 MyBatis，Vue 只会基础，希望功能丰富但不要太难，大概 40 天。"
          />
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
        <el-button type="primary" :loading="loading" native-type="submit" color="#0071e3">开始匹配</el-button>
      </form>

      <aside class="lg:col-span-5 space-y-4">
        <div v-if="result?.profile" class="card p-7 text-[15px] leading-7">
          <h2 class="text-[21px] font-semibold tracking-tight mb-4">技术画像</h2>
          <p class="text-mute">语言 {{ result.profile.language || '—' }} · 水平 {{ skillLabel[result.profile.skillLevel] || result.profile.skillLevel || '—' }}</p>
          <p class="mt-2 text-mute">后端 {{ (result.profile.backend || []).join(', ') || '—' }}</p>
          <p class="mt-2 text-mute">前端 {{ (result.profile.frontend || []).join(', ') || '—' }}</p>
          <p class="mt-2 text-mute">难度 {{ difficultyLabel[result.profile.difficulty] || result.profile.difficulty || '—' }} · 周期 {{ result.profile.durationDays || '—' }} 天</p>
          <p class="mt-2 text-mute">兴趣 {{ result.profile.interests || '—' }}</p>
        </div>
        <div v-else class="card p-7 text-[15px] leading-7 text-mute">
          匹配完成后，这里会显示从描述中提取出的能力画像。
        </div>
        <div v-if="result?.summary" class="card p-7 text-[15px] leading-7 text-mute">{{ result.summary }}</div>
      </aside>
    </div>

    <section v-if="result?.recommendations?.length" class="mt-16 space-y-5">
      <h2 class="section-title">推荐结果</h2>
      <article v-for="item in result.recommendations" :key="item.project.id" class="card p-6 md:p-8">
        <div class="flex flex-wrap justify-between gap-5 items-start">
          <div class="min-w-0">
            <h3 class="text-[28px] font-semibold tracking-tight">{{ item.project.name }}</h3>
            <p class="text-[15px] text-mute mt-2">综合匹配 {{ item.matchScore }}%</p>
          </div>
          <div class="flex items-center gap-5">
            <div class="score-ring" :style="{ '--score': `${item.matchScore * 3.6}deg` }">
              <span>{{ item.matchScore }}</span>
            </div>
            <router-link :to="`/projects/${item.project.id}`" class="link-more">查看详情 ›</router-link>
          </div>
        </div>
        <div class="mt-6 grid grid-cols-2 md:grid-cols-5 gap-3 text-[13px] text-mute">
          <span>技术栈 {{ item.techScore }}%</span>
          <span>难度 {{ item.difficultyScore }}%</span>
          <span>类型 {{ item.typeScore }}%</span>
          <span>兴趣 {{ item.interestScore }}%</span>
          <span>周期 {{ item.durationScore }}%</span>
        </div>
        <ul class="mt-5 text-[15px] space-y-1 text-[#248a3d]">
          <li v-for="reason in item.reasons" :key="reason">{{ reason }}</li>
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
