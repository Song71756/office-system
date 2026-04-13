<template>
  <div class="role-container">
    <!-- 工具栏 -->
    <el-card shadow="hover">
      <div class="toolbar">
        <el-button type="success" v-if="hasPermission('role:add')" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增角色
        </el-button>
        <el-button @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </el-card>

    <!-- 角色列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="roleList" stripe border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link size="small" v-if="hasPermission('role:edit')" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" v-if="hasPermission('role:assign')" @click="handleAssignPerm(row)">分配权限</el-button>
            <el-button type="danger" link size="small" v-if="hasPermission('role:delete')" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="如 ROLE_ADMIN" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="如 系统管理员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px">
      <p style="margin-bottom: 12px; color: #606266">为角色 <strong>{{ currentRole?.roleName }}</strong> 分配权限：</p>
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        :props="{ label: 'permissionName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :check-strictly="true"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="submitAssignPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, saveRole, assignPermissions, deleteRole } from '@/api/role'
import { getPermissionList } from '@/api/permission'

// ===== 权限 =====
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
const hasPermission = (perm) => permissions.includes(perm)

// ===== 数据 =====
const roleList = ref([])

const loadData = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data || []
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
  roleCode: '',
  roleName: '',
  description: ''
})

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const resetForm = () => {
  formData.id = null
  formData.roleCode = ''
  formData.roleName = ''
  formData.description = ''
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
    roleCode: row.roleCode,
    roleName: row.roleName,
    description: row.description || ''
  })
  formDialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await saveRole(formData)
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
  ElMessageBox.confirm(`确定删除角色「${row.roleName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 分配权限 =====
const permDialogVisible = ref(false)
const permLoading = ref(false)
const currentRole = ref(null)
const permTreeRef = ref(null)
const permTree = ref([])

// 将扁平权限列表转为树形结构
const buildPermTree = (list) => {
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

const handleAssignPerm = async (row) => {
  currentRole.value = row
  try {
    const res = await getPermissionList()
    const allPerms = res.data || []
    permTree.value = buildPermTree(allPerms)
  } catch (error) {
    // 错误已在拦截器中处理
  }
  permDialogVisible.value = true
}

const submitAssignPerms = async () => {
  permLoading.value = true
  try {
    const checkedIds = permTreeRef.value.getCheckedKeys()
    const halfCheckedIds = permTreeRef.value.getHalfCheckedKeys()
    const allIds = [...checkedIds, ...halfCheckedIds]
    await assignPermissions(currentRole.value.id, allIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    permLoading.value = false
  }
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
.role-container {
  padding: 20px;
}
.toolbar {
  display: flex;
  gap: 10px;
}
</style>
