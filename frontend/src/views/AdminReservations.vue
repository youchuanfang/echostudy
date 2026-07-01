<template>
  <div class="es-page">
    <PageHeader page-key="reservationManage" />
    <AppCard title="全部预约记录">
      <el-table :data="rows">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="roomName" label="空间" min-width="140" />
        <el-table-column label="预约单位" width="110">
          <template #default="{ row }">{{ row.seatNo || '整间空间' }}</template>
        </el-table-column>
        <el-table-column prop="reserveDate" label="日期" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="endTime" label="结束" />
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column label="来源"><template #default="{ row }">{{ sourceText(row.source) }}</template></el-table-column>
        <el-table-column label="操作" width="180"><template #default="{ row }"><el-button size="small" @click="post(row.id, 'cancel')">取消</el-button><el-button size="small" @click="post(row.id, 'finish')">结束</el-button></template></el-table-column>
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
async function load() { rows.value = await api.get('/admin/reservations') }
async function post(id, action) { await api.post(`/admin/reservations/${id}/${action}`); await load() }
function sourceText(source) {
  return { ONLINE: '学生线上预约', OFFLINE_ADMIN: '管理员代预约', AI: 'AI 自动预约' }[source] || source || '-'
}
onMounted(load)
</script>
