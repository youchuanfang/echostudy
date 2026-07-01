<template>
  <div class="auth-page">
    <el-card class="auth-panel">
      <h2>学生注册</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-button type="primary" style="width: 100%" @click="submit">注册</el-button>
      </el-form>
      <el-divider />
      <el-button link @click="$router.push('/login')">返回登录</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const form = reactive({ username: '', password: '', realName: '', studentNo: '', phone: '' })

async function submit() {
  await api.post('/auth/register', form)
  ElMessage.success('注册成功')
  router.push('/login')
}
</script>
