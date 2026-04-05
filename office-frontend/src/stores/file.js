import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMyFiles, getChildFiles } from '@/api/file'

export const useFileStore = defineStore('file', () => {
  // 缓存数据
  const fileListCache = ref(new Map()) // key: `${id}-${page}-${size}`
  const folderTreeCache = ref(null)
  const lastFetchTime = ref(new Map()) // 记录每次获取的时间
  
  // 缓存有效期（5分钟）
  const CACHE_DURATION = 5 * 60 * 1000
  
  // 生成缓存 key
  const generateCacheKey = (folderId, page, size) => {
    return `${folderId || 'root'}-${page}-${size}`
  }
  
  // 检查缓存是否有效
  const isCacheValid = (key) => {
    const lastTime = lastFetchTime.value.get(key)
    if (!lastTime) return false
    return Date.now() - lastTime < CACHE_DURATION
  }
  
  // 获取文件列表（带缓存）
  const fetchFileList = async (params = {}, forceRefresh = false) => {
    const { parentId = null, pageNum = 1, pageSize = 20 } = params
    const cacheKey = generateCacheKey(parentId, pageNum, pageSize)
    
    // 如果缓存有效且不强制刷新，直接返回缓存数据
    if (!forceRefresh && isCacheValid(cacheKey) && fileListCache.value.has(cacheKey)) {
      console.log('使用缓存数据:', cacheKey)
      return fileListCache.value.get(cacheKey)
    }
    
    // 从 API 获取新数据
    console.log('从 API 获取数据:', cacheKey)
    const response = await getMyFiles(params)
    
    // 更新缓存
    fileListCache.value.set(cacheKey, response)
    lastFetchTime.value.set(cacheKey, Date.now())
    
    return response
  }
  
  // 获取文件夹树（带缓存）
  const fetchFolderTree = async (forceRefresh = false) => {
    const cacheKey = 'folderTree'
    
    // 如果缓存有效且不强制刷新，直接返回缓存数据
    if (!forceRefresh && isCacheValid(cacheKey) && folderTreeCache.value) {
      console.log('使用缓存的文件夹树')
      // 返回统一格式
      return { data: { list: folderTreeCache.value } }
    }
    
    // 递归加载所有文件夹
    const loadAllFolders = async (parentId = null) => {
      const res = await getChildFiles(parentId)
      const list = res.data?.list || []
      // 保留文件夹类型的项（fileType 为 'folder' 或 null，且 filePath 为 null）
      const folders = list.filter(item => 
        item.fileType === 'folder' || 
        (item.fileType === null && item.filePath === null)
      )
      
      // 递归加载子文件夹
      for (const folder of folders) {
        folder.children = await loadAllFolders(folder.id)
      }
      
      return folders
    }
    
    // 从 API 获取新数据
    console.log('从 API 获取文件夹树')
    const folders = await loadAllFolders()
    
    // 更新缓存
    folderTreeCache.value = folders
    lastFetchTime.value.set(cacheKey, Date.now())
    
    // 返回统一格式
    return { data: { list: folders } }
  }
  
  // 清除特定缓存
  const clearCache = (folderId = null, page = 1, size = 20) => {
    const cacheKey = generateCacheKey(folderId, page, size)
    fileListCache.value.delete(cacheKey)
    lastFetchTime.value.delete(cacheKey)
  }
  
  // 清除所有文件列表缓存
  const clearAllFileListCache = () => {
    fileListCache.value.clear()
    // 只删除文件列表相关的缓存时间记录
    for (const key of lastFetchTime.value.keys()) {
      if (key !== 'folderTree') {
        lastFetchTime.value.delete(key)
      }
    }
  }
  
  // 清除文件夹树缓存
  const clearFolderTreeCache = () => {
    folderTreeCache.value = null
    lastFetchTime.value.delete('folderTree')
  }
  
  // 清除所有缓存
  const clearAllCache = () => {
    fileListCache.value.clear()
    folderTreeCache.value = null
    lastFetchTime.value.clear()
  }
  
  // 获取当前文件夹的缓存数据
  const getCurrentFolderCache = (folderId, page = 1, size = 20) => {
    const cacheKey = generateCacheKey(folderId, page, size)
    return fileListCache.value.get(cacheKey) || null
  }
  
  // 检查是否有缓存
  const hasCache = (folderId, page = 1, size = 20) => {
    const cacheKey = generateCacheKey(folderId, page, size)
    return isCacheValid(cacheKey) && fileListCache.value.has(cacheKey)
  }
  
  return {
    // 状态
    fileListCache,
    folderTreeCache,
    
    // 方法
    fetchFileList,
    fetchFolderTree,
    clearCache,
    clearAllFileListCache,
    clearFolderTreeCache,
    clearAllCache,
    getCurrentFolderCache,
    hasCache,
    isCacheValid
  }
})
