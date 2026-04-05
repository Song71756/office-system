<template>
  <div class="notice-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="关键字">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入公告标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已撤回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 + 表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd" v-if="hasPermission('notice:publish')">
            发布公告
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1">全体公告</el-tag>
            <el-tag v-else-if="row.type === 2" type="warning">部门通知</el-tag>
            <el-tag v-else-if="row.type === 3" type="danger">会议通知</el-tag>
            <el-tag v-else type="info">其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">已撤回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.priority === 'H'" type="danger" effect="dark">紧急</el-tag>
            <el-tag v-else-if="row.priority === 'M'" type="warning" effect="dark">重要</el-tag>
            <el-tag v-else type="info" effect="dark">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="80" align="center" />
        <el-table-column prop="publishTime" label="发布时间" width="170" align="center">
          <template #default="{ row }">
            {{ row.publishTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link :icon="Edit" @click="handleEdit(row)" v-if="hasPermission('notice:publish')">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)" v-if="hasPermission('notice:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="公告详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="viewData.type === 1">全体公告</el-tag>
          <el-tag v-else-if="viewData.type === 2" type="warning">部门通知</el-tag>
          <el-tag v-else-if="viewData.type === 3" type="danger">会议通知</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag v-if="viewData.priority === 'H'" type="danger">紧急</el-tag>
          <el-tag v-else-if="viewData.priority === 'M'" type="warning">重要</el-tag>
          <el-tag v-else type="info">普通</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="viewData.status === 0" type="info">草稿</el-tag>
          <el-tag v-else-if="viewData.status === 1" type="success">已发布</el-tag>
          <el-tag v-else-if="viewData.status === 2" type="warning">已撤回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="阅读量">{{ viewData.viewCount }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ viewData.publishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ viewData.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2" v-if="viewData.attachmentName">
          <div class="attachment-wrapper">
             <el-icon><Document /></el-icon>
             <el-link 
               type="primary" 
               @click="handleDownload(viewData)"
               class="attachment-link"
             >
               {{ viewData.attachmentName }}
            </el-link>
            <el-button 
              type="primary" 
              link 
              :icon="Download"
              @click="handleDownload(viewData)"
            >
               下载
            </el-button>
        </div>
      </el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div class="notice-content" v-html="viewData.content"></div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="formData.id ? '编辑公告' : '发布公告'" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="全体公告" :value="1" />
            <el-option label="部门通知" :value="2" />
            <el-option label="会议通知" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="formData.priority">
            <el-radio value="L">普通</el-radio>
            <el-radio value="M">重要</el-radio>
            <el-radio value="H">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <!-- 附件上传 -->
        <el-form-item label="附件" prop="attachment">
          <el-upload
             class="upload-demo"
             drag
             :http-request="handleUpload"
             :show-file-list="true"
             :on-success="handleUploadSuccess"
             :on-remove="handleUploadRemove"
             :limit="1">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
             拖拽文件到这或者<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传单个文件，且不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="过期时间">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择过期时间（可选）"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">保存为草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Delete, UploadFilled } from '@element-plus/icons-vue'
import { getNoticePage, getNoticeById, saveNotice, deleteNotice } from '@/api/notice'
import { uploadOffice,deleteFileByNewFileName } from '@/api/file'

// 权限判断
const hasPermission = (perm) => {
  const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
  return permissions.includes(perm)
}

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: undefined
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref({})

// 新增/编辑表单
const formDialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const defaultFormData = {
  id: null,
  title: '',
  content: '',
  type: 1,
  priority: 'L',
  status: 1,
  attachmentName: '',
  attachmentPath: '',
  newFileName: '',
  endTime: null
}

const formData = reactive({ ...defaultFormData })

const formRules = reactive({
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
})

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNoticePage(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await getNoticeById(row.id)
    viewData.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 新增
const handleAdd = () => {
  Object.assign(formData, { ...defaultFormData })
  formDialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  try {
    const res = await getNoticeById(row.id)
    const data = res.data
    Object.assign(formData, {
      id: data.id,
      title: data.title,
      content: data.content,
      type: data.type,
      priority: data.priority || 'L',
      status: data.status,
      endTime: data.endTime,
      attachmentName: data.attachmentName,
      attachmentPath: data.attachmentPath,
      newFileName: data.newFileName
    })
    formDialogVisible.value = true
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 提交表单
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await saveNotice(formData)
      ElMessage.success(formData.id ? '编辑成功' : '发布成功')
      formDialogVisible.value = false
      fetchData()
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除公告「${row.title}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 先删除附件（如果有），再删除公告记录
      if(row.newFileName) {
        await deleteFileByNewFileName(row.newFileName)
      }
      await deleteNotice(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}


// 上传附件
const handleUpload = async (options) => {
  try {
    const res = await uploadOffice(options.file)
    return  res
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// 文件上传成功处理
const handleUploadSuccess = (response, file) => {
  // response 是后端返回的 Result 对象，实际数据在 response.data 中
  if (response.code === 200) {
    const fileName = response.data.originalName // 文件原始名称
    const fileUrl = response.data.fileUrl // 文件URL
    formData.attachmentName = fileName
    formData.attachmentPath = fileUrl
    formData.newFileName = response.data.newFileName // 后端存储的新的文件名（如果需要）
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error('文件上传失败：' + response.message)
  }
}


// 文件移除处理
const handleUploadRemove = (file, fileList) => {
  formData.attachmentName = ''
  formData.attachmentPath = ''
  formData.newFileName = ''
}


//附件下载
const handleDownload = (row) => {
  if (!row.attachmentPath) {
    ElMessage.warning('没有附件可下载')
    return
  }

  // 创建一个隐藏的链接元素，触发下载
  const link = document.createElement('a')
  link.href = row.attachmentPath
  link.download = row.attachmentName || '附件'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}


// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { ...defaultFormData })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.notice-container {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.notice-content {
  line-height: 1.8;
  word-break: break-all;
  white-space: pre-wrap;
}

.attachment-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.attachment-link {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
