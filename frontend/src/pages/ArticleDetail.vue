<template>
  <div v-if="article" class="page-wrap max-w-3xl">
    <router-link to="/articles" class="text-sm text-ink/50">返回文章</router-link>
    <h1 class="font-serif text-3xl mt-3">{{ article.title }}</h1>
    <p class="mt-2 text-sm text-ink/50">{{ article.keywords }}</p>
    <article class="card p-6 mt-6 whitespace-pre-wrap leading-8">{{ article.content }}</article>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../api'

const article = ref(null)
const route = useRoute()
onMounted(async () => {
  const res = await http.get(`/articles/${route.params.id}`)
  article.value = res.data
})
</script>
