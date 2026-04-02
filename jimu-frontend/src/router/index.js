import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页', icon: 'Odometer' } },
      { path: 'user', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'role', name: 'RoleManage', component: () => import('../views/RoleManage.vue'), meta: { title: '角色管理', icon: 'UserFilled' } },
      { path: 'menu', name: 'MenuManage', component: () => import('../views/MenuManage.vue'), meta: { title: '菜单管理', icon: 'Menu' } },
      { path: 'dept', name: 'DeptManage', component: () => import('../views/DeptManage.vue'), meta: { title: '部门管理', icon: 'OfficeBuilding' } },
      { path: 'permission', name: 'PermissionManage', component: () => import('../views/PermissionManage.vue'), meta: { title: '报表权限', icon: 'Lock' } },
      { path: 'fill', name: 'FillFormManage', component: () => import('../views/FillFormManage.vue'), meta: { title: '填报管理', icon: 'Document' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') return next()
  const token = localStorage.getItem('token')
  if (!token) return next('/login')
  next()
})

export default router
