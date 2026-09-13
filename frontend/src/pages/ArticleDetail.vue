<template>
  <div v-if="article" class="page-wrap max-w-3xl">
    <router-link to="/articles" class="link-more !text-[13px]">返回文章 ›</router-link>
    <p class="mt-8 text-[13px] text-mute">{{ article.keywords }}</p>
    <h1 class="page-title mt-3">{{ article.title }}</h1>
    <article class="mt-10 text-[19px] leading-9 whitespace-pre-wrap text-ink">{{ article.content }}</article>
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
