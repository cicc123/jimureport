<template>
  <div>
    <h2 style="margin-bottom: 20px">系统概览</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in cards" :key="item.title">
        <el-card shadow="hover" style="margin-bottom: 16px">
          <div style="display: flex; align-items: center; gap: 16px">
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
            <div>
              <div style="font-size: 24px; font-weight: bold">{{ item.value }}</div>
              <div style="color: #999; font-size: 13px">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card>
      <template #header><span>快速入口</span></template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/user')">用户管理</el-button>
        <el-button type="success" @click="$router.push('/role')">角色管理</el-button>
        <el-button type="warning" @click="$router.push('/permission')">报表权限</el-button>
        <el-button type="info" @click="goReport">积木报表</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const cards = ref([
  { title: '用户总数', value: 0, icon: 'User', color: '#409EFF' },
  { title: '角色数量', value: 0, icon: 'UserFilled', color: '#67C23A' },
  { title: '菜单数量', value: 0, icon: 'Menu', color: '#E6A23C' },
  { title: '填报表单', value: 0, icon: 'Document', color: '#F56C6C' }
])

function goReport() {
  const token = localStorage.getItem('token')
  window.open(`/jmreport/list?token=${token}`)
}

onMounted(async () => {
  try {
    const users = await api.get('/api/user/list', { params: { pageNum: 1, pageSize: 1 } })
    cards.value[0].value = users.total || 0
  } catch {}
  try {
    const roles = await api.get('/api/role/list')
    cards.value[1].value = roles.length || 0
  } catch {}
  try {
    const menus = await api.get('/api/menu/list')
    cards.value[2].value = menus.length || 0
  } catch {}
  try {
    const forms = await api.get('/api/fill/forms', { params: { pageNum: 1, pageSize: 1 } })
    cards.value[3].value = forms.total || 0
  } catch {}
})
</script>
