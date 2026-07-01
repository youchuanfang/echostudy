<template>
  <div class="es-page">
    <PageHeader page-key="adminDashboard" />
    <div class="metric-grid">
      <div class="metric-card"><h3>今日预约</h3><strong>{{ data.todayReservationCount || 0 }}</strong></div>
      <div class="metric-card clickable" @click="router.push('/admin/approvals')"><h3>待审批</h3><strong>{{ data.pendingApprovalCount || 0 }}</strong></div>
      <div class="metric-card"><h3>今日签到</h3><strong>{{ data.todaySignInCount || 0 }}</strong></div>
      <div class="metric-card clickable" @click="router.push('/admin/repairs')"><h3>今日报修</h3><strong>{{ data.todayRepairCount || 0 }}</strong></div>
      <div class="metric-card clickable" @click="router.push('/admin/repairs')"><h3>未处理报修</h3><strong>{{ data.pendingRepairCount || 0 }}</strong></div>
      <div class="metric-card"><h3>今日违规</h3><strong>{{ data.todayViolationCount || 0 }}</strong></div>
      <div class="metric-card"><h3>封禁用户</h3><strong>{{ data.bannedUserCount || 0 }}</strong></div>
      <div class="metric-card clickable" @click="router.push('/admin/statistics')"><h3>AI 成功率</h3><strong>{{ percent(data.aiSuccessRate) }}</strong></div>
    </div>
    <AppCard title="空间预约率">
      <el-table :data="data.roomRates || []">
        <el-table-column prop="roomName" label="空间" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="activeReservationCount" label="有效预约" />
        <el-table-column label="预约率"><template #default="{ row }">{{ percent(row.rate) }}</template></el-table-column>
      </el-table>
    </AppCard>
    <AppCard title="最新操作日志">
      <el-table :data="data.latestOperationLogs || []">
        <el-table-column prop="operationType" label="操作类型" />
        <el-table-column prop="operationContent" label="操作内容" min-width="220" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const data = ref({})
function percent(value) {
  return `${Math.round((Number(value) || 0) * 100)}%`
}
onMounted(async () => { data.value = await api.get('/admin/dashboard') })
</script>

<style scoped>
.metric-card.clickable {
  cursor: pointer;
}
</style>
