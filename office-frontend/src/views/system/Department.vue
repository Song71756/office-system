<template>
  <div class="dept-container">
    <!-- 工具栏 -->
    <el-card shadow="hover">
      <div class="toolbar">
        <el-button type="success" v-if="hasPermission('dept:edit')" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon> 新增顶级部门
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </el-card>

    <!-- 部门树形表格 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="deptTree" row-key="id" border default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80">
          <template #default="{ row }">{{ row.orderNum ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" v-if="hasPermission('dept:edit')" @click="handleAdd(row)">新增子部门</el-button>
            <el-button type="warning" link size="small" v-if="hasPermission('dept:edit')" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" v-if="hasPermission('dept:delete')" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-select v-model="formData.parentId" placeholder="无（顶级部门）" clearable style="width: 100%">
            <el-option label="无（顶级部门）" :value="0" />
            <el-option v-for="dept in flatDeptList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.orderNum" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入部门描述" />
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
import { getDepartmentTree, getDepartmentList, saveDepartment, deleteDepartment } from '@/api/department'

// ===== 权限 =====
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
const hasPermission = (perm) => permissions.includes(perm)

// ===== 数据 =====
const deptTree = ref([])
const flatDeptList = ref([])

const loadData = async () => {
  try {
    const res = await getDepartmentTree()
    deptTree.value = res.data || []
    // 同时加载扁平列表用于上级部门选择
    const listRes = await getDepartmentList()
    flatDeptList.value = listRes.data || []
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// ===== 新增/编辑 =====
const formDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  name: '',
  parentId: 0,
  orderNum: 0,
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const resetForm = () => {
  formData.id = null
  formData.name = ''
  formData.parentId = 0
  formData.orderNum = 0
  formData.description = ''
}

const handleAdd = (parent) => {
  isEdit.value = false
  resetForm()
  if (parent) {
    formData.parentId = parent.id
  }
  formDialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    parentId: row.parentId || 0,
    orderNum: row.orderNum || 0,
    description: row.description || ''
  })
  formDialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await saveDepartment(formData)
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
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
  ElMessageBox.confirm(`确定删除部门「${row.name}」吗？若存在下级部门或关联员工将无法删除`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 工具方法 =====
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
.dept-container {
  padding: 20px;
}
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
