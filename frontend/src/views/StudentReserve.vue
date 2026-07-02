<template>
  <div class="es-page">
    <PageHeader page-key="spaceReserve" />

    <AppCard title="楼栋分类">
      <div class="building-cards">
        <button
          class="building-card"
          :class="{ active: filters.building === 'ALL' }"
          type="button"
          @click="selectBuilding('ALL')"
        >
          全部
        </button>
        <button
          v-for="building in buildings"
          :key="building"
          class="building-card"
          :class="{ active: filters.building === building }"
          type="button"
          @click="selectBuilding(building)"
        >
          {{ building }}
        </button>
      </div>
    </AppCard>

    <AppCard title="筛选条件">
      <div class="form-grid">
        <el-form-item label="空间类型">
          <el-select v-model="filters.spaceType" clearable placeholder="全部类型">
            <el-option label="全部" value="" />
            <el-option v-for="type in spaceTypes" :key="type" :label="spaceTypeMap[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标日期"><el-date-picker v-model="form.reserveDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledReserveDate" /></el-form-item>
        <el-form-item label="开始时间">
          <el-select v-model="form.startTime"><el-option v-for="n in nodes" :key="n.id" :label="timeText(n.timeValue)" :value="n.timeValue" :disabled="isStartTimeDisabled(n.timeValue)" /></el-select>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-select v-model="form.endTime"><el-option v-for="n in nodes" :key="n.id" :label="timeText(n.timeValue)" :value="n.timeValue" :disabled="isEndTimeDisabled(n.timeValue)" /></el-select>
        </el-form-item>
        <el-form-item label="只看开放空间"><el-switch v-model="filters.openOnly" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="loadSpaces">搜索</el-button></div>
      </div>
    </AppCard>

    <AppCard title="空间资源列表">
      <div class="space-list">
        <div
          v-for="space in filteredSpaces"
          :key="space.id"
          class="space-item"
          :class="{ active: form.roomId === space.id }"
        >
          <div class="space-item__main">
            <div class="space-item__title">
              <strong>{{ space.name }}</strong>
              <StatusTag :value="space.spaceType" />
              <span class="approval-pill">{{ space.needApproval ? '需要审批' : '无需审批' }}</span>
            </div>
            <div class="space-item__meta">
              <span>{{ [space.building, space.floorNo, space.locationDesc].filter(Boolean).join(' / ') || '暂无位置' }}</span>
              <span>容量 {{ space.capacity || 0 }}</span>
              <span>{{ timeText(space.openTime) }} - {{ timeText(space.closeTime) }}</span>
            </div>
            <p>{{ space.usageNotice || space.description || '暂无使用须知' }}</p>
          </div>
          <el-button type="primary" plain @click="toggleSpace(space)">
            {{ form.roomId === space.id ? '收起' : isSeatBased(space) ? '选择座位/工位' : '预约该空间' }}
          </el-button>
          <div v-if="form.roomId === space.id && !isSeatBased(space)" class="space-item__detail">
            <div class="room-reserve-panel">
              <div>
                <strong>{{ space.name }}</strong>
                <p>{{ space.usageNotice || space.description || '该空间按整间预约，无需选择座位/工位。' }}</p>
              </div>
              <div class="panel-actions">
                <el-button @click="collapseSpace">收起</el-button>
                <el-button type="primary" @click="reserveRoom">提交预约</el-button>
              </div>
            </div>
          </div>
          <div v-if="form.roomId === space.id && isSeatBased(space) && layout.roomId" class="space-item__detail">
            <div class="inline-seat-layout">
              <SeatLegend />
              <div class="seat-grid" :style="{ gridTemplateColumns: `repeat(${layout.maxCol}, 88px)` }">
                <button
                  v-for="seat in layout.seats"
                  :key="seat.seatId"
                  class="seat"
                  :class="seat.displayStatus"
                  :disabled="!seat.clickable"
                  @click="reserve(seat)"
                >
                  <span class="seat-badges"><span v-if="seat.hasSocket">电</span><span v-if="seat.nearWindow">窗</span></span>
                  <span class="seat-title">{{ seat.seatNo }}</span>
                  <span class="seat-subtitle">{{ statusText(seat.displayStatus) }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </AppCard>

    <el-dialog v-model="approvalDialog" title="提交审批预约" width="520px">
      <el-form :model="approvalForm" label-position="top">
        <el-form-item label="预约用途"><el-input v-model="approvalForm.purpose" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="参与人数"><el-input-number v-model="approvalForm.participantCount" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="approvalForm.contactPhone" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReservation">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import SeatLegend from '../components/SeatLegend.vue'
import StatusTag from '../components/StatusTag.vue'
import { spaceTypeMap, statusText } from '../config/statusMap'

const router = useRouter()
const spaceTypes = ['STUDY_ROOM', 'SEMINAR_ROOM', 'CLASSROOM', 'LAB_SEAT', 'PUBLIC_AREA']
const seatBasedTypes = ['STUDY_ROOM', 'PUBLIC_AREA', 'LAB_SEAT']
const expandedSpaceStorageKey = 'echostudy.student.reserve.expandedSpaceId'
const spaces = ref([])
const buildings = ref([])
const nodes = ref([])
const layout = ref({})
const selectedSeat = ref(null)
const approvalDialog = ref(false)
const nowMinutes = ref(minutesSinceMidnight(new Date()))
let nowTimer = null
const filters = reactive({ building: 'ALL', spaceType: '', openOnly: true })
const form = reactive({
  reserveDate: new Date().toISOString().slice(0, 10),
  roomId: null,
  startTime: '08:00:00',
  endTime: '09:00:00'
})
const approvalForm = reactive({ purpose: '', participantCount: 1, contactPhone: '' })

const selectedSpace = computed(() => spaces.value.find((space) => space.id === form.roomId))
const filteredSpaces = computed(() => spaces.value.filter((space) => {
  if (filters.spaceType && space.spaceType !== filters.spaceType) return false
  if (filters.openOnly && !space.openStatus) return false
  return true
}))

function timeText(value) {
  return String(value || '').slice(0, 5)
}

function dateText(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function minutesSinceMidnight(date) {
  return date.getHours() * 60 + date.getMinutes()
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

function isStartTimeDisabled(value) {
  return isToday(form.reserveDate) && timeToMinutes(value) < nowMinutes.value
}

function isEndTimeDisabled(value) {
  if (!form.startTime) {
    return false
  }
  return timeToMinutes(value) <= timeToMinutes(form.startTime)
}

function firstEnabledStartTime() {
  return nodes.value.find((node) => !isStartTimeDisabled(node.timeValue))?.timeValue || ''
}

function firstEnabledEndTime() {
  return nodes.value.find((node) => !isEndTimeDisabled(node.timeValue))?.timeValue || ''
}

function ensureTimeSelection() {
  if (form.startTime && isStartTimeDisabled(form.startTime)) {
    form.startTime = firstEnabledStartTime()
  }
  if (!form.startTime && nodes.value.length) {
    form.startTime = firstEnabledStartTime()
  }
  if (form.endTime && isEndTimeDisabled(form.endTime)) {
    form.endTime = firstEnabledEndTime()
  }
  if (!form.endTime && nodes.value.length) {
    form.endTime = firstEnabledEndTime()
  }
}

function validateTimeSelection() {
  refreshNow()
  if (!form.reserveDate || !form.startTime || !form.endTime) {
    ElMessage.warning('请选择完整的预约日期和时间')
    return false
  }
  if (form.reserveDate < dateText(new Date())) {
    ElMessage.warning('不能预约过去日期')
    return false
  }
  if (isStartTimeDisabled(form.startTime)) {
    ElMessage.warning('开始时间不能早于当前时间')
    return false
  }
  if (isEndTimeDisabled(form.endTime)) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }
  return true
}

function isSeatBased(space) {
  return space && seatBasedTypes.includes(space.spaceType)
}

async function loadSpaces() {
  spaces.value = await api.get('/student/spaces', {
    params: filters.building && filters.building !== 'ALL' ? { building: filters.building } : {}
  })
  if (form.roomId && !spaces.value.some((space) => space.id === form.roomId)) {
    form.roomId = null
    layout.value = {}
    sessionStorage.removeItem(expandedSpaceStorageKey)
  }
}

async function loadBuildings() {
  buildings.value = await api.get('/student/buildings')
}

async function selectBuilding(building) {
  filters.building = building
  await loadSpaces()
}

async function loadNodes() {
  nodes.value = await api.get('/student/time-nodes')
  ensureTimeSelection()
}

async function toggleSpace(space) {
  if (form.roomId === space.id) {
    collapseSpace()
    return
  }
  await selectSpace(space)
}

async function selectSpace(space) {
  form.roomId = space.id
  selectedSeat.value = null
  sessionStorage.setItem(expandedSpaceStorageKey, String(space.id))
  if (isSeatBased(space)) {
    await loadLayout()
  } else {
    layout.value = {}
  }
}

function collapseSpace() {
  form.roomId = null
  selectedSeat.value = null
  layout.value = {}
  sessionStorage.removeItem(expandedSpaceStorageKey)
}

async function loadLayout() {
  if (!validateTimeSelection()) {
    return
  }
  if (!form.roomId) {
    ElMessage.warning('请先选择空间')
    return
  }
  if (!isSeatBased(selectedSpace.value)) {
    layout.value = {}
    return
  }
  layout.value = await api.get('/student/seats/layout', {
    params: {
      roomId: form.roomId,
      date: form.reserveDate,
      startTime: form.startTime,
      endTime: form.endTime
    }
  })
}

async function reserveRoom() {
  if (!validateTimeSelection()) {
    return
  }
  selectedSeat.value = null
  if (selectedSpace.value?.needApproval) {
    approvalForm.purpose = ''
    approvalForm.participantCount = 1
    approvalForm.contactPhone = ''
    approvalDialog.value = true
    return
  }
  await ElMessageBox.confirm(`确认预约空间 ${selectedSpace.value.name}？`)
  await submitReservation()
}

async function reserve(seat) {
  if (!validateTimeSelection()) {
    return
  }
  selectedSeat.value = seat
  if (selectedSpace.value?.needApproval) {
    approvalForm.purpose = ''
    approvalForm.participantCount = 1
    approvalForm.contactPhone = ''
    approvalDialog.value = true
    return
  }
  await ElMessageBox.confirm(`确认预约座位/工位 ${seat.seatNo}？`)
  await submitReservation()
}

async function submitReservation() {
  if (!validateTimeSelection()) {
    return
  }
  const payload = {
    ...form
  }
  if (selectedSeat.value?.seatId) {
    payload.seatId = selectedSeat.value.seatId
  }
  if (selectedSpace.value?.needApproval) {
    Object.assign(payload, approvalForm)
  }
  const result = await api.post('/student/reservations/online', payload)
  approvalDialog.value = false
  ElMessage.success(result.status === 'PENDING_APPROVAL' ? '预约申请已提交，请等待管理员审批' : '预约成功')
  if (isSeatBased(selectedSpace.value)) {
    await loadLayout()
  }
  ElMessageBox.confirm('是否查看我的预约？', '提交成功', {
    confirmButtonText: '查看我的预约',
    cancelButtonText: '继续预约',
    type: 'success'
  }).then(() => router.push('/student/reservations')).catch(() => {})
}

onMounted(async () => {
  refreshNow()
  nowTimer = window.setInterval(() => {
    refreshNow()
    ensureTimeSelection()
  }, 30000)
  await Promise.all([loadBuildings(), loadSpaces(), loadNodes()])
  const savedSpaceId = Number(sessionStorage.getItem(expandedSpaceStorageKey))
  const savedSpace = filteredSpaces.value.find((space) => space.id === savedSpaceId)
  if (savedSpace) {
    await selectSpace(savedSpace)
  }
})

onBeforeUnmount(() => {
  if (nowTimer) {
    window.clearInterval(nowTimer)
  }
})

watch([() => form.reserveDate, () => nodes.value, nowMinutes], () => {
  ensureTimeSelection()
})

watch(() => form.startTime, () => {
  ensureTimeSelection()
})
</script>

<style scoped>
.building-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.building-card {
  min-width: 86px;
  height: 38px;
  padding: 0 16px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-sm);
  background: #f8fbff;
  color: var(--es-text-secondary);
  font-weight: 800;
  cursor: pointer;
  transition: all 0.18s ease;
}

.building-card:hover {
  border-color: var(--es-primary);
  color: var(--es-primary);
}

.building-card.active {
  border-color: var(--es-primary);
  background: var(--es-primary);
  color: #ffffff;
  box-shadow: 0 8px 18px rgba(47, 143, 131, 0.16);
}

.space-list {
  display: grid;
  gap: 14px;
}

.space-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-md);
  background: #ffffff;
}

.space-item.active {
  border-color: var(--es-primary);
  box-shadow: 0 8px 20px rgba(47, 143, 131, 0.12);
}

.space-item__title,
.space-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.space-item__title {
  margin-bottom: 8px;
}

.space-item__meta {
  color: var(--es-text-secondary);
  font-size: 13px;
}

.space-item p {
  margin: 10px 0 0;
  color: var(--es-text-secondary);
}

.space-item__detail {
  flex: 1 0 100%;
  width: 100%;
  padding-top: 4px;
}

.inline-seat-layout {
  display: grid;
  gap: 14px;
  padding-top: 4px;
}

.inline-seat-layout .seat-grid {
  width: max-content;
  max-width: 100%;
  justify-self: center;
  justify-content: center;
}

.panel-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-actions {
  flex-shrink: 0;
}

.room-reserve-panel {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 18px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-md);
  background: #ffffff;
}

.room-reserve-panel p {
  margin: 8px 0 0;
  color: var(--es-text-secondary);
}

.approval-pill {
  padding: 4px 8px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #475569;
  font-size: 12px;
  font-weight: 800;
}
</style>
