<template>
  <div class="es-page">
    <PageHeader page-key="adminOperationLogs" />

    <AppCard title="日志筛选">
      <div class="form-grid">
        <el-form-item label="操作人ID"><el-input v-model="filters.adminId" /></el-form-item>
        <el-form-item label="操作类型"><el-input v-model="filters.operationType" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="filters.keyword" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="load">搜索</el-button></div>
      </div>
    </AppCard>

    <AppCard title="操作日志">
      <el-table :data="rows">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="adminName" label="操作人" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" min-width="140" />
        <el-table-column prop="operationContent" label="操作内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="targetType" label="目标类型" width="130" />
        <el-table-column prop="targetId" label="目标编号" width="110" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'

const rows = ref([])
const filters = reactive({ adminId: '', operationType: '', startDate: '', endDate: '', keyword: '' })

async function load() {
  rows.value = await api.get('/admin/operation-logs', {
    params: {
      adminId: filters.adminId || undefined,
      operationType: filters.operationType || undefined,
      startDate: filters.startDate || undefined,
      endDate: filters.endDate || undefined,
      keyword: filters.keyword || undefined
    }
  })
}

onMounted(load)
</script>
