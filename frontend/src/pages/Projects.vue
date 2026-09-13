<template>
  <div class="page-wrap">
    <p class="eyebrow">商城</p>
    <h1 class="page-title">找到那一件。</h1>
    <p class="page-kicker">按名称、技术栈或分类搜索。售出即下架，保证你买到的是唯一一份。</p>

    <div class="mt-10 flex flex-wrap gap-3">
      <el-input v-model="keyword" class="!w-64" placeholder="搜索作品 / 技术栈" @keyup.enter="load(1)" />
      <el-select v-model="category" clearable placeholder="分类" class="!w-36" @change="load(1)">
        <el-option v-for="c in filters.categories" :key="c" :label="c" :value="c" />
      </el-select>
      <el-select v-model="difficulty" clearable placeholder="难度" class="!w-36" @change="load(1)">
        <el-option v-for="d in filters.difficulties" :key="d" :label="difficultyLabel[d] || d" :value="d" />
      </el-select>
      <el-select v-model="projectType" clearable placeholder="类型" class="!w-40" @change="load(1)">
        <el-option v-for="t in filters.projectTypes" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" color="#1d1d1f" @click="load(1)">搜索</el-button>
    </div>

    <p v-if="!page.records.length" class="mt-20 text-center text-[17px] text-mute">没有符合条件的作品。</p>
    <div v-else class="mt-10 grid md:grid-cols-2 xl:grid-cols-3 gap-5">
      <article v-for="p in page.records" :key="p.id" class="card card-hover overflow-hidden flex flex-col">
        <router-link :to="`/projects/${p.id}`">
          <div class="tile-visual" :class="tileClass(p.id)"></div>
        </router-link>
        <div class="p-6 flex flex-col flex-1">
          <div class="flex justify-between gap-3 items-start">
            <router-link :to="`/projects/${p.id}`" class="text-[21px] font-semibold tracking-tight">{{ p.name }}</router-link>
            <span class="chip shrink-0" :class="sold(p) ? 'chip-off' : 'chip-ok'">
              {{ sold(p) ? '已售出' : '可购买' }}
            </span>
          </div>
          <p class="mt-3 text-[15px] text-mute line-clamp-3 leading-6 flex-1">{{ p.description }}</p>
          <p class="mt-4 text-[13px] text-mute">{{ p.techStack }}</p>
          <div class="mt-5 flex items-center justify-between gap-3">
            <p class="text-[21px] font-semibold tracking-tight">{{ money(p.salePrice) }}</p>
            <div class="flex gap-2">
              <el-button :disabled="sold(p)" @click="addCart(p)">加入购物袋</el-button>
              <el-button type="primary" color="#0071e3" :disabled="sold(p)" @click="$router.push(`/projects/${p.id}`)">查看</el-button>
            </div>
          </div>
        </div>
      </article>
    </div>

    <div v-if="page.total > 0" class="mt-12 flex justify-center">
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
import { ElMessage } from 'element-plus'
import http from '../api'
import { useCartStore } from '../cart'
import { difficultyLabel, money, sold } from '../labels'

const cart = useCartStore()
const keyword = ref('')
const category = ref('')
const difficulty = ref('')
const projectType = ref('')
const filters = reactive({ categories: [], difficulties: [], projectTypes: [] })
const page = reactive({ records: [], total: 0, size: 9, current: 1 })

function tileClass(id) {
  return ['tile-a', 'tile-b', 'tile-c', 'tile-d'][(Number(id) || 0) % 4]
}

function addCart(p) {
  if (sold(p)) return
  const ok = cart.add(p)
  ElMessage.success(ok ? '已加入购物袋' : '购物袋里已有这件作品')
}

async function load(current = 1) {
  const res = await http.get('/projects', {
    params: {
      keyword: keyword.value,
      category: category.value,
      difficulty: difficulty.value,
      projectType: projectType.value,
      page: current,
      size: 9
    },
    skipErrorMessage: true
  })
  Object.assign(page, res.data)
}

onMounted(async () => {
  try {
    const meta = await http.get('/meta/filters', { skipErrorMessage: true })
    Object.assign(filters, meta.data)
    await load(1)
  } catch {
    page.records = []
  }
})
</script>
