<template>
  <div class="es-page">
    <PageHeader page-key="userManage" />
    <AppCard title="用户列表">
      <el-table :data="rows">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column label="角色"><template #default="{ row }">{{ row.role === 'ADMIN' ? '管理员' : '学生' }}</template></el-table-column>
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="violationCount" label="违规" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" @click="post(row.id, 'ban')">封禁</el-button>
            <el-button size="small" @click="post(row.id, 'unban')">解禁</el-button>
            <el-button size="small" @click="post(row.id, 'disable')">禁用</el-button>
            <el-button size="small" @click="post(row.id, 'enable')">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
async function load() { rows.value = await api.get('/admin/users') }
async function post(id, action) { await api.post(`/admin/users/${id}/${action}`); await load() }
onMounted(load)
</script>
