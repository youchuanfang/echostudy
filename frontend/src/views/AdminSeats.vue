<template>
  <div class="es-page">
    <PageHeader page-key="seatManage" />
    <AppCard title="座位生成与筛选">
      <div class="form-grid">
        <el-form-item label="座位/工位空间">
          <el-select v-model="roomId" placeholder="请选择座位/工位空间" @change="load">
            <el-option v-for="r in seatRooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="行数"><el-input-number v-model="batch.rows" :min="1" :max="20" /></el-form-item>
        <el-form-item label="列数"><el-input-number v-model="batch.cols" :min="1" :max="20" /></el-form-item>
        <div class="form-action"><el-button type="primary" :disabled="!roomId" @click="batchGenerate">批量生成</el-button></div>
      </div>
    </AppCard>
    <AppCard v-if="roomId" title="座位/工位网格">
      <SeatLegend />
      <div class="seat-grid" :style="{ gridTemplateColumns: `repeat(${maxCol}, minmax(64px, 1fr))` }">
        <button v-for="seat in seats" :key="seat.id" class="seat" :class="seat.faulty ? 'FAULTY' : seat.enabled ? 'AVAILABLE' : 'DISABLED'" @click="toggle(seat)">
          <span class="seat-badges"><span v-if="seat.hasSocket">插</span><span v-if="seat.nearWindow">窗</span></span>
          <span class="seat-title">{{ seat.seatNo }}</span>
          <span class="seat-subtitle">{{ seat.faulty ? '故障' : seat.enabled ? '可用' : '不可用' }}</span>
        </button>
      </div>
    </AppCard>
    <AppCard v-else title="座位/工位网格">
      <el-empty description="暂无可维护座位/工位的空间" />
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import SeatLegend from '../components/SeatLegend.vue'

const rooms = ref([])
const seats = ref([])
const roomId = ref(null)
const batch = reactive({ rows: 4, cols: 6 })
const seatBasedTypes = ['STUDY_ROOM', 'PUBLIC_AREA', 'LAB_SEAT']
const seatRooms = computed(() => rooms.value.filter((room) => seatBasedTypes.includes(room.spaceType)))
const maxCol = computed(() => Math.max(...seats.value.map((s) => s.colNo), 1))

async function loadRooms() {
  rooms.value = await api.get('/admin/rooms')
  roomId.value = roomId.value || seatRooms.value[0]?.id || null
  await load()
}
async function load() { seats.value = roomId.value ? await api.get('/admin/seats', { params: { roomId: roomId.value } }) : [] }
async function batchGenerate() { if (!roomId.value) return; await api.post('/admin/seats/batch-generate', { roomId: roomId.value, ...batch }); await load() }
async function toggle(seat) { await api.put(`/admin/seats/${seat.id}/status`, { enabled: !seat.enabled, faulty: seat.faulty }); await load() }
onMounted(loadRooms)
</script>
