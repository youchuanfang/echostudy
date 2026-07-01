<template>
  <div class="es-page">
    <PageHeader page-key="aiTaskManage" />
    <AppCard title="全部 AI 任务">
      <template #extra><el-button text type="primary" @click="load">刷新</el-button></template>
      <el-table :data="rows">
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="targetDate" label="日期" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="endTime" label="结束" />
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="resultReservationId" label="预约ID" />
        <el-table-column prop="failReason" label="失败原因" />
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
async function load() { rows.value = await api.get('/admin/ai-tasks') }
onMounted(load)
</script>
