import request from '@/utils/request'

// 分页查询我的日程
export function getMySchedulePage(params) {
  return request({
    url: '/schedule/myPage',
    method: 'get',
    params
  })
}

// 分页查询全部日程
export function getAllSchedulePage(params) {
  return request({
    url: '/schedule/page',
    method: 'get',
    params
  })
}

// 获取日程详情
export function getScheduleById(id) {
  return request({
    url: `/schedule/${id}`,
    method: 'get'
  })
}

// 新增或编辑日程
export function saveSchedule(data) {
  return request({
    url: '/schedule/save',
    method: 'post',
    data
  })
}


// 新增或编辑个人日程
export function saveMySchedule(data) {
  return request({
    url: '/schedule/save/myself',
    method: 'post',
    data
  })
}

// 删除日程
export function deleteSchedule(id) {
  return request({
    url: `/schedule/delete/${id}`,
    method: 'delete'
  })
}

// 删除个人日程
export function deleteMySchedule(id) {
  return request({
    url: `/schedule/delete/myself/${id}`,
    method: 'delete'
  })
}
