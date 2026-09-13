<template>
  <nav class="mb-8 flex flex-wrap gap-2 text-[13px]">
    <router-link
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="px-3.5 py-1.5 rounded-full text-mute hover:text-ink hover:bg-white"
      active-class=""
      exact-active-class="!bg-[#1d1d1f] !text-white"
    >
      {{ item.label }}
    </router-link>
  </nav>
</template>

<script setup>
const items = [
  { to: '/admin', label: '项目' },
  { to: '/admin/users', label: '用户' },
  { to: '/admin/claims', label: '售出' },
  { to: '/admin/banners', label: '公告栏' },
  { to: '/admin/custom-orders', label: '定制' },
  { to: '/admin/recommendations', label: '推荐' },
  { to: '/admin/online', label: '访问统计' },
  { to: '/admin/deploy-orders', label: '部署订单' }
]
</script>
