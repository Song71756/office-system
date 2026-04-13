import request from '@/utils/request'

// 分页查询公文列表
export function getDocumentPage(params) {
  return request({
    url: '/document/page',
    method: 'get',
    params
  })
}

// 分页查询我的公文
export function getMyDocumentPages(params) {
  return request({
    url: '/document/mypage',
    method: 'get',
    params
  })
}

// 获取公文详情
export function getDocumentById(id) {
  return request({
    url: `/document/${id}`,
    method: 'get'
  })
}

// 起草或修改公文
export function saveDocument(data) {
  return request({
    url: '/document/save',
    method: 'post',
    data
  })
}

// 起草或修改个人公文
export function saveMyDocument(data) {
  return request({
    url: '/document/save/myself',
    method: 'post',
    data
  })
}

// 提交公文审核
export function submitDocument(id) {
  return request({
    url: `/document/submit/${id}`,
    method: 'post'
  })
}

// 审批公文
export function approveDocument(id, data) {
  return request({
    url: `/document/approve/${id}`,
    method: 'post',
    data
  })
}

// 删除公文
export function deleteDocument(id) {
  return request({
    url: `/document/delete/${id}`,
    method: 'delete'
  })
}

// 删除个人公文
export function deleteMyDocument(id) {
  return request({
    url: `/document/delete/myself/${id}`,
    method: 'delete'
  })
}
