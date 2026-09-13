<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">领取记录</h1>
    <el-table :data="claims" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="projectId" label="项目" width="90" />
      <el-table-column prop="deliveryType" label="发货" width="160" />
      <el-table-column prop="contact" label="联系方式" />
      <el-table-column prop="amount" label="售价" width="80" />
      <el-table-column prop="guideAmount" label="指导费" width="90" />
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
