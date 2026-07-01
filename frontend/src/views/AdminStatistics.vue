<template>
  <div class="es-page">
    <PageHeader page-key="adminStatistics" />
    <div class="metric-grid">
      <div class="metric-card"><h3>空间数量</h3><strong>{{ overview.spaceCount || 0 }}</strong></div>
      <div class="metric-card"><h3>座位/工位</h3><strong>{{ overview.seatCount || 0 }}</strong></div>
      <div class="metric-card"><h3>预约总数</h3><strong>{{ overview.reservationTotal || 0 }}</strong></div>
      <div class="metric-card"><h3>完成预约</h3><strong>{{ overview.completedCount || 0 }}</strong></div>
      <div class="metric-card"><h3>违规总数</h3><strong>{{ overview.violationCount || 0 }}</strong></div>
      <div class="metric-card"><h3>报修总数</h3><strong>{{ overview.repairTotal || 0 }}</strong></div>
    </div>
    <AppCard title="空间使用">
      <el-table :data="spaces">
        <el-table-column prop="roomName" label="空间" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="reservationCount" label="预约数" />
        <el-table-column label="使用率"><template #default="{ row }">{{ Math.round((row.rate || 0) * 100) }}%</template></el-table-column>
      </el-table>
    </AppCard>
    <AppCard title="报修状态">
      <el-table :data="repairRows">
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="count" label="数量" />
      </el-table>
    </AppCard>
    <AppCard title="学习排行">
      <el-table :data="learningRank">
        <el-table-column prop="userName" label="学生" />
        <el-table-column prop="completedCount" label="完成次数" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import { repairStatusMap } from '../config/statusMap'

const overview = ref({})
const spaces = ref([])
const repairs = ref({})
const learningRank = ref([])
const repairRows = computed(() => Object.entries(repairs.value || {}).map(([status, count]) => ({ status: repairStatusMap[status] || status, count })))

onMounted(async () => {
  const [overviewData, spaceRows, repairData, rankRows] = await Promise.all([
    api.get('/admin/statistics/overview'),
    api.get('/admin/statistics/spaces'),
    api.get('/admin/statistics/repairs'),
    api.get('/admin/statistics/learning-rank')
  ])
  overview.value = overviewData
  spaces.value = spaceRows
  repairs.value = repairData
  learningRank.value = rankRows
})
</script>
