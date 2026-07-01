<template>
  <div class="es-page">
    <PageHeader page-key="offlineReservation" />
    <AppCard title="线下代预约条件">
      <div class="form-grid">
        <el-form-item label="学生"><el-select v-model="form.userId" filterable placeholder="学生"><el-option v-for="u in students" :key="u.id" :label="u.username" :value="u.id" /></el-select></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.reserveDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="空间"><el-select v-model="form.roomId" placeholder="空间"><el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-time-picker v-model="form.startTime" value-format="HH:mm:ss" placeholder="开始" /></el-form-item>
        <el-form-item label="结束时间"><el-time-picker v-model="form.endTime" value-format="HH:mm:ss" placeholder="结束" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="prepareReservation">{{ selectedRoom && isSeatBased(selectedRoom) ? '查看座位/工位' : '代预约空间' }}</el-button></div>
      </div>
    </AppCard>
    <AppCard v-if="selectedRoom && !isSeatBased(selectedRoom)" :title="`${selectedRoom.name} 整间代预约`">
      <div class="room-reserve-panel">
        <span>该空间按整间预约，无需选择座位/工位。</span>
        <el-button type="primary" @click="reserveRoom">确认代预约</el-button>
      </div>
    </AppCard>
    <AppCard v-if="layout.roomId && selectedRoom && isSeatBased(selectedRoom)" :title="layout.roomName">
      <SeatLegend />
      <div class="seat-grid" :style="{ gridTemplateColumns: `repeat(${layout.maxCol}, 88px)` }">
        <button v-for="seat in layout.seats" :key="seat.seatId" class="seat" :class="seat.displayStatus" :disabled="!seat.clickable" @click="reserve(seat)">
          <span class="seat-badges"><span v-if="seat.hasSocket">插</span><span v-if="seat.nearWindow">窗</span></span>
          <span class="seat-title">{{ seat.seatNo }}</span>
          <span class="seat-subtitle">{{ statusText(seat.displayStatus) }}</span>
        </button>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import SeatLegend from '../components/SeatLegend.vue'
import { statusText } from '../config/statusMap'

const users = ref([])
const rooms = ref([])
const layout = ref({})
const students = ref([])
const form = reactive({ userId: null, reserveDate: new Date().toISOString().slice(0, 10), roomId: null, startTime: '08:00:00', endTime: '09:00:00' })
const seatBasedTypes = ['STUDY_ROOM', 'PUBLIC_AREA', 'LAB_SEAT']
const selectedRoom = computed(() => rooms.value.find((room) => room.id === form.roomId))

function isSeatBased(room) {
  return room && seatBasedTypes.includes(room.spaceType)
}

onMounted(async () => {
  users.value = await api.get('/admin/users')
  students.value = users.value.filter((u) => u.role === 'STUDENT')
  rooms.value = await api.get('/admin/rooms')
  form.userId = students.value[0]?.id
  form.roomId = rooms.value[0]?.id
})

async function prepareReservation() {
  if (isSeatBased(selectedRoom.value)) {
    await loadLayout()
  } else {
    layout.value = {}
  }
}

async function loadLayout() {
  if (!isSeatBased(selectedRoom.value)) {
    layout.value = {}
    return
  }
  layout.value = await api.get('/admin/seats/layout', {
    params: {
      roomId: form.roomId,
      date: form.reserveDate,
      startTime: form.startTime,
      endTime: form.endTime
    }
  })
}
async function reserveRoom() {
  await ElMessageBox.confirm(`为学生代预约 ${selectedRoom.value.name}？`)
  await api.post('/admin/reservations/offline', { ...form })
  ElMessage.success('代预约成功')
}
async function reserve(seat) {
  await ElMessageBox.confirm(`为学生代预约 ${seat.seatNo}？`)
  await api.post('/admin/reservations/offline', { ...form, seatId: seat.seatId })
  ElMessage.success('代预约成功')
  await loadLayout()
}
</script>

<style scoped>
.room-reserve-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-md);
  background: #ffffff;
  color: var(--es-text-secondary);
}
</style>
