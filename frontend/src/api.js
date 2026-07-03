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
    return normalizeDateTime(body.data)
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

function normalizeDateTime(value) {
  if (Array.isArray(value)) {
    return value.map(normalizeDateTime)
  }
  if (value && typeof value === 'object') {
    Object.keys(value).forEach((key) => {
      value[key] = normalizeDateTime(value[key])
    })
    return value
  }
  if (typeof value === 'string') {
    return formatDateTimeString(value)
  }
  return value
}

function formatDateTimeString(value) {
  const match = value.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?$/)
  if (match) {
    return `${match[1]} ${match[2]}`
  }
  const timeMatch = value.match(/^(\d{2}:\d{2}:\d{2})(?:\.\d+)?$/)
  if (timeMatch) {
    return timeMatch[1]
  }
  return value
}

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
