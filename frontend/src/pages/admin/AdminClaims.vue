<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="font-serif text-2xl">领取记录</h1>
    <el-table :data="claims" class="mt-6" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="projectId" label="项目" width="90" />
      <el-table-column prop="claimTime" label="时间" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const claims = ref([])
onMounted(async () => {
  claims.value = (await http.get('/admin/claims')).data || []
})
</script>
