import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const permissions = ref([])

  async function login(data) {
    const res = await api.post('/api/user/login', data)
    token.value = res.token
    userInfo.value = res
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res))
    return res
  }

  async function fetchPermissions() {
    try {
      const res = await api.get('/api/permission/current/permissions')
      permissions.value = res || []
    } catch { permissions.value = [] }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    api.post('/api/user/logout').catch(() => {})
  }

  function hasPermission(perm) {
    if (permissions.value.includes('*:*:*')) return true
    return permissions.value.includes(perm)
  }

  return { token, userInfo, permissions, login, logout, fetchPermissions, hasPermission }
})
