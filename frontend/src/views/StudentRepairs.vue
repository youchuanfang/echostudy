<template>
  <div class="es-page">
    <PageHeader page-key="studentRepairs" />

    <AppCard title="提交报修">
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="报修级别">
            <el-radio-group v-model="form.repairLevel" @change="onLevelChange">
              <el-radio-button label="SPACE">空间级</el-radio-button>
              <el-radio-button v-if="isSeatBased(selectedSpace)" label="SEAT">座位/工位级</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="选择空间">
            <el-select v-model="form.roomId" filterable placeholder="请选择空间" @change="loadSeats">
              <el-option v-for="space in spaces" :key="space.id" :label="space.name" :value="space.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.repairLevel === 'SEAT' && isSeatBased(selectedSpace)" label="选择座位/工位">
            <el-select v-model="form.seatId" filterable placeholder="请选择座位/工位">
              <el-option v-for="seat in seats" :key="seat.id" :label="seat.seatNo" :value="seat.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="故障类型">
            <el-select v-model="form.repairType">
              <el-option v-for="type in repairTypes" :key="type" :label="repairTypeMap[type]" :value="type" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="问题描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述故障现象、位置和影响范围" />
        </el-form-item>
        <el-button type="primary" @click="submit">提交报修</el-button>
      </el-form>
    </AppCard>

    <AppCard title="我的报修">
      <el-table :data="repairs">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="roomName" label="空间" min-width="140" />
        <el-table-column label="报修对象" width="110">
          <template #default="{ row }">{{ row.seatNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="级别" width="130">
          <template #default="{ row }">{{ repairLevelMap[row.repairLevel] || row.repairLevel }}</template>
        </el-table-column>
        <el-table-column label="故障类型" width="120">
          <template #default="{ row }">{{ repairTypeMap[row.repairType] || row.repairType }}</template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column prop="adminReply" label="管理员回复" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="cancel(row)">取消报修</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { repairLevelMap, repairTypeMap } from '../config/statusMap'

const repairTypes = ['SOCKET', 'CHAIR', 'DESK', 'LIGHT', 'AIR_CONDITIONER', 'NETWORK', 'ACCESS_CONTROL', 'HYGIENE', 'OTHER']
const spaces = ref([])
const seats = ref([])
const repairs = ref([])
const form = reactive(defaultForm())
const seatBasedTypes = ['STUDY_ROOM', 'PUBLIC_AREA', 'LAB_SEAT']
const selectedSpace = computed(() => spaces.value.find((space) => space.id === form.roomId))

function isSeatBased(space) {
  return space && seatBasedTypes.includes(space.spaceType)
}

function defaultForm() {
  return {
    roomId: null,
    seatId: null,
    repairLevel: 'SPACE',
    repairType: 'OTHER',
    description: ''
  }
}

function resetForm() {
  Object.assign(form, defaultForm(), { roomId: spaces.value[0]?.id || null })
  if (form.roomId) loadSeats()
}

function onLevelChange() {
  if (form.repairLevel === 'SPACE' || !isSeatBased(selectedSpace.value)) {
    form.seatId = null
  }
}

async function loadSpaces() {
  spaces.value = await api.get('/student/spaces')
  form.roomId = spaces.value[0]?.id || null
}

async function loadSeats() {
  form.seatId = null
  if (!isSeatBased(selectedSpace.value)) {
    form.repairLevel = 'SPACE'
    seats.value = []
    return
  }
  if (!form.roomId) {
    seats.value = []
    return
  }
  seats.value = await api.get(`/student/spaces/${form.roomId}/seats`)
}

async function loadRepairs() {
  repairs.value = await api.get('/student/repairs/my')
}

async function submit() {
  if (!form.roomId) {
    ElMessage.warning('请选择空间')
    return
  }
  if (form.repairLevel === 'SEAT' && !form.seatId) {
    ElMessage.warning('请选择座位/工位')
    return
  }
  if (!form.description.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }
  await api.post('/student/repairs', { ...form })
  ElMessage.success('报修已提交')
  resetForm()
  await loadRepairs()
}

async function cancel(row) {
  await ElMessageBox.confirm(`确认取消报修 #${row.id}？`)
  await api.post(`/student/repairs/${row.id}/cancel`)
  ElMessage.success('报修已取消')
  await loadRepairs()
}

onMounted(async () => {
  await loadSpaces()
  await loadSeats()
  await loadRepairs()
})
</script>
