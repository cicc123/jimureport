<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between">
          <span>角色管理</span>
          <el-button type="primary" :icon="Plus" @click="showAdd">新增角色</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="roleName" label="角色名称" width="180" />
        <el-table-column prop="roleKey" label="权限标识" width="180" />
        <el-table-column prop="roleSort" label="排序" width="80" />
        <el-table-column prop="dataScope" label="数据范围" width="120">
          <template #default="{ row }">
            <el-tag>{{ dataScopeMap[row.dataScope] || row.dataScope }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="权限标识" prop="roleKey"><el-input v-model="form.roleKey" /></el-form-item>
        <el-form-item label="排序" prop="roleSort"><el-input-number v-model="form.roleSort" :min="0" /></el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope">
            <el-option v-for="(v, k) in dataScopeMap" :key="k" :label="v" :value="k" />
          </el-select>
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

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const formRef = ref()
const tableData = ref([])
const dataScopeMap = { '1': '全部', '2': '自定义', '3': '本部门', '4': '本部门及以下', '5': '仅本人' }
const form = reactive({ id: '', roleName: '', roleKey: '', roleSort: 1, dataScope: '1' })
const rules = { roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }], roleKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }] }

async function loadData() {
  loading.value = true
  try { tableData.value = await api.get('/api/role/list') || [] } finally { loading.value = false }
}
function showAdd() { isEdit.value = false; Object.assign(form, { id: '', roleName: '', roleKey: '', roleSort: 1, dataScope: '1' }); dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  await formRef.value.validate(); saving.value = true
  try {
    if (isEdit.value) { await api.put('/api/role', form) } else { await api.post('/api/role', form) }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '操作失败') } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' })
  await api.delete(`/api/role/${row.id}`); ElMessage.success('删除成功'); loadData()
}
onMounted(loadData)
</script>
