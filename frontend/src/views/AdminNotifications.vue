<template>
  <div class="es-page">
    <PageHeader page-key="adminNotifications" />

    <AppCard title="消息筛选">
      <div class="form-grid">
        <el-form-item label="用户ID"><el-input v-model="filters.userId" /></el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="filters.type" clearable>
            <el-option v-for="type in notificationTypes" :key="type" :label="notificationTypeMap[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态">
          <el-select v-model="filters.readStatus" clearable>
            <el-option label="未读" :value="false" />
            <el-option label="已读" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词"><el-input v-model="filters.keyword" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="load">搜索</el-button></div>
      </div>
    </AppCard>

    <AppCard title="全站消息">
      <el-table :data="rows">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" min-width="130"><template #default="{ row }">{{ row.realName || row.username || row.userId }}</template></el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="120"><template #default="{ row }">{{ notificationTypeMap[row.type] || row.type }}</template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.readStatus ? '已读' : '未读' }}</template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import { notificationTypeMap } from '../config/statusMap'

const notificationTypes = ['RESERVATION', 'APPROVAL', 'VIOLATION', 'BAN', 'AI_TASK', 'REPAIR', 'ANNOUNCEMENT', 'SYSTEM']
const rows = ref([])
const filters = reactive({ userId: '', type: '', readStatus: '', keyword: '' })

async function load() {
  rows.value = await api.get('/admin/notifications', {
    params: {
      userId: filters.userId || undefined,
      type: filters.type || undefined,
      readStatus: filters.readStatus === '' ? undefined : filters.readStatus,
      keyword: filters.keyword || undefined
    }
  })
}

onMounted(load)
</script>
