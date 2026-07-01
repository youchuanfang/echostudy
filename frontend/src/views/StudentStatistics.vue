<template>
  <div class="es-page">
    <PageHeader page-key="studentStatistics" />
    <div class="metric-grid">
      <div class="metric-card"><h3>今日学习</h3><strong>{{ minutes(data.todayMinutes) }}</strong></div>
      <div class="metric-card"><h3>本周学习</h3><strong>{{ minutes(data.weekMinutes) }}</strong></div>
      <div class="metric-card"><h3>本月学习</h3><strong>{{ minutes(data.monthMinutes) }}</strong></div>
      <div class="metric-card"><h3>累计学习</h3><strong>{{ minutes(data.totalMinutes) }}</strong></div>
      <div class="metric-card"><h3>预约次数</h3><strong>{{ data.reservationCount || 0 }}</strong></div>
      <div class="metric-card"><h3>违规次数</h3><strong>{{ data.violationCount || 0 }}</strong></div>
    </div>
    <AppCard title="最近学习记录">
      <el-table :data="data.recentRecords || []">
        <el-table-column prop="reserveDate" label="日期" />
        <el-table-column prop="roomName" label="空间" />
        <el-table-column label="时间段"><template #default="{ row }">{{ String(row.startTime).slice(0,5) }} - {{ String(row.endTime).slice(0,5) }}</template></el-table-column>
        <el-table-column label="有效时长"><template #default="{ row }">{{ minutes(row.minutes) }}</template></el-table-column>
      </el-table>
    </AppCard>
    <AppCard title="常用空间">
      <el-table :data="data.commonSpaces || []">
        <el-table-column prop="roomName" label="空间" />
        <el-table-column prop="count" label="次数" />
      </el-table>
    </AppCard>
    <AppCard title="常用时间段">
      <el-table :data="data.commonTimeSlots || []">
        <el-table-column prop="timeSlot" label="时间段" />
        <el-table-column prop="count" label="次数" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'

const data = ref({})
function minutes(value) {
  const total = Number(value || 0)
  return `${Math.floor(total / 60)}h ${total % 60}m`
}
onMounted(async () => { data.value = await api.get('/student/statistics/learning') })
</script>
