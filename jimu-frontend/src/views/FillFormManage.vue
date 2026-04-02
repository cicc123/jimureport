<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between">
          <span>填报表单管理</span>
          <el-button type="primary" :icon="Plus" @click="showCreate">新建表单</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="formName" label="表单名称" />
        <el-table-column prop="reportId" label="报表ID" width="200" />
        <el-table-column prop="formDesc" label="描述" show-overflow-tooltip />
        <el-table-column prop="allowDuplicate" label="重复提交" width="100">
          <template #default="{ row }">
            <el-tag :type="row.allowDuplicate === '1' ? 'warning' : 'success'">{{ row.allowDuplicate === '1' ? '允许' : '禁止' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="allowDraft" label="草稿" width="80">
          <template #default="{ row }">
            <el-tag :type="row.allowDraft === '1' ? 'success' : 'info'">{{ row.allowDraft === '1' ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRecords(row)">提交记录</el-button>
            <el-button link type="success" @click="exportData(row)">导出</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top: 16px; justify-content: center" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建填报表单" width="500px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="报表ID" prop="reportId"><el-input v-model="form.reportId" placeholder="输入积木报表ID" /></el-form-item>
        <el-form-item label="表单名称" prop="formName"><el-input v-model="form.formName" /></el-form-item>
        <el-form-item label="表单描述"><el-input v-model="form.formDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="允许草稿">
          <el-switch v-model="form.allowDraft" active-value="1" inactive-value="0" />
        </el-form-item>
        <el-form-item label="重复提交">
          <el-switch v-model="form.allowDuplicate" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const loading = ref(false), saving = ref(false), dialogVisible = ref(false), formRef = ref()
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ reportId: '', formName: '', formDesc: '', allowDraft: '0', allowDuplicate: '0' })

async function loadData() {
  loading.value = true
  try {
    const res = await api.get('/api/fill/forms', { params: { pageNum: page.current, pageSize: page.size } })
    tableData.value = res.records || []; page.total = res.total || 0
  } catch { } finally { loading.value = false }
}
function showCreate() { Object.assign(form, { reportId: '', formName: '', formDesc: '', allowDraft: '0', allowDuplicate: '0' }); dialogVisible.value = true }
async function handleCreate() {
  saving.value = true
  try {
    await api.post('/api/fill/forms', form); ElMessage.success('创建成功'); dialogVisible.value = false; loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '创建失败') } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该表单配置？', '提示', { type: 'warning' })
  await api.delete(`/api/fill/forms/${row.id}`); ElMessage.success('删除成功'); loadData()
}
function viewRecords(row) { ElMessage.info(`表单 ${row.formName} 的提交记录`) }
async function exportData(row) {
  try {
    const token = localStorage.getItem('token')
    window.open(`/api/fill/export/${row.formId}?X-Access-Token=${token}`)
  } catch { ElMessage.error('导出失败') }
}
onMounted(loadData)
</script>
