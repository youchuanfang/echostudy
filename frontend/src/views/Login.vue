<template>
  <div class="auth-page">
    <el-card class="auth-panel">
      <h2>EchoStudy 登录</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-button type="primary" style="width: 100%" @click="submit">登录</el-button>
      </el-form>
      <el-divider />
      <el-button link @click="$router.push('/register')">注册学生账号</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const form = reactive({ username: 'student', password: '123456' })

async function submit() {
  const res = await api.post('/auth/login', form)
  localStorage.setItem('token', res.token)
  localStorage.setItem('user', JSON.stringify(res.user))
  router.push(res.user.role === 'ADMIN' ? '/admin' : '/student')
}
</script>
