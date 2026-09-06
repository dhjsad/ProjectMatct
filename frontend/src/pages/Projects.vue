<template>
  <div class="page-wrap">
    <h1 class="font-serif text-3xl">项目库</h1>
    <p class="mt-2 text-ink/70">按分类、难度和类型筛选。未领取用户仍可查看介绍、架构与教程。</p>

    <div class="mt-6 flex flex-wrap gap-3">
      <el-input v-model="keyword" class="!w-56" placeholder="搜索名称 / 技术栈" @keyup.enter="load(1)" />
      <el-select v-model="category" clearable placeholder="分类" class="!w-36" @change="load(1)">
        <el-option v-for="c in filters.categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="difficulty" clearable placeholder="难度" class="!w-36" @change="load(1)">
        <el-option v-for="d in filters.difficulties" :key="d" :label="difficultyLabel[d] || d" :value="d" />
      </el-select>
      <el-select v-model="projectType" clearable placeholder="类型" class="!w-40" @change="load(1)">
        <el-option v-for="t in filters.projectTypes" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button @click="load(1)">搜索</el-button>
    </div>

    <div class="mt-8 grid md:grid-cols-2 gap-4">
      <router-link v-for="p in page.records" :key="p.id" :to="`/projects/${p.id}`" class="card p-5">
        <div class="flex justify-between gap-3">
          <h2 class="font-medium">{{ p.name }}</h2>
          <span class="text-xs" :class="p.remainingCount > 0 ? 'text-moss' : 'text-ink/40'">
            {{ p.remainingCount > 0 ? `可领取 · 余 ${p.remainingCount}` : '已领完' }}
          </span>
        </div>
        <p class="mt-2 text-sm text-ink/70 line-clamp-3">{{ p.description }}</p>
        <p class="mt-3 text-xs text-ink/50">
          {{ p.projectType }} · {{ difficultyLabel[p.difficulty] }} · {{ p.techStack }} · 约 {{ p.estimatedDuration }} 天
        </p>
      </router-link>
    </div>

    <div class="mt-8 flex justify-center">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="page.size"
        :current-page="page.current"
        :total="page.total"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api'
import { difficultyLabel } from '../labels'

const keyword = ref('')
const category = ref('')
const difficulty = ref('')
const projectType = ref('')
const filters = reactive({ categories: [], difficulties: [], projectTypes: [] })
const page = reactive({ records: [], total: 0, size: 8, current: 1 })

async function load(current = 1) {
  const res = await http.get('/projects', {
    params: { keyword: keyword.value, category: category.value, difficulty: difficulty.value, projectType: projectType.value, page: current, size: 8 }
  })
  Object.assign(page, res.data)
}

onMounted(async () => {
  const meta = await http.get('/meta/filters')
  Object.assign(filters, meta.data)
  await load(1)
})
</script>
