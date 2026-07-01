<template>
  <div class="auth-page">
    <el-card class="auth-panel login-panel">
      <h2>{{ role === 'ADMIN' ? '管理员登录' : '学生登录' }}</h2>

      <div class="role-cards">
        <button class="role-card" :class="{ active: role === 'STUDENT' }" type="button" @click="switchRole('STUDENT')">
          <strong>学生端</strong>
          <span>空间预约、我的预约、学习统计</span>
        </button>
        <button class="role-card" :class="{ active: role === 'ADMIN' }" type="button" @click="switchRole('ADMIN')">
          <strong>管理员端</strong>
          <span>资源管理、审批、运营统计</span>
        </button>
      </div>

      <el-form v-if="mode === 'login'" :model="form" label-position="top">
        <el-form-item label="登录方式">
          <el-segmented v-model="form.loginType" :options="loginOptions" />
        </el-form-item>
        <el-form-item :label="accountLabel">
          <el-input v-model="form.account" :placeholder="accountPlaceholder" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="submit">登录</el-button>
      </el-form>

      <el-form v-else :model="forgotForm" label-position="top">
        <el-form-item label="手机号" :error="phoneError">
          <el-input v-model="forgotForm.phone" placeholder="请输入注册手机号" @input="phoneError = ''" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="forgotForm.code" placeholder="请输入验证码" />
            <el-button @click="sendCode">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" :error="passwordError">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            show-password
            placeholder="至少8位，数字/字母/特殊符号至少两类"
            @input="validateForgotPassword"
          />
          <div class="form-tip">至少8位，数字、字母、特殊符号中至少包含两类。</div>
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="resetPassword">修改密码</el-button>
      </el-form>

      <el-divider />
      <div class="auth-links">
        <el-button v-if="role === 'STUDENT' && mode === 'login'" link @click="$router.push('/register')">注册学生账号</el-button>
        <el-button link @click="mode = mode === 'login' ? 'forgot' : 'login'">
          {{ mode === 'login' ? '忘记密码' : '返回登录' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const role = ref('STUDENT')
const mode = ref('login')
const phoneError = ref('')
const passwordError = ref('')
const form = reactive({ role: 'STUDENT', loginType: 'USERNAME', account: 'student', password: 'test123456' })
const forgotForm = reactive({ phone: '', code: '', newPassword: '' })

const loginOptions = computed(() => {
  const options = [
    { label: '用户名', value: 'USERNAME' },
    { label: '手机号', value: 'PHONE' }
  ]
  if (role.value === 'STUDENT') {
    options.splice(1, 0, { label: '学号', value: 'STUDENT_NO' })
  }
  return options
})

const accountLabel = computed(() => ({ USERNAME: '用户名', STUDENT_NO: '学号', PHONE: '手机号' }[form.loginType]))
const accountPlaceholder = computed(() => `请输入${accountLabel.value}`)

function switchRole(nextRole) {
  role.value = nextRole
  form.role = nextRole
  form.loginType = 'USERNAME'
  form.account = nextRole === 'ADMIN' ? 'admin' : 'student'
  form.password = 'test123456'
  mode.value = 'login'
}

async function submit() {
  const res = await api.post('/auth/login', { ...form, username: form.account, role: role.value })
  localStorage.setItem('token', res.token)
  localStorage.setItem('user', JSON.stringify(res.user))
  router.push(res.user.role === 'ADMIN' ? '/admin' : '/student')
}

function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

function validateForgotPassword() {
  passwordError.value = isStrongPassword(forgotForm.newPassword) ? '' : '密码至少8位，且需包含数字、字母、特殊符号中的至少两类'
}

function isStrongPassword(password) {
  if (!password || password.length < 8) return false
  let types = 0
  if (/\d/.test(password)) types++
  if (/[A-Za-z]/.test(password)) types++
  if (/[^A-Za-z0-9]/.test(password)) types++
  return types >= 2
}

async function sendCode() {
  if (!isValidPhone(forgotForm.phone)) {
    phoneError.value = '请输入正确手机号！'
    return
  }
  const message = await api.post('/auth/forgot-password/code', { phone: forgotForm.phone, role: role.value })
  ElMessage.success(message)
}

async function resetPassword() {
  if (!isValidPhone(forgotForm.phone)) {
    phoneError.value = '请输入正确手机号！'
    return
  }
  validateForgotPassword()
  if (passwordError.value) return
  await api.post('/auth/forgot-password/reset', { ...forgotForm, role: role.value })
  ElMessage.success('密码已修改')
  mode.value = 'login'
  form.password = ''
}
</script>

<style scoped>
.login-panel {
  width: min(560px, 100%);
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.role-card {
  display: grid;
  gap: 6px;
  min-height: 86px;
  padding: 14px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-md);
  background: #f8fbff;
  color: var(--es-text-secondary);
  text-align: left;
  cursor: pointer;
}

.role-card strong {
  color: var(--es-text-main);
  font-size: 16px;
}

.role-card span {
  font-size: 12px;
  line-height: 1.5;
}

.role-card.active {
  border-color: var(--es-primary);
  background: var(--es-primary-light);
  box-shadow: 0 8px 18px rgba(47, 143, 131, 0.14);
}

.code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  width: 100%;
}

.form-tip {
  margin-top: 6px;
  color: var(--es-text-secondary);
  font-size: 12px;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
</style>
