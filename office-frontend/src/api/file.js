import request from '@/utils/request'

// 分页获取我的文件列表（支持层级）
export function getMyFiles(params) {
  return request({
    url: '/file/myFiles',
    method: 'get',
    params
  })
}

// 获取指定文件夹下的子文件（用于树形懒加载）
export function getChildFiles(parentId) {
  const params = { pageSize: 1000 }
  // 始终传递 parentId 参数，null 表示根目录
  params.parentId = parentId
  return request({
    url: '/file/myFiles',
    method: 'get',
    params
  })
}

// 上传办公文件
export function uploadOffice(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload/office',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 创建文件夹
export function createFolder(data) {
  return request({
    url: '/file/folder',
    method: 'post',
    data
  })
}

// 重命名文件/文件夹
export function renameFile(data) {
  return request({
    url: '/file/rename',
    method: 'put',
    data
  })
}

// 移动文件
export function moveFile(data) {
  return request({
    url: '/file/move',
    method: 'put',
    data
  })
}

// 删除文件(根据id删除文件)
export function deleteFile(id) {
  return request({
    url: `/file/delete/${id}`,
    method: 'delete'
  })
}


// 删除文件(根据新文件名删除文件)
export function deleteFileByNewFileName(fileName) {
  return request({
    url: `/file/delete/newFileName`,
    method: 'delete',
    params: { newFileName: fileName }
  })
}

// 下载文件（返回下载地址）
export function getDownloadUrl(id) {
  return `/api/file/download/${id}`
}

export function downloadFile(id) {
  return request({
    url: `/file/download/${id}`,
    method: 'get',
    responseType: 'blob' // 重要：告诉 axios 以 Blob 格式处理响应数据
  })
}
