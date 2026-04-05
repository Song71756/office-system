import request from '@/utils/request'

// 分页查询公告列表
export function getNoticePage(params) {
  return request({
    url: '/notice/page',
    method: 'get',
    params
  })
}

// 获取公告详情
export function getNoticeById(id) {
  return request({
    url: `/notice/${id}`,
    method: 'get'
  })
}

// 新增或编辑公告
export function saveNotice(data) {
  return request({
    url: '/notice/save',
    method: 'post',
    data
  })
}

// 删除公告
export function deleteNotice(id) {
  return request({
    url: `/notice/delete/${id}`,
    method: 'delete'
  })
}
