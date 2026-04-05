<template>
  <div class="file-container">
    <!-- 工具栏 -->
    <el-card shadow="hover">
      <div class="toolbar">
        <div class="toolbar-left">
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumb"
              :key="item.id"
              @click="navigateTo(item, index)"
              :class="{ clickable: index < breadcrumb.length - 1 }"
            >
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="keyword"
            placeholder="搜索文件名"
            clearable
            style="width: 200px; margin-right: 10px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button-group style="margin-left: 10px">
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </el-button>
            <el-button :type="viewMode === 'tree' ? 'primary' : 'default'" @click="viewMode = 'tree'">
              <el-icon><FolderOpened /></el-icon>
            </el-button>
          </el-button-group>
          <el-button type="success" v-if="hasPermission('file:edit')" @click="showNewFolderDialog" style="margin-left: 10px">
            <el-icon><FolderAdd /></el-icon> 新建文件夹
          </el-button>
          <el-upload
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            style="display: inline-block; margin-left: 10px"
          >
            <el-button type="warning">
              <el-icon><Upload /></el-icon> 上传文件
            </el-button>
          </el-upload>
        </div>
      </div>
    </el-card>

    <!-- 文件列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <!-- 列表视图 -->
      <template v-if="viewMode === 'list'">
        <el-table :data="fileList" stripe border style="width: 100%" @row-dblclick="handleRowDblClick">
          <el-table-column label="ID" width="80" prop="id" />
          <el-table-column label="名称" min-width="250">
            <template #default="{ row }">
              <div class="file-name-cell">
                <el-icon :size="20" :color="isFolder(row) ? '#e6a23c' : '#409eff'" style="margin-right: 8px">
                  <Folder v-if="isFolder(row)" />
                  <Document v-else />
                </el-icon>
                <span class="file-name" @click="handleRowDblClick(row)">{{ row.originalName || row.fileName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="110">
            <template #default="{ row }">
              {{ isFolder(row) ? '-' : formatSize(row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" v-if="isFolder(row)" type="warning">文件夹</el-tag>
              <span v-else>{{ row.fileType || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="下载次数" width="90" prop="downloadCount">
            <template #default="{ row }">{{ isFolder(row) ? '-' : (row.downloadCount || 0) }}</template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button v-if="!isFolder(row)" type="primary" link size="small" @click="handleDownload(row)">下载</el-button>
              <el-button type="warning" link size="small" v-if="hasPermission('file:edit')" @click="handleRename(row)">重命名</el-button>
              <el-button type="info" link size="small" v-if="hasPermission('file:edit')" @click="handleMove(row)">移动</el-button>
              <el-button type="danger" link size="small" v-if="hasPermission('file:delete')" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <div class="pagination-area">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadData"
            @current-change="loadData"
          />
        </div>
      </template>

      <!-- 树形视图 -->
      <template v-else-if="viewMode === 'tree'">
        <el-tree
          :props="treeProps"
          :load="loadTreeChildren"
          lazy
          node-key="id"
          :default-expanded-keys="expandedKeys"
          @node-expand="handleNodeExpand"
          @node-collapse="handleNodeCollapse"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <div class="tree-node-content">
                <el-icon :size="18" :color="isFolder(data) ? '#e6a23c' : '#409eff'" style="margin-right: 8px">
                  <Folder v-if="isFolder(data)" />
                  <Document v-else />
                </el-icon>
                <span class="tree-node-id">[{{ data.id }}]</span>
                <span class="tree-node-name">{{ data.originalName || data.fileName }}</span>
                <span class="tree-node-info" v-if="!isFolder(data)">
                  {{ formatSize(data.fileSize) }}
                  <span class="tree-node-time">{{ formatTime(data.createTime, 'short') }}</span>
                </span>
              </div>
              <div class="tree-node-actions" @click.stop>
                <el-button v-if="!isFolder(data)" type="primary" link size="small" @click="handleDownload(data)">下载</el-button>
                <el-button type="warning" link size="small" v-if="hasPermission('file:edit')" @click="handleRename(data)">重命名</el-button>
                <el-button type="info" link size="small" v-if="hasPermission('file:edit')" @click="handleMove(data)">移动</el-button>
                <el-button type="danger" link size="small" v-if="hasPermission('file:delete')" @click="handleDelete(data)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </template>
    </el-card>

    <!-- 新建文件夹弹窗 -->
    <el-dialog v-model="folderDialogVisible" title="新建文件夹" width="400px">
      <el-form ref="folderFormRef" :model="folderForm" :rules="folderRules">
        <el-form-item label="文件夹名称" prop="folderName">
          <el-input v-model="folderForm.folderName" placeholder="请输入文件夹名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="folderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="folderLoading" @click="submitNewFolder">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px">
      <el-form ref="renameFormRef" :model="renameForm" :rules="renameRules">
        <el-form-item label="新名称" prop="newName">
          <el-input v-model="renameForm.newName" placeholder="请输入新名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="renameLoading" @click="submitRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 移动弹窗 -->
    <el-dialog v-model="moveDialogVisible" title="移动到" width="400px">
      <el-form :model="moveForm">
        <el-form-item label="目标文件夹">
          <el-tree-select
            v-model="moveForm.targetParentId"
            :data="folderTree"
            :props="folderTreeProps"
            :render-after-expand="false"
            check-strictly
            clearable
            placeholder="请选择目标文件夹（留空为根目录）"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="moveLoading" @click="submitMove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getChildFiles, uploadOffice, createFolder, renameFile, moveFile, deleteFile, downloadFile } from '@/api/file'
import { useFileStore } from '@/stores/file'

// ===== Pinia Store =====
const fileStore = useFileStore()

// ===== 权限 =====
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
const hasPermission = (perm) => permissions.includes(perm)

// ===== 面包屑导航 =====
const breadcrumb = ref([{ id: 0, name: '全部文件' }])
const currentParentId = ref(0)

const navigateTo = (item, index) => {
  if (index >= breadcrumb.value.length - 1) return
  breadcrumb.value = breadcrumb.value.slice(0, index + 1)
  currentParentId.value = item.id
  pageNum.value = 1
  loadData()
}

// ===== 文件列表 =====
const fileList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const viewMode = ref('list') // 'list' 或 'tree'

const loadData = async (forceRefresh = false) => {
  try {
    // 使用 Pinia store 获取数据（带缓存）
    const res = await fileStore.fetchFileList({
      parentId: currentParentId.value === 0 ? null : currentParentId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }, forceRefresh)
    
    fileList.value = res.data.list || []
    total.value = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

// ===== 双击进入文件夹 =====
const isFolder = (row) => {
  return row.fileType === 'folder' || (!row.fileType && !row.filePath)
}

const handleRowDblClick = (row) => {
  if (isFolder(row)) {
    breadcrumb.value.push({ id: row.id, name: row.originalName || row.fileName })
    currentParentId.value = row.id
    pageNum.value = 1
    keyword.value = ''
    loadData()
  }
}

// ===== 上传文件 =====
const beforeUpload = (file) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  try {
    await uploadOffice(options.file)
    ElMessage.success('上传成功')
    // 清除缓存，强制刷新数据
    fileStore.clearAllFileListCache()
    loadData(true)
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// ===== 下载 =====
const handleDownload = async (row) => {
  try {
    // 使用downloadFile API，会自动通过axios拦截器添加token
    const response = await downloadFile(row.id)
    
    // 创建Blob对象进行下载（response.data包含二进制数据）
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.originalName || row.fileName
    link.target = '_blank'
    
    document.body.appendChild(link)
    link.click()
    
    // 清理资源
    setTimeout(() => {
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }, 100)
    
    ElMessage.success('下载开始')
    
  } catch (error) {
    console.error('下载失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('下载失败：' + (error.response?.data?.message || error.message))
    }
  }
}

// ===== 新建文件夹 =====
const folderDialogVisible = ref(false)
const folderLoading = ref(false)
const folderFormRef = ref(null)
const folderForm = reactive({ folderName: '' })
const folderRules = { folderName: [{ required: true, message: '请输入文件夹名称', trigger: 'blur' }] }

const showNewFolderDialog = () => {
  folderForm.folderName = ''
  folderDialogVisible.value = true
}

const submitNewFolder = () => {
  folderFormRef.value.validate(async (valid) => {
    if (!valid) return
    folderLoading.value = true
    try {
      await createFolder({ folderName: folderForm.folderName, parentId: currentParentId.value })
      ElMessage.success('文件夹创建成功')
      folderDialogVisible.value = false
      // 清除缓存，强制刷新数据
      fileStore.clearAllFileListCache()
      loadData(true)
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      folderLoading.value = false
    }
  })
}

// ===== 重命名 =====
const renameDialogVisible = ref(false)
const renameLoading = ref(false)
const renameFormRef = ref(null)
const renameForm = reactive({ id: null, newName: '' })
const renameRules = { newName: [{ required: true, message: '请输入新名称', trigger: 'blur' }] }

const handleRename = (row) => {
  renameForm.id = row.id
  renameForm.newName = row.originalName || row.fileName
  renameDialogVisible.value = true
}

const submitRename = () => {
  renameFormRef.value.validate(async (valid) => {
    if (!valid) return
    renameLoading.value = true
    try {
      await renameFile({ id: renameForm.id, newName: renameForm.newName })
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      // 清除缓存，强制刷新数据
      fileStore.clearAllFileListCache()
      loadData(true)
    } catch (error) {
      // 错误已在拦截器中处理
    } finally {
      renameLoading.value = false
    }
  })
}

// ===== 移动 =====
const moveDialogVisible = ref(false)
const moveLoading = ref(false)
const moveForm = reactive({ id: null, targetParentId: '' })
const folderTree = ref([])
const folderTreeProps = {
  label: (data) => data.originalName || data.fileName || '未命名文件夹',
  value: 'id',
  children: 'children',
  // 后端数据没有 hasChildren 字段，根据 fileType 判断
  // 文件夹类型允许懒加载（可能有子节点），文件类型为叶子节点
  isLeaf: (data) => data.fileType !== 'folder'
}

const handleMove = async (row) => {
  moveForm.id = row.id
  moveForm.targetParentId = ''
  // 在打开弹窗前就加载所有文件夹数据
  await loadAllFolderTree()
  moveDialogVisible.value = true
}

// 一次性加载所有文件夹（递归）
const loadAllFolderTree = async () => {
  try {
    // 使用 Pinia store 获取文件夹树（带缓存）
    const res = await fileStore.fetchFolderTree()
    const folderList = res.data?.list || []
    
    // 添加"根目录"选项
    folderTree.value = [
      {
        id: null,
        originalName: '根目录',
        fileName: '根目录',
        children: folderList
      }
    ]
  } catch (error) {
    // 出错时至少显示根目录选项
    folderTree.value = [
      {
        id: null,
        originalName: '根目录',
        fileName: '根目录',
        children: []
      }
    ]
  }
}

const submitMove = async () => {
  moveLoading.value = true
  try {
    await moveFile({ id: moveForm.id, targetParentId: moveForm.targetParentId || null })
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    // 清除缓存，强制刷新数据
    fileStore.clearAllFileListCache()
    loadData(true)
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    moveLoading.value = false
  }
}

// ===== 删除 =====
const handleDelete = (row) => {
  const name = row.originalName || row.fileName
  ElMessageBox.confirm(`确定删除「${name}」吗？此操作不可恢复`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteFile(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 错误已在拦截器中处理
    }
  }).catch(() => {})
}

// ===== 工具方法 =====
const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}

const formatTime = (datetime, format = 'full') => {
  if (!datetime) return format === 'full' ? '-' : ''
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  if (format === 'short') {
    return `${month}-${day} ${hours}:${minutes}`
  }
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// ===== 树形视图 =====
const treeProps = {
  label: (data) => data.originalName || data.fileName,
  children: 'children',
  isLeaf: (data) => !isFolder(data)
}
const expandedKeys = ref([])

const loadTreeChildren = async (node, resolve) => {
  if (node.level === 0) {
    // 根节点加载
    try {
      const res = await getChildFiles()
      const data = res.data?.list || []
      resolve(data)
    } catch (error) {
      resolve([])
    }
  } else {
    // 子节点加载
    try {
      const res = await getChildFiles(node.data.id)
      const data = res.data?.list || []
      resolve(data)
    } catch (error) {
      resolve([])
    }
  }
}

const handleNodeExpand = (data) => {
  if (data.id && !expandedKeys.value.includes(data.id)) {
    expandedKeys.value.push(data.id)
  }
}

const handleNodeCollapse = (data) => {
  const index = expandedKeys.value.indexOf(data.id)
  if (index > -1) {
    expandedKeys.value.splice(index, 1)
  }
}

// ===== 初始化 =====
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-container {
  padding: 20px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-left {
  display: flex;
  align-items: center;
}
.toolbar-right {
  display: flex;
  align-items: center;
}
.clickable {
  cursor: pointer;
  color: #409eff;
}
.file-name-cell {
  display: flex;
  align-items: center;
}
.file-name {
  cursor: pointer;
}
.file-name:hover {
  color: #409eff;
}
.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 树形视图样式 */
.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.tree-node-content {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.tree-node-id {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
  font-weight: 500;
}

.tree-node-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.tree-node-info {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

.tree-node-time {
  margin-left: 8px;
  color: #b0b0b0;
  font-size: 11px;
}

.tree-node-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .tree-node-actions {
  opacity: 1;
}

:deep(.el-tree-node__content) {
  height: 36px;
}

:deep(.el-tree-node__expand-icon) {
  color: #909399;
}

:deep(.el-tree-node__expand-icon.is-leaf) {
  color: transparent;
}
</style>
