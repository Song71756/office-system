import request from '@/utils/request'

// 获取所有权限列表
export function getPermissionList() {
  return request({
    url: '/permission/list',
    method: 'get'
  })
}

// 新增或更新权限
export function savePermission(data) {
  return request({
    url: '/permission/save',
    method: 'post',
    data
  })
}

// 删除权限
export function deletePermission(id) {
  return request({
    url: `/permission/delete/${id}`,
    method: 'delete'
  })
}
