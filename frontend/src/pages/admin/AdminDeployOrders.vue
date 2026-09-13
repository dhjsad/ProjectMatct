<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">远程部署订单</h1>
    <el-table :data="rows" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户" width="80" />
      <el-table-column prop="projectId" label="项目" width="80" />
      <el-table-column prop="amount" label="金额" width="80" />
      <el-table-column prop="status" label="状态" width="130" />
      <el-table-column prop="contact" label="联系方式" />
      <el-table-column prop="environmentNote" label="环境说明" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PAID'" link @click="setStatus(row, 'IN_PROGRESS')">开始处理</el-button>
          <el-button v-if="row.status === 'IN_PROGRESS'" link @click="setStatus(row, 'DONE')">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const rows = ref([])

async function load() {
  rows.value = (await http.get('/admin/deploy-orders')).data || []
}

async function setStatus(row, status) {
  await http.post(`/admin/deploy-orders/${row.id}/status`, null, { params: { status } })
  ElMessage.success('已更新')
  await load()
}

onMounted(load)
</script>
