import request from '@/utils/request'

// 获取所有角色列表
export function getRoleList() {
  return request({
    url: '/role/list',
    method: 'get'
  })
}

// 获取角色详情
export function getRoleById(id) {
  return request({
    url: `/role/${id}`,
    method: 'get'
  })
}

// 新增或更新角色
export function saveRole(data) {
  return request({
    url: '/role/save',
    method: 'post',
    data
  })
}

// 为角色分配权限
export function assignPermissions(roleId, permissionIds) {
  return request({
    url: `/role/assignPermissions/${roleId}`,
    method: 'post',
    data: permissionIds
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: `/role/delete/${id}`,
    method: 'delete'
  })
}
