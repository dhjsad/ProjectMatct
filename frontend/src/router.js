import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './store'

const routes = [
  { path: '/', name: 'home', component: () => import('./pages/Home.vue') },
  { path: '/match', name: 'match', component: () => import('./pages/Match.vue') },
  { path: '/projects', name: 'projects', component: () => import('./pages/Projects.vue') },
  { path: '/projects/:id', name: 'project', component: () => import('./pages/ProjectDetail.vue') },
  { path: '/cart', name: 'cart', component: () => import('./pages/Cart.vue') },
  { path: '/custom', name: 'custom', component: () => import('./pages/Custom.vue') },
  { path: '/articles', name: 'articles', component: () => import('./pages/Articles.vue') },
  { path: '/articles/:id', name: 'article', component: () => import('./pages/ArticleDetail.vue') },
  { path: '/login', name: 'login', component: () => import('./pages/Login.vue'), meta: { guest: true } },
  { path: '/register', name: 'register', component: () => import('./pages/Register.vue'), meta: { guest: true } },
  { path: '/me', name: 'me', component: () => import('./pages/Me.vue'), meta: { auth: true } },
  { path: '/admin', name: 'admin', component: () => import('./pages/admin/AdminProjects.vue'), meta: { admin: true } },
  { path: '/admin/users', name: 'admin-users', component: () => import('./pages/admin/AdminUsers.vue'), meta: { admin: true } },
  { path: '/admin/claims', name: 'admin-claims', component: () => import('./pages/admin/AdminClaims.vue'), meta: { admin: true } },
  { path: '/admin/recommendations', name: 'admin-recs', component: () => import('./pages/admin/AdminRecommendations.vue'), meta: { admin: true } },
  { path: '/admin/online', name: 'admin-online', component: () => import('./pages/admin/AdminOnline.vue'), meta: { admin: true } },
  { path: '/admin/deploy-orders', name: 'admin-deploy', component: () => import('./pages/admin/AdminDeployOrders.vue'), meta: { admin: true } },
  { path: '/admin/banners', name: 'admin-banners', component: () => import('./pages/admin/AdminBanners.vue'), meta: { admin: true } },
  { path: '/admin/custom-orders', name: 'admin-custom', component: () => import('./pages/admin/AdminCustomOrders.vue'), meta: { admin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLogin) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.admin && !auth.isAdmin) {
    return { name: 'home' }
  }
  if (to.meta.guest && auth.isLogin) {
    return { name: 'home' }
  }
  return true
})

export default router
