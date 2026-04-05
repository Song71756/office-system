import request from '@/utils/request'

// 获取部门树形结构
export function getDepartmentTree() {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}

// 获取部门扁平列表
export function getDepartmentList() {
  return request({
    url: '/department/list',
    method: 'get'
  })
}

// 获取部门详情
export function getDepartmentById(id) {
  return request({
    url: `/department/${id}`,
    method: 'get'
  })
}

// 新增或更新部门
export function saveDepartment(data) {
  return request({
    url: '/department/save',
    method: 'post',
    data
  })
}

// 删除部门
export function deleteDepartment(id) {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}
