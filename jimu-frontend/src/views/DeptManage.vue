<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between">
          <span>部门管理</span>
          <el-button type="primary" :icon="Plus" @click="showAdd">新增部门</el-button>
        </div>
      </template>
      <el-table :data="deptTree" v-loading="loading" row-key="id" border :tree-props="{ children: 'children' }">
        <el-table-column prop="deptName" label="部门名称" width="260" />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group>
        </el-form-item>
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
const deptTree = ref([])
const form = reactive({ id: '', deptName: '', parentId: '0', orderNum: 0, leader: '', phone: '', email: '', status: '0' })

async function loadData() {
  loading.value = true
  try { deptTree.value = await api.get('/api/department/tree') || [] } finally { loading.value = false }
}
function showAdd() { isEdit.value = false; Object.assign(form, { id: '', deptName: '', parentId: '0', orderNum: 0, leader: '', phone: '', email: '', status: '0' }); dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) { await api.put('/api/department', form) } else { await api.post('/api/department', form) }
    ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '操作失败') } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该部门？', '提示', { type: 'warning' })
  await api.delete(`/api/department/${row.id}`); ElMessage.success('删除成功'); loadData()
}
onMounted(loadData)
</script>
