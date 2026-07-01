<template>
  <div class="es-page">
    <PageHeader page-key="violationManage" />
    <AppCard title="全部违规记录">
      <el-table :data="rows">
        <el-table-column prop="createTime" label="时间" />
        <el-table-column prop="userId" label="用户ID" />
        <el-table-column prop="reservationId" label="预约ID" />
        <el-table-column label="类型"><template #default="{ row }">{{ violationTypeText(row.violationType) }}</template></el-table-column>
        <el-table-column prop="reason" label="原因" />
        <el-table-column prop="violationCountSnapshot" label="累计" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import { violationTypeText } from '../config/statusMap'
const rows = ref([])
onMounted(async () => { rows.value = await api.get('/admin/violations') })
</script>
