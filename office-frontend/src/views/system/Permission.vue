<template>
  <div class="perm-container">
    <!-- 工具栏 -->
    <el-card shadow="hover">
      <div class="toolbar">
        <el-button type="success" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon> 新增顶级权限
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </el-card>

    <!-- 权限树形表格 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="permTree" row-key="id" border default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="permissionName" label="权限名称" min-width="180" />
        <el-table-column prop="permissionCode" label="权限编码" width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.path || '-' }}</template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">{{ row.icon || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70">
          <template #default="{ row }">{{ row.sort ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="handleAdd(row)">新增子权限</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑权限' : '新增权限'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="formData.permissionName" placeholder="如 新增用户" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="formData.permissionCode" placeholder="如 user:add" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option label="目录" value="M" />
            <el-option label="菜单" value="C" />
            <el-option label="按钮" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级权限">
          <el-select v-model="formData.parentId" placeholder="无（顶级）" clearable style="width: 100%">
            <el-option label="无（顶级权限）" :value="0" />
            <el-option v-for="perm in flatPermList" :key="perm.id" :label="perm.permissionName + '（' + perm.permissionCode + '）'" :value="perm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="formData.path" placeholder="路由路径或API地址" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="图标名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
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
import { getPermissionList, savePermission, deletePermission } from '@/api/permission'

// ===== 数据 =====
const permTree = ref([])
const flatPermList = ref([])

// 将扁平列表转为树形结构
const buildTree = (list) => {
  const map = {}
  const tree = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      tree.push(map[item.id])
    }
  })
  return tree
}

const loadData = async () => {
  try {
    const res = await getPermissionList()
    const allPerms = res.data || []
    flatPermList.value = allPerms
    permTree.value = buildTree(allPerms)
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
  permissionName: '',
  permissionCode: '',
  type: 'F',
  parentId: 0,
  path: '',
  icon: '',
  sort: 0
})

const formRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const resetForm = () => {
  formData.id = null
  formData.permissionName = ''
  formData.permissionCode = ''
  formData.type = 'F'
  formData.parentId = 0
  formData.path = ''
  formData.icon = ''
  formData.sort = 0
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
    permissionName: row.permissionName,
    permissionCode: row.permissionCode,
    type: row.type || 'F',
    parentId: row.parentId || 0,
    path: row.path || '',
    icon: row.icon || '',
    sort: row.sort || 0
  })
  formDialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await savePermission(formData)
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
  ElMessageBox.confirm(`确定删除权限「${row.permissionName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePermission(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 工具方法 =====
const typeText = (t) => {
  const map = { M: '目录', C: '菜单', F: '按钮' }
  return map[t] || '未知'
}
const typeTagType = (t) => {
  const map = { M: '', C: 'success', F: 'warning' }
  return map[t] || 'info'
}

// ===== 初始化 =====
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.perm-container {
  padding: 20px;
}
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
