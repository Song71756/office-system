<template>
  <div class="schedule-container">
    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <el-tab-pane label="我的日程" name="my" />
      <el-tab-pane label="全部日程" name="all" />
    </el-tabs>

    <!-- 搜索栏 -->
    <el-card shadow="hover">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="关键字">
          <el-input v-model="queryParams.keyword" placeholder="搜索日程标题" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="success" v-if="(hasPermission('schedule:create'))||(hasPermission('schedule:create:myself')&&activeTab === 'my')"  @click="handleAdd">
            <el-icon><Plus /></el-icon> 新建日程
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日程列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="scheduleList" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="创建人" width="100">
          <template #default="{ row }">
            {{ row.realName || row.username || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
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
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" link size="small" v-if="(hasPermission('schedule:edit'))||(hasPermission('schedule:edit:myself')&&activeTab === 'my')" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" v-if="(hasPermission('schedule:delete'))||(hasPermission('schedule:delete:myself')&&activeTab === 'my')" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="viewDialogVisible" title="日程详情" width="600px">
      <el-descriptions :column="2" border v-if="currentSchedule">
        <el-descriptions-item label="标题" :span="2">{{ currentSchedule.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag size="small" :type="typeTagType(currentSchedule.type)">{{ typeText(currentSchedule.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag size="small" :type="priorityTagType(currentSchedule.priority)">{{ priorityText(currentSchedule.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTagType(currentSchedule.status)">{{ statusText(currentSchedule.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="全天日程">{{ currentSchedule.isAllDay === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentSchedule.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentSchedule.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="地点" :span="2">{{ currentSchedule.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参与人" :span="2">{{ currentSchedule.participants || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提醒">{{ remindText(currentSchedule.remindType) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentSchedule.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">{{ currentSchedule.content || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑日程' : '新建日程'" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入日程标题" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="工作" value="1" />
                <el-option label="会议" value="2" />
                <el-option label="个人" value="3" />
                <el-option label="其他" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="formData.priority">
                <el-radio label="L">低</el-radio>
                <el-radio label="M">中</el-radio>
                <el-radio label="H">高</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="formData.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="formData.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="全天日程">
          <el-switch v-model="formData.isAllDay" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="formData.location" placeholder="请输入地点" />
        </el-form-item>
        <el-form-item label="参与人">
          <el-input v-model="formData.participants" placeholder="多人用逗号分隔，如：张三,李四" />
        </el-form-item>
        <el-form-item label="提醒">
          <el-select v-model="formData.remindType" style="width: 100%">
            <el-option label="不提醒" :value="0" />
            <el-option label="提前5分钟" :value="1" />
            <el-option label="提前15分钟" :value="2" />
            <el-option label="提前1小时" :value="3" />
            <el-option label="准时提醒" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请输入日程详细内容" />
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
import { getMySchedulePage, getAllSchedulePage, saveSchedule, deleteSchedule, saveMySchedule, deleteMySchedule } from '@/api/schedule'

// ===== 权限 =====
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
const hasPermission = (perm) => permissions.includes(perm)

// ===== Tab 切换 =====
const activeTab = ref('my')

const handleTabChange = () => {
  queryParams.pageNum = 1
  queryParams.keyword = ''
  queryParams.status = null
  loadData()
}

// ===== 查询参数 =====
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null
})
const total = ref(0)
const scheduleList = ref([])

const loadData = async () => {
  try {
    const apiFn = activeTab.value === 'my' ? getMySchedulePage : getAllSchedulePage
    const res = await apiFn(queryParams)
    scheduleList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.status = null
  queryParams.pageNum = 1
  loadData()
}

// ===== 查看详情 =====
const viewDialogVisible = ref(false)
const currentSchedule = ref(null)

const handleView = (row) => {
  currentSchedule.value = row
  viewDialogVisible.value = true
}

// ===== 新增/编辑 =====
const formDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  title: '',
  type: '1',
  priority: 'M',
  startTime: '',
  endTime: '',
  isAllDay: 0,
  location: '',
  participants: '',
  remindType: 0,
  status: 0,
  content: ''
})

const formRules = {
  title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const resetForm = () => {
  formData.id = null
  formData.title = ''
  formData.type = '1'
  formData.priority = 'M'
  formData.startTime = ''
  formData.endTime = ''
  formData.isAllDay = 0
  formData.location = ''
  formData.participants = ''
  formData.remindType = 0
  formData.status = 0
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
    title: row.title,
    type: row.type ? String(row.type) : '1',
    priority: row.priority || 'M',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    isAllDay: row.isAllDay || 0,
    location: row.location || '',
    participants: row.participants || '',
    remindType: row.remindType || 0,
    status: row.status,
    content: row.content || ''
  })
  formDialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await activeTab.value === 'my' ? saveMySchedule(formData) : saveSchedule(formData)
      ElMessage.success(isEdit.value ? '编辑成功' : '新建成功')
      formDialogVisible.value = false
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      submitLoading.value = false
    }
  })
}

// ===== 删除 =====
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除日程「${row.title}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await activeTab.value === 'my' ? deleteMySchedule(row.id) : deleteSchedule(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 工具方法 =====
const typeText = (type) => {
  const map = { '1': '工作', '2': '会议', '3': '个人', '4': '其他' }
  return map[type] || '未知'
}
const typeTagType = (type) => {
  const map = { '1': '', '2': 'success', '3': 'warning', '4': 'info' }
  return map[type] || 'info'
}

const priorityText = (p) => {
  const map = { L: '低', M: '中', H: '高' }
  return map[p] || '-'
}
const priorityTagType = (p) => {
  const map = { L: 'info', M: '', H: 'danger' }
  return map[p] || 'info'
}

const statusText = (s) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已取消' }
  return map[s] || '未知'
}
const statusTagType = (s) => {
  const map = { 0: 'info', 1: '', 2: 'success', 3: 'warning' }
  return map[s] || 'info'
}

const remindText = (r) => {
  const map = { 0: '不提醒', 1: '提前5分钟', 2: '提前15分钟', 3: '提前1小时', 4: '准时提醒' }
  return map[r] || '-'
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
.schedule-container {
  padding: 20px;
}
.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
