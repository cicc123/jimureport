<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between">
          <span>菜单管理</span>
          <el-button type="primary" :icon="Plus" @click="showAdd">新增菜单</el-button>
        </div>
      </template>
      <el-table :data="menuTree" v-loading="loading" row-key="id" border :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" width="200" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }"><el-icon><component :is="row.icon" /></el-icon></template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="path" label="路由地址" />
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="warning">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 'F'" type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio-button label="M">目录</el-radio-button>
            <el-radio-button label="C">菜单</el-radio-button>
            <el-radio-button label="F">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="路由地址"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.perms" placeholder="如 system:user:list" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), isEdit = ref(false), formRef = ref()
const menuTree = ref([])
const form = reactive({ id: '', menuName: '', parentId: '0', menuType: 'M', path: '', perms: '', orderNum: 0 })

async function loadData() {
  loading.value = true
  try { menuTree.value = await api.get('/api/menu/list') || [] } finally { loading.value = false }
}
function showAdd() { isEdit.value = false; Object.assign(form, { id: '', menuName: '', parentId: '0', menuType: 'M', path: '', perms: '', orderNum: 0 }); dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await api.put('/api/menu', form) } else { await api.post('/api/menu', form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '操作失败') } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该菜单？', '提示', { type: 'warning' })
  await api.delete(`/api/menu/${row.id}`); ElMessage.success('删除成功'); loadData()
}
onMounted(loadData)
</script>
