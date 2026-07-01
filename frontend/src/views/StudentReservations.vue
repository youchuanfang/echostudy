<template>
  <div class="es-page">
    <PageHeader page-key="myReservation" />
    <AppCard title="我的预约列表">
      <div class="toolbar reservation-toolbar">
        <el-select v-model="statusFilter" clearable placeholder="状态筛选" style="width: 180px">
          <el-option v-for="status in statuses" :key="status" :label="statusText(status)" :value="status" />
        </el-select>
      </div>
      <el-table :data="filteredRows">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roomName" label="空间" min-width="140" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ spaceTypeMap[row.spaceType] || row.spaceType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reserveDate" label="日期" width="120" />
        <el-table-column label="时间段" min-width="120">
          <template #default="{ row }">{{ timeText(row.startTime) }} - {{ timeText(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="预约单位" width="110">
          <template #default="{ row }">{{ row.seatNo || '整间空间' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column label="审批" width="90">
          <template #default="{ row }">{{ row.needApproval ? '需要' : '无需' }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="150" show-overflow-tooltip />
        <el-table-column prop="participantCount" label="人数" width="80" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="rejectReason" label="驳回原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canCancel(row)" size="small" @click="act(row.id, 'cancel')">
              {{ row.status === 'PENDING_APPROVAL' ? '取消申请' : '取消' }}
            </el-button>
            <el-button v-if="row.status === 'RESERVED'" size="small" type="primary" @click="openSign(row.id)">签到</el-button>
            <el-button v-if="row.status === 'USING'" size="small" @click="act(row.id, 'leave')">暂离</el-button>
            <el-button v-if="row.status === 'LEAVE'" size="small" @click="act(row.id, 'return')">返座</el-button>
            <el-button v-if="row.status === 'USING' || row.status === 'LEAVE'" size="small" type="success" @click="act(row.id, 'finish')">结束</el-button>
            <span v-if="row.status === 'REJECTED'" class="muted-action">查看驳回原因</span>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="signDialog" title="定位签到" width="420px">
      <el-form label-position="top">
        <el-form-item label="纬度"><el-input v-model="sign.latitude" /></el-form-item>
        <el-form-item label="经度"><el-input v-model="sign.longitude" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="getLocation">获取当前位置</el-button>
        <el-button type="primary" @click="submitSign">提交签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { spaceTypeMap, statusText } from '../config/statusMap'

const statuses = ['PENDING_APPROVAL', 'RESERVED', 'USING', 'LEAVE', 'COMPLETED', 'REJECTED', 'CANCELLED', 'VIOLATED']
const rows = ref([])
const statusFilter = ref('')
const signDialog = ref(false)
const signId = ref(null)
const sign = reactive({ latitude: '31.2304160', longitude: '121.4737010' })

const filteredRows = computed(() => statusFilter.value ? rows.value.filter((row) => row.status === statusFilter.value) : rows.value)

function timeText(value) {
  return String(value || '').slice(0, 5)
}

function canCancel(row) {
  return row.status === 'RESERVED' || row.status === 'PENDING_APPROVAL'
}

async function load() {
  rows.value = await api.get('/student/reservations/my')
}

async function act(id, action) {
  await api.post(`/student/reservations/${id}/${action}`)
  ElMessage.success('操作成功')
  await load()
}

function openSign(id) {
  signId.value = id
  signDialog.value = true
}

function getLocation() {
  navigator.geolocation.getCurrentPosition((pos) => {
    sign.latitude = String(pos.coords.latitude)
    sign.longitude = String(pos.coords.longitude)
  })
}

async function submitSign() {
  await api.post(`/student/reservations/${signId.value}/sign-in`, sign)
  signDialog.value = false
  ElMessage.success('签到成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.reservation-toolbar {
  margin-bottom: 16px;
}

.muted-action {
  color: var(--es-text-secondary);
  font-size: 13px;
  font-weight: 700;
}
</style>
