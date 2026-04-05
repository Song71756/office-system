import request from '@/utils/request'

// 打卡（自动判断签到/签退）
export function punch(data) {
  return request({
    url: '/attendance/punch',
    method: 'post',
    data
  })
}

// 分页获取我的考勤历史
export function getMyHistory(params) {
  return request({
    url: '/attendance/myHistory',
    method: 'get',
    params
  })
}

// 获取我在某年某月的考勤统计
export function getMyMonthStats(params) {
  return request({
    url: '/attendance/myMonthStats',
    method: 'get',
    params
  })
}
