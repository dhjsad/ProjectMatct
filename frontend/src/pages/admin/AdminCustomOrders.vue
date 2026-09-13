<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">商业定制</h1>
    <el-table :data="rows" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="contactName" label="联系人" width="110" />
      <el-table-column prop="contact" label="联系方式" width="140" />
      <el-table-column prop="company" label="公司" width="120" />
      <el-table-column prop="title" label="主题" />
      <el-table-column prop="budget" label="预算" width="100" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link @click="show(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" link @click="setStatus(row, 'IN_PROGRESS')">接洽</el-button>
          <el-button v-if="row.status === 'IN_PROGRESS'" link @click="setStatus(row, 'DONE')">完成</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="定制详情" width="560px">
      <pre class="text-[13px] whitespace-pre-wrap leading-7 text-mute">{{ current?.requirement }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const rows = ref([])
const visible = ref(false)
const current = ref(null)

async function load() {
  rows.value = (await http.get('/admin/custom-orders')).data || []
}

function show(row) {
  current.value = row
  visible.value = true
}

async function setStatus(row, status) {
  await http.post(`/admin/custom-orders/${row.id}/status`, null, { params: { status } })
  ElMessage.success('已更新')
  await load()
}

onMounted(load)
</script>
