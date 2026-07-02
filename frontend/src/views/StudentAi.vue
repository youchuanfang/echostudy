<template>
  <div class="es-page">
    <PageHeader page-key="aiTask" />
    <AppCard title="创建 AI 预约任务">
      <div class="form-grid">
        <el-form-item label="目标日期"><el-date-picker v-model="form.targetDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledReserveDate" /></el-form-item>
        <el-form-item label="开始时间"><el-time-select v-model="form.startTime" start="08:00" step="00:30" end="22:00" :min-time="startMinTime" placeholder="开始" /></el-form-item>
        <el-form-item label="结束时间"><el-time-select v-model="form.endTime" start="08:00" step="00:30" end="22:00" :min-time="endMinTime" placeholder="结束" /></el-form-item>
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
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const rooms = ref([])
const rows = ref([])
const form = reactive({ targetDate: new Date().toISOString().slice(0, 10), startTime: '08:00', endTime: '09:00', preferredRoomId: null, preferSocket: false, preferWindow: false, allowChangeRoom: true })
const nowMinutes = ref(minutesSinceMidnight(new Date()))
let nowTimer = null

const startMinTime = computed(() => (isToday(form.targetDate) ? minuteText(nowMinutes.value) : '00:00'))
const endMinTime = computed(() => minuteText(timeToMinutes(form.startTime) + 1))

function dateText(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function minutesSinceMidnight(date) {
  return date.getHours() * 60 + date.getMinutes()
}

function minuteText(minutes) {
  const normalized = Math.max(0, minutes)
  const hour = Math.floor(normalized / 60)
  const minute = normalized % 60
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
}

function timeToMinutes(value) {
  const [hour = 0, minute = 0] = String(value || '').split(':').map(Number)
  return hour * 60 + minute
}

function isToday(dateValue) {
  return dateValue === dateText(new Date())
}

function disabledReserveDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function refreshNow() {
  nowMinutes.value = minutesSinceMidnight(new Date())
}

function isStartTimeInvalid() {
  return isToday(form.targetDate) && timeToMinutes(form.startTime) < nowMinutes.value
}

function isEndTimeInvalid() {
  return timeToMinutes(form.endTime) <= timeToMinutes(form.startTime)
}

function nextStepTimeAfter(minutes) {
  const next = Math.ceil(minutes / 30) * 30
  return minuteText(next)
}

function ensureTimeSelection() {
  if (isStartTimeInvalid()) {
    form.startTime = nextStepTimeAfter(nowMinutes.value)
  }
  if (isEndTimeInvalid()) {
    form.endTime = nextStepTimeAfter(timeToMinutes(form.startTime) + 1)
  }
}

function validateTimeSelection() {
  refreshNow()
  if (form.targetDate < dateText(new Date())) {
    ElMessage.warning('不能创建过去日期的 AI 任务')
    return false
  }
  if (isStartTimeInvalid()) {
    ElMessage.warning('开始时间不能早于当前时间')
    return false
  }
  if (isEndTimeInvalid()) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }
  return true
}

async function load() {
  rooms.value = await api.get('/student/rooms')
  rows.value = await api.get('/student/ai-tasks/my')
}

async function create() {
  if (!validateTimeSelection()) {
    return
  }
  await api.post('/student/ai-tasks', form)
  ElMessage.success('AI 任务已创建')
  await load()
}

onMounted(async () => {
  refreshNow()
  nowTimer = window.setInterval(() => {
    refreshNow()
    ensureTimeSelection()
  }, 30000)
  ensureTimeSelection()
  await load()
})

onBeforeUnmount(() => {
  if (nowTimer) {
    window.clearInterval(nowTimer)
  }
})

watch([() => form.targetDate, nowMinutes], () => {
  ensureTimeSelection()
})

watch(() => form.startTime, () => {
  ensureTimeSelection()
})
</script>
