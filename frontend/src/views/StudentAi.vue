<template>
  <div class="es-page">
    <PageHeader page-key="aiTask" />
    <AppCard title="创建 AI 预约任务">
      <div class="form-grid">
        <el-form-item label="目标日期"><el-date-picker v-model="form.targetDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="开始时间"><el-time-select v-model="form.startTime" start="08:00" step="00:30" end="22:00" placeholder="开始" /></el-form-item>
        <el-form-item label="结束时间"><el-time-select v-model="form.endTime" start="08:00" step="00:30" end="22:00" placeholder="结束" /></el-form-item>
        <el-form-item label="偏好自习室"><el-select v-model="form.preferredRoomId" clearable placeholder="偏好自习室（可不选）"><el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="优先插座"><el-checkbox v-model="form.preferSocket">优先选择有插座的座位</el-checkbox></el-form-item>
        <el-form-item label="优先靠窗"><el-checkbox v-model="form.preferWindow">优先选择靠窗的座位</el-checkbox></el-form-item>
        <el-form-item label="允许换房间"><el-checkbox v-model="form.allowChangeRoom">允许更换其他自习室</el-checkbox></el-form-item>
        <div class="form-action"><el-button type="primary" @click="create">创建任务</el-button></div>
      </div>
    </AppCard>
    <AppCard title="我的 AI 任务列表">
      <template #extra><el-button text type="primary" @click="load">刷新</el-button></template>
      <el-table :data="rows">
        <el-table-column prop="targetDate" label="日期" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="endTime" label="结束" />
        <el-table-column label="插座偏好"><template #default="{ row }"><span class="bool-icon" :class="row.preferSocket ? 'yes' : 'no'">{{ row.preferSocket ? '✓' : '×' }}</span></template></el-table-column>
        <el-table-column label="靠窗偏好"><template #default="{ row }"><span class="bool-icon" :class="row.preferWindow ? 'yes' : 'no'">{{ row.preferWindow ? '✓' : '×' }}</span></template></el-table-column>
        <el-table-column label="允许换房间"><template #default="{ row }"><span class="bool-icon" :class="row.allowChangeRoom ? 'yes' : 'no'">{{ row.allowChangeRoom ? '✓' : '×' }}</span></template></el-table-column>
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="resultReservationId" label="预约ID" />
        <el-table-column prop="failReason" label="失败原因" />
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const rooms = ref([])
const rows = ref([])
const form = reactive({ targetDate: new Date().toISOString().slice(0, 10), startTime: '08:00', endTime: '09:00', preferredRoomId: null, preferSocket: false, preferWindow: false, allowChangeRoom: true })

async function load() {
  rooms.value = await api.get('/student/rooms')
  rows.value = await api.get('/student/ai-tasks/my')
}

async function create() {
  await api.post('/student/ai-tasks', form)
  ElMessage.success('AI 任务已创建')
  await load()
}

onMounted(load)
</script>
