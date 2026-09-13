<template>
  <div class="page-wrap">
    <p class="eyebrow">选题文章</p>
    <h1 class="page-title">先想清楚，再动手做。</h1>
    <p class="page-kicker">工具之外保留可被检索的内容：选题方法、避坑和项目学习路径。</p>

    <p v-if="!articles.length" class="mt-20 text-center text-[17px] text-mute">还没有发布文章。</p>
    <div v-else class="mt-12 card divide-y divide-black/5">
      <router-link
        v-for="a in articles"
        :key="a.id"
        :to="`/articles/${a.id}`"
        class="block p-7 md:p-8 hover:bg-white"
      >
        <p class="text-[13px] text-mute">{{ a.keywords }}</p>
        <h2 class="mt-2 text-[28px] font-semibold tracking-tight leading-tight">{{ a.title }}</h2>
        <p class="mt-3 text-[15px] text-mute leading-7 max-w-3xl">{{ a.summary }}</p>
        <p class="mt-4 link-more">继续阅读 ›</p>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api'

const articles = ref([])
onMounted(async () => {
  try {
    const res = await http.get('/articles', { skipErrorMessage: true })
    articles.value = res.data || []
  } catch {
    articles.value = []
  }
})
</script>
