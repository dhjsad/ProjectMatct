<template>
  <div class="page-wrap">
    <h1 class="font-serif text-3xl">选题与学习文章</h1>
    <p class="mt-2 text-ink/70">工具之外保留可被检索的内容：选题方法、避坑和项目学习路径。</p>
    <div class="mt-8 space-y-4">
      <router-link v-for="a in articles" :key="a.id" :to="`/articles/${a.id}`" class="card p-5 block">
        <h2 class="font-medium">{{ a.title }}</h2>
        <p class="mt-2 text-sm text-ink/70">{{ a.summary }}</p>
        <p class="mt-2 text-xs text-ink/45">{{ a.keywords }}</p>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api'

const articles = ref([])
onMounted(async () => {
  const res = await http.get('/articles')
  articles.value = res.data || []
})
</script>
