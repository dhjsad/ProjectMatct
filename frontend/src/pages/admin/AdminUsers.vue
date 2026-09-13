<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">用户与画像</h1>
    <el-table :data="users" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link @click="show(row)">画像</el-button>
          <el-button link @click="openPwd(row)">改密</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="用户详情" width="560px">
      <pre class="text-xs whitespace-pre-wrap">{{ JSON.stringify(detail, null, 2) }}</pre>
    </el-dialog>
    <el-dialog v-model="pwdVisible" :title="`修改 ${pwdUser?.username || ''} 的密码`" width="420px">
      <el-input v-model="newPassword" type="password" placeholder="新密码至少 6 位" show-password />
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="resetPwd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const users = ref([])
const visible = ref(false)
const detail = ref(null)
const pwdVisible = ref(false)
const pwdUser = ref(null)
const newPassword = ref('')

onMounted(async () => {
  users.value = (await http.get('/admin/users')).data || []
})

async function show(row) {
  detail.value = (await http.get(`/admin/users/${row.id}/profile`)).data
  visible.value = true
}

function openPwd(row) {
  pwdUser.value = row
  newPassword.value = ''
  pwdVisible.value = true
}

async function resetPwd() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.error('新密码至少 6 位')
    return
  }
  await http.put(`/admin/users/${pwdUser.value.id}/password`, { newPassword: newPassword.value })
  pwdVisible.value = false
  ElMessage.success('密码已重置')
}
</script>
