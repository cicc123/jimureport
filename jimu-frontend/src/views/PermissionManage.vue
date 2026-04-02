<template>
  <div>
    <el-card>
      <template #header><span>报表权限管理</span></template>
      <el-row :gutter="20">
        <el-col :span="12">
          <h3 style="margin-bottom: 12px">角色列表</h3>
          <el-table :data="roles" v-loading="roleLoading" border stripe highlight-current-row @current-change="onRoleSelect">
            <el-table-column prop="roleName" label="角色名称" />
            <el-table-column prop="roleKey" label="权限标识" />
            <el-table-column prop="dataScope" label="数据范围" width="120">
              <template #default="{ row }">{{ dataScopeMap[row.dataScope] || row.dataScope }}</template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="12">
          <h3 style="margin-bottom: 12px">角色权限</h3>
          <div v-if="!selectedRole" style="color: #999; padding: 40px 0; text-align: center">← 请选择一个角色</div>
          <template v-else>
            <el-descriptions :column="1" border style="margin-bottom: 16px">
              <el-descriptions-item label="角色名称">{{ selectedRole.roleName }}</el-descriptions-item>
              <el-descriptions-item label="权限标识">{{ selectedRole.roleKey }}</el-descriptions-item>
            </el-descriptions>
            <h4 style="margin-bottom: 8px">菜单权限</h4>
            <el-tree :data="menuTree" :default-checked-keys="roleMenuIds" show-checkbox node-key="id" :props="{ label: 'menuName' }" />
          </template>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const roles = ref([]), roleLoading = ref(false), selectedRole = ref(null), menuTree = ref([]), roleMenuIds = ref([])
const dataScopeMap = { '1': '全部', '2': '自定义', '3': '本部门', '4': '本部门及下级', '5': '仅本人' }

async function loadRoles() {
  roleLoading.value = true
  try { roles.value = await api.get('/api/permission/roles') || [] } finally { roleLoading.value = false }
}
async function onRoleSelect(row) {
  selectedRole.value = row
  try {
    const ids = await api.get(`/api/permission/roles/${row.id}/menus`)
    roleMenuIds.value = ids || []
  } catch { roleMenuIds.value = [] }
}
async function loadMenus() {
  try { menuTree.value = await api.get('/api/permission/menus/tree') || [] } catch {}
}
onMounted(() => { loadRoles(); loadMenus() })
</script>
