import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code !== 200) {
      const message = toUserMessage(body.message, '操作失败，请检查填写内容后重试')
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    const fallback = status ? '操作失败，请稍后重试' : '网络请求失败，请检查网络后重试'
    ElMessage.error(toUserMessage(error.response?.data?.message, fallback))
    return Promise.reject(error)
  }
)

function toUserMessage(message, fallback) {
  if (!message || isTechnicalMessage(message)) {
    return fallback
  }
  return message
}

function isTechnicalMessage(message) {
  const value = String(message)
  return [
    '###',
    'Exception',
    'SQLException',
    'SQLServerException',
    'DataIntegrityViolation',
    'DuplicateKey',
    'com.',
    'org.springframework',
    'java.',
    'ConstraintViolation',
    'Violation of',
    'INSERT INTO',
    'UPDATE ',
    'DELETE FROM',
    'Cannot',
    'undefined'
  ].some((pattern) => value.includes(pattern))
}

export default api
