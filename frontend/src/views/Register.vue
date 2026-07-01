<template>
  <div class="auth-page">
    <el-card class="auth-panel">
      <h2>学生注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="至少8位，数字/字母/特殊符号至少两类"
            @input="formRef?.validateField('password')"
          />
          <div class="form-tip">至少8位，数字、字母、特殊符号中至少包含两类。</div>
        </el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="学号" prop="studentNo"><el-input v-model="form.studentNo" maxlength="10" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" maxlength="11" /></el-form-item>
        <el-button type="primary" style="width: 100%" @click="submit">注册</el-button>
      </el-form>
      <el-divider />
      <el-button link @click="$router.push('/login')">返回登录</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const formRef = ref(null)
const form = reactive({ username: '', password: '', realName: '', studentNo: '', phone: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentNo: [{ validator: validateStudentNo, trigger: ['blur', 'change'] }],
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

function validateStudentNo(rule, value, callback) {
  if (!/^\d{10}$/.test(value || '')) {
    callback(new Error('学号必须是10位数字'))
    return
  }
  callback()
}

function validatePhone(rule, value, callback) {
  if (!/^1[3-9]\d{9}$/.test(value || '')) {
    callback(new Error('请输入正确手机号！'))
    return
  }
  callback()
}

async function submit() {
  await formRef.value.validate()
  await api.post('/auth/register', form)
  ElMessage.success('注册成功')
  router.push('/login')
}
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  color: var(--es-text-secondary);
  font-size: 12px;
}
</style>
