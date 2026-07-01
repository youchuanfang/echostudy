<template>
  <div class="es-page">
    <PageHeader page-key="adminRegister" />

    <AppCard v-if="!canRegisterAdmin" title="注册新管理员">
      <el-empty description="不好意思，您无此权限" />
    </AppCard>

    <AppCard v-else title="注册新管理员">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="admin-register-grid">
          <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="至少8位，数字/字母/特殊符号至少两类"
              @input="formRef?.validateField('password')"
            />
          </el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
          <el-form-item label="允许注册新管理员">
            <el-switch v-model="form.canRegisterAdmin" active-text="允许" inactive-text="不允许" />
          </el-form-item>
        </div>
        <div class="form-action-row">
          <el-button type="primary" @click="submit">注册管理员</el-button>
        </div>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'

const formRef = ref(null)
const currentUser = ref(JSON.parse(localStorage.getItem('user') || 'null'))
const user = computed(() => currentUser.value)
const canRegisterAdmin = computed(() => Boolean(user.value?.canRegisterAdmin))
const form = reactive({ username: '', password: '', realName: '', phone: '', canRegisterAdmin: false })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  phone: [{ validator: validatePhone, trigger: ['blur', 'change'] }]
}

function isStrongPassword(password) {
  if (!password || password.length < 8) return false
  let types = 0
  if (/\d/.test(password)) types++
  if (/[A-Za-z]/.test(password)) types++
  if (/[^A-Za-z0-9]/.test(password)) types++
  return types >= 2
}

function validatePassword(rule, value, callback) {
  if (!isStrongPassword(value)) {
    callback(new Error('密码至少8位，且需包含数字、字母、特殊符号中的至少两类'))
    return
  }
  callback()
}

function validatePhone(rule, value, callback) {
  if (!value) {
    callback()
    return
  }
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确手机号！'))
    return
  }
  callback()
}

async function submit() {
  if (!canRegisterAdmin.value) {
    ElMessage.error('不好意思，您无此权限')
    return
  }
  await formRef.value.validate()
  await api.post('/auth/admin/register', form)
  ElMessage.success('管理员注册成功')
  Object.assign(form, { username: '', password: '', realName: '', phone: '', canRegisterAdmin: false })
  formRef.value.clearValidate()
}

onMounted(async () => {
  const freshUser = await api.get('/auth/me')
  currentUser.value = freshUser
  localStorage.setItem('user', JSON.stringify(freshUser))
})
</script>

<style scoped>
.admin-register-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 18px 24px;
  align-items: start;
}

.admin-register-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.admin-register-grid :deep(.el-input),
.admin-register-grid :deep(.el-select),
.admin-register-grid :deep(.el-input-number) {
  width: 100%;
}

.form-action-row {
  margin-top: 22px;
}

@media (max-width: 1200px) {
  .admin-register-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 720px) {
  .admin-register-grid {
    grid-template-columns: 1fr;
  }
}
</style>
