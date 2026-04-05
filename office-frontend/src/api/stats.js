import request from '@/utils/request'

// 获取首页看板统计数据
export function getDashboard() {
  return request({
    url: '/stats/dashboard',
    method: 'get'
  })
}
