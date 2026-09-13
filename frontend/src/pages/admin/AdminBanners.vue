<template>
  <div class="page-wrap">
    <AdminNav />
    <h1 class="text-[32px] font-semibold tracking-tight">公告与广告栏</h1>
    <p class="mt-2 text-mute text-[15px]">顶部公告和侧边广告栏可分别配置文案、字体、字号和颜色。</p>

    <div class="mt-8 grid lg:grid-cols-2 gap-6">
      <form v-for="form in forms" :key="form.slotKey" class="card p-7 space-y-4" @submit.prevent="save(form)">
        <h2 class="text-[21px] font-semibold tracking-tight">{{ form.slotKey === 'TOP' ? '顶部公告栏' : '侧边广告栏' }}</h2>
        <el-input v-model="form.content" type="textarea" rows="3" placeholder="显示文字" />
        <el-select v-model="form.fontFamily" class="w-full" placeholder="字体">
          <el-option label="系统默认 / Inter" value="Inter, -apple-system, BlinkMacSystemFont, &quot;PingFang SC&quot;, sans-serif" />
          <el-option label="苹方 / 黑体" value="&quot;PingFang SC&quot;, &quot;Microsoft YaHei&quot;, sans-serif" />
          <el-option label="宋体" value="&quot;Songti SC&quot;, SimSun, serif" />
          <el-option label="等宽" value="ui-monospace, SFMono-Regular, Consolas, monospace" />
        </el-select>
        <el-select v-model="form.fontSize" class="w-full" placeholder="字号">
          <el-option label="12px" value="12px" />
          <el-option label="13px" value="13px" />
          <el-option label="14px" value="14px" />
          <el-option label="16px" value="16px" />
          <el-option label="18px" value="18px" />
        </el-select>
        <div class="grid grid-cols-2 gap-3">
          <label class="text-[13px] text-mute">文字颜色
            <input v-model="form.color" type="color" class="mt-2 block h-10 w-full rounded-lg border-0 bg-transparent" />
            <el-input v-model="form.color" class="mt-2" />
          </label>
          <label class="text-[13px] text-mute">背景颜色
            <input v-model="form.bgColor" type="color" class="mt-2 block h-10 w-full rounded-lg border-0 bg-transparent" />
            <el-input v-model="form.bgColor" class="mt-2" />
          </label>
        </div>
        <el-select v-model="form.enabledFlag" class="w-full">
          <el-option label="显示" :value="1" />
          <el-option label="隐藏" :value="0" />
        </el-select>
        <div class="rounded-2xl px-4 py-3 text-center" :style="preview(form)">{{ form.content || '预览文字' }}</div>
        <el-button native-type="submit" type="primary" color="#0071e3">保存</el-button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api'
import { useSiteStore } from '../../site'
import AdminNav from './AdminNav.vue'

const site = useSiteStore()
const forms = reactive([
  empty('TOP', '该作品只售卖一次，保证独一无二', '#f5f5f7', '#1d1d1f'),
  empty('SIDE', '独一无二 · 售出即下架', '#1d1d1f', '#f5f5f7')
])

function empty(slotKey, content, color, bgColor) {
  return {
    slotKey,
    content,
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif',
    fontSize: '13px',
    color,
    bgColor,
    enabledFlag: 1
  }
}

function preview(form) {
  return {
    color: form.color,
    background: form.bgColor,
    fontFamily: form.fontFamily,
    fontSize: form.fontSize
  }
}

async function load() {
  const rows = (await http.get('/admin/banners')).data || []
  for (const row of rows) {
    const form = forms.find((item) => item.slotKey === row.slotKey)
    if (form) Object.assign(form, row)
  }
}

async function save(form) {
  await http.put('/admin/banners', form)
  ElMessage.success('已保存')
  await site.refresh()
}

onMounted(load)
</script>
