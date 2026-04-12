import request from '@/utils/request'

// 获取当前登录用户信息
export function getCurrentUser() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

// 用户退出登录
export function logout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

// 分页查询用户列表
export function getUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 获取用户全量列表
export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

// 管理员创建用户
export function createUser(data) {
  return request({
    url: '/user/create',
    method: 'post',
    data
  })
}

// 更新用户信息
export function updateUser(data) {
  return request({
    url: '/user/edit',
    method: 'put',
    data
  })
}

//更新个人信息
export function updateMyself(data) {
  return request({
    url: '/user/editMyself',
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete'
  })
}

// 为用户分配角色
export function assignRoles(userId, roleIds) {
  return request({
    url: `/user/assignRoles/${userId}`,
    method: 'post',
    data: roleIds
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/user/updatePassword',
    method: 'post',
    data
  })
}
