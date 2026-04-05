<template>
  <div class="document-container">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <el-tab-pane label="我的公文" name="my" />
      <el-tab-pane label="全部公文" name="all" />
    </el-tabs>
    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <el-form :inline="true" :model="queryParams">
        <el-row :gutter="16" align="middle">
          <el-col :span="6">
            <el-form-item label="关键字">
              <el-input v-model="queryParams.keyword" placeholder="搜索标题或编号" clearable @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="审核中" :value="1" />
                <el-option label="已通过" :value="2" />
                <el-option label="已驳回" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
              <el-button @click="resetQuery">
                <el-icon><Refresh /></el-icon> 重置
              </el-button>
              <el-button type="success" v-if="hasPermission('document:create')" @click="handleAdd">
                <el-icon><Plus /></el-icon> 起草公文
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 公文列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="docList" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="docNumber" label="公文编号" width="140" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ row.type || '-' }}</template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="priorityTagType(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" v-if="hasPermission('document:edit') && row.status === 0" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" v-if="hasPermission('document:edit') && row.status === 0" @click="handleSubmit(row)">提交审核</el-button>
            <el-button type="primary" link size="small" v-if="hasPermission('document:approve') && row.status === 1" @click="handleApprove(row)">审批</el-button>
            <el-button type="danger" link size="small" v-if="hasPermission('document:delete')" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="公文详情" width="650px">
      <el-descriptions :column="2" border v-if="currentDoc">
        <el-descriptions-item label="公文编号">{{ currentDoc.docNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentDoc.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentDoc.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag size="small" :type="priorityTagType(currentDoc.priority)">{{ priorityText(currentDoc.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagType(currentDoc.status)">{{ statusText(currentDoc.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentDoc.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ currentDoc.approveComment || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(currentDoc.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div style="white-space: pre-wrap">{{ currentDoc.content || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 起草/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑公文' : '起草公文'" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="公文编号">
          <el-input v-model="formData.docNumber" placeholder="可选，如 [2026]001号" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公文标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="通知" value="通知" />
                <el-option label="请示" value="请示" />
                <el-option label="函" value="函" />
                <el-option label="报告" value="报告" />
                <el-option label="批复" value="批复" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="formData.priority">
                <el-radio :label="1">普通</el-radio>
                <el-radio :label="2">重要</el-radio>
                <el-radio :label="3">紧急</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="6" placeholder="请输入公文正文" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSave(0)">保存草稿</el-button>
        <el-button type="success" :loading="submitLoading" v-if="hasPermission('document:approve')" @click="handleSave(2)">直接发布</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveDialogVisible" title="公文审批" width="500px">
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="90px">
        <el-form-item label="审批结果" prop="status">
          <el-radio-group v-model="approveForm.status">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="approveComment">
          <el-input v-model="approveForm.approveComment" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="submitApprove">确认审批</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocumentPage, saveDocument, submitDocument, approveDocument, deleteDocument,getMyDocumentPages } from '@/api/document'

// ===== 权限 =====
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
const hasPermission = (perm) => permissions.includes(perm)

// ===== 查询参数 =====
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  personal: false
})
const total = ref(0)
const docList = ref([])
const activeTab = ref('all')

const loadData = async () => {
  try {
    const res = queryParams.personal 
      ? await getMyDocumentPages(queryParams)
      : await getDocumentPage(queryParams)
    docList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleTabChange = (tabName) => {
  queryParams.personal = tabName === 'my'
  handleSearch()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.personal = false
  activeTab.value = 'all'
  queryParams.pageNum = 1
  loadData()
}

// ===== 查看详情 =====
const viewDialogVisible = ref(false)
const currentDoc = ref(null)

const handleView = (row) => {
  currentDoc.value = row
  viewDialogVisible.value = true
}

// ===== 起草/编辑 =====
const formDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  docNumber: '',
  title: '',
  type: '',
  priority: 1,
  content: ''
})

const formRules = {
  title: [{ required: true, message: '请输入公文标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公文类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公文内容', trigger: 'blur' }]
}

const resetForm = () => {
  formData.id = null
  formData.docNumber = ''
  formData.title = ''
  formData.type = ''
  formData.priority = 1
  formData.content = ''
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  formDialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    docNumber: row.docNumber || '',
    title: row.title,
    type: row.type || '',
    priority: row.priority || 1,
    content: row.content || ''
  })
  formDialogVisible.value = true
}

const handleSave = (targetStatus) => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = { ...formData, status: targetStatus }
      await saveDocument(data)
      ElMessage.success(targetStatus === 2 ? '发布成功' : '保存成功')
      formDialogVisible.value = false
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// ===== 提交审核 =====
const handleSubmit = (row) => {
  ElMessageBox.confirm(`确定提交公文「${row.title}」进行审核吗？`, '提交确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      await submitDocument(row.id)
      ElMessage.success('已提交审核')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 审批 =====
const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  docId: null,
  status: 2,
  approveComment: ''
})
const approveRules = {
  status: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  approveComment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

const handleApprove = (row) => {
  approveForm.docId = row.id
  approveForm.status = 2
  approveForm.approveComment = ''
  approveDialogVisible.value = true
}

const submitApprove = () => {
  approveFormRef.value.validate(async (valid) => {
    if (!valid) return
    approveLoading.value = true
    try {
      await approveDocument(approveForm.docId, {
        status: approveForm.status,
        approveComment: approveForm.approveComment
      })
      ElMessage.success('审批完成')
      approveDialogVisible.value = false
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      approveLoading.value = false
    }
  })
}

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除公文「${row.title}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 工具方法 =====
const priorityText = (p) => {
  const map = { 1: '普通', 2: '重要', 3: '紧急' }
  return map[p] || '-'
}
const priorityTagType = (p) => {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[p] || 'info'
}

const statusText = (s) => {
  const map = { 0: '草稿', 1: '审核中', 2: '已通过', 3: '已驳回' }
  return map[s] || '未知'
}
const statusTagType = (s) => {
  const map = { 0: 'info', 1: '', 2: 'success', 3: 'danger' }
  return map[s] || 'info'
}

const formatTime = (datetime) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 16)
}

// ===== 初始化 =====
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.document-container {
  padding: 20px;
}
.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
