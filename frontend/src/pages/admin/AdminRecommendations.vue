<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">推荐记录</h1>
    <el-table :data="rows" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="projectId" label="项目" width="90" />
      <el-table-column prop="matchScore" label="匹配度" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="时间" width="200" />
      <el-table-column prop="recommendReason" label="原因" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const rows = ref([])
onMounted(async () => {
  rows.value = (await http.get('/admin/recommendations')).data || []
})
</script>
