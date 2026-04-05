import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，统一配置基础路径和超时时间
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：在每个请求头中自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理后端返回的 Result 结构
request.interceptors.response.use(
  (response) => {
    // 如果是文件下载请求（responseType为blob），直接返回原始响应
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const res = response.data
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }
    // 后端 Result 中 code !== 200 代表业务异常
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    // 处理 HTTP 层面的错误（如 401 未授权）
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request