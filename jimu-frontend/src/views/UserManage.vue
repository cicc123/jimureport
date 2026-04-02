<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="showAdd">新增用户</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom: 16px">
        <el-input v-model="query.username" placeholder="用户名搜索" clearable style="width: 180px" @clear="loadData" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px; margin-left: 8px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
        <el-button type="primary" style="margin-left: 8px" @click="loadData">搜索</el-button>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="changeStatus(row)">{{ row.status === '0' ? '停用' : '启用' }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px; justify-content: center" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
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

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

const query = reactive({ username: '', status: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ id: '', username: '', realName: '', email: '', phone: '', status: '0' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await api.get('/api/user/list', { params: { pageNum: page.current, pageSize: page.size, username: query.username || undefined, status: query.status || undefined } })
    tableData.value = res.records || []
    page.total = res.total || 0
  } catch { } finally { loading.value = false }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { id: '', username: '', realName: '', email: '', phone: '', status: '0' })
  dialogVisible.value = true
}

function showEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/api/user', form)
      ElMessage.success('修改成功')
    } else {
      await api.post('/api/user', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally { saving.value = false }
}

async function changeStatus(row) {
  const newStatus = row.status === '0' ? '1' : '0'
  await api.put('/api/user/status', { id: row.id, status: newStatus })
  ElMessage.success('状态已更新')
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
  await api.delete(`/api/user/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
