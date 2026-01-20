import axios from 'axios'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 添加token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误
// 后端返回格式：Result<T> {code, message, data}
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果code不是200，说明有错误
    if (res.code !== 200) {
      // token过期或无效，清除token并跳转到登录页
      if (res.code === 401 || res.code === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.href = '/login'
      }
      // 返回错误信息，格式与后端Result.fail()一致
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 成功时返回整个Result对象 {code: 200, message: "success", data: T}
    return res
  },
  (error) => {
    // 处理HTTP错误（网络错误、服务器错误等）
    if (error.response) {
      const { status, data } = error.response
      if (status === 401 || status === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.href = '/login'
      }
      // 如果后端返回了Result格式的错误，使用data.message
      // 否则使用HTTP状态码错误
      const errorMessage = data?.message || `请求失败: ${status}`
      return Promise.reject(new Error(errorMessage))
    }
    // 网络错误或其他错误
    return Promise.reject(new Error(error.message || '网络错误，请检查网络连接'))
  }
)

export default service
