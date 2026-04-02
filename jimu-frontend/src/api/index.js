import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const api = axios.create({
  baseURL: '',
  timeout: 15000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['X-Access-Token'] = token
  }
  return config
})

api.interceptors.response.use(
  res => {
    if (res.data.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    }
    if (res.data.code === 403) {
      ElMessage.error('没有权限访问该资源')
      return Promise.reject(new Error('无权限'))
    }
    return res.data
  },
  err => {
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(err)
  }
)

export default api
