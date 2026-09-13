<template>
  <div class="page-wrap">
    <AdminNav />
    <div class="flex items-center justify-between">
      <h1 class="text-[32px] font-semibold tracking-tight">项目管理</h1>
      <el-button type="primary" color="#0071e3" @click="open()">新增项目</el-button>
    </div>
    <el-table :data="page.records" class="mt-6 table-shell">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="difficulty" label="难度" width="90" />
      <el-table-column prop="salePrice" label="售价" width="80" />
      <el-table-column prop="guidePrice" label="指导价" width="90" />
      <el-table-column label="售卖" width="90">
        <template #default="{ row }">{{ row.remainingCount > 0 ? '在售' : '已售出' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button link @click="open(row)">编辑</el-button>
          <el-button link @click="openFiles(row)">源码文件</el-button>
          <el-button link @click="push(row)">推送</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-4 flex justify-end">
      <el-pagination layout="prev, pager, next" :total="page.total" :page-size="page.size" :current-page="page.current" @current-change="load" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑项目' : '新增项目'" width="720px">
      <el-form label-position="top">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
        <div class="grid md:grid-cols-2 gap-3">
          <el-form-item label="类型"><el-input v-model="form.projectType" /></el-form-item>
          <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
          <el-form-item label="技术栈"><el-input v-model="form.techStack" /></el-form-item>
          <el-form-item label="难度 easy/medium/hard"><el-input v-model="form.difficulty" /></el-form-item>
          <el-form-item label="周期（天）"><el-input-number v-model="form.estimatedDuration" :min="7" /></el-form-item>
          <el-form-item label="是否在售（1 在售 / 0 已售）"><el-input-number v-model="form.remainingCount" :min="0" :max="1" /></el-form-item>
          <el-form-item label="售价（元）"><el-input-number v-model="form.salePrice" :min="0" /></el-form-item>
          <el-form-item label="技术指导价（元）"><el-input-number v-model="form.guidePrice" :min="0" /></el-form-item>
          <el-form-item label="远程部署价格（元）"><el-input-number v-model="form.deployPrice" :min="0" /></el-form-item>
          <el-form-item label="开放远程部署">
            <el-select v-model="form.deployServiceEnabled" class="w-full">
              <el-option label="开放" :value="1" />
              <el-option label="关闭" :value="0" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="适合用户"><el-input v-model="form.suitableFor" /></el-form-item>
        <el-form-item label="功能模块"><el-input v-model="form.modules" type="textarea" /></el-form-item>
        <el-form-item label="架构"><el-input v-model="form.architecture" type="textarea" /></el-form-item>
        <el-form-item label="数据库设计"><el-input v-model="form.dbDesign" type="textarea" /></el-form-item>
        <el-form-item label="部署说明"><el-input v-model="form.deployGuide" type="textarea" /></el-form-item>
        <el-form-item label="示例代码"><el-input v-model="form.sampleCode" type="textarea" /></el-form-item>
        <el-form-item label="教程"><el-input v-model="form.tutorial" type="textarea" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="form.status" placeholder="PUBLISHED" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="fileVisible" title="上传源码 / 教程文件" width="640px">
      <p class="text-sm text-ink/60 mb-3">领取用户下载「源码学习包」时，会打进你上传的 SOURCE 压缩包。部署教程即使不上传文件，也会按项目说明生成 Markdown。</p>
      <div class="flex flex-wrap gap-2 mb-4">
        <el-input v-model="upload.title" class="!w-40" placeholder="标题" />
        <el-select v-model="upload.resourceType" class="!w-36">
          <el-option label="源码包" value="SOURCE" />
          <el-option label="部署文档" value="DEPLOY_DOC" />
          <el-option label="其他资料" value="DOC" />
        </el-select>
        <el-select v-model="upload.accessType" class="!w-32">
          <el-option label="领取后" value="CLAIMED" />
          <el-option label="公开" value="PUBLIC" />
        </el-select>
        <input type="file" @change="onFile" />
        <el-button type="primary" :disabled="!upload.file" @click="doUpload">上传</el-button>
      </div>
      <el-table :data="files" size="small">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="resourceType" label="类型" width="110" />
        <el-table-column prop="fileName" label="文件" />
        <el-table-column label="" width="80">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeFile(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api'
import AdminNav from './AdminNav.vue'

const page = reactive({ records: [], total: 0, size: 20, current: 1 })
const visible = ref(false)
const fileVisible = ref(false)
const fileProjectId = ref(null)
const files = ref([])
const upload = reactive({ title: '', resourceType: 'SOURCE', accessType: 'CLAIMED', file: null })
const form = reactive(empty())

function empty() {
  return {
    id: null, name: '', description: '', projectType: '毕业设计', category: 'Java', techStack: '',
    difficulty: 'medium', estimatedDuration: 30, remainingCount: 1, salePrice: 399, guidePrice: 299, deployPrice: 199, deployServiceEnabled: 1,
    suitableFor: '', modules: '',
    architecture: '', dbDesign: '', deployGuide: '', sampleCode: '', tutorial: '', status: 'PUBLISHED'
  }
}

async function load(current = 1) {
  const res = await http.get('/admin/projects', { params: { page: current, size: 20 } })
  Object.assign(page, res.data)
}

function open(row) {
  Object.assign(form, empty(), row || {})
  visible.value = true
}

async function save() {
  if (form.id) {
    await http.put(`/admin/projects/${form.id}`, form)
    ElMessage.success('已保存')
  } else {
    await http.post('/admin/projects', form)
    ElMessage.success('已发布，正在按用户画像匹配并发送站内消息')
  }
  visible.value = false
  await load(page.current)
}

async function push(row) {
  const res = await http.post(`/admin/projects/${row.id}/push`)
  ElMessage.success(res.data?.message || '已推送')
}

async function remove(row) {
  await ElMessageBox.confirm(`删除「${row.name}」？`)
  await http.delete(`/admin/projects/${row.id}`)
  await load(page.current)
}

async function openFiles(row) {
  fileProjectId.value = row.id
  fileVisible.value = true
  files.value = (await http.get(`/admin/projects/${row.id}/resources`)).data || []
}

function onFile(e) {
  upload.file = e.target.files?.[0] || null
}

async function doUpload() {
  const data = new FormData()
  data.append('file', upload.file)
  data.append('title', upload.title)
  data.append('resourceType', upload.resourceType)
  data.append('accessType', upload.accessType)
  await http.post(`/admin/projects/${fileProjectId.value}/resources`, data)
  ElMessage.success('已上传')
  upload.file = null
  files.value = (await http.get(`/admin/projects/${fileProjectId.value}/resources`)).data || []
}

async function removeFile(row) {
  await http.delete(`/admin/projects/${fileProjectId.value}/resources/${row.id}`)
  files.value = (await http.get(`/admin/projects/${fileProjectId.value}/resources`)).data || []
}

onMounted(() => load(1))
</script>
