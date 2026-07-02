<template>
  <div class="es-page">
    <PageHeader page-key="spaceManage" />

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

    <AppCard title="空间资源">
      <div class="toolbar">
        <el-select v-model="filters.spaceType" clearable placeholder="空间类型" style="width: 180px" @change="load">
          <el-option label="全部" value="" />
          <el-option v-for="type in spaceTypes" :key="type" :label="spaceTypeMap[type]" :value="type" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增空间</el-button>
      </div>
    </AppCard>

    <AppCard title="空间资源列表">
      <el-table :data="rows">
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }"><StatusTag :value="row.spaceType" /></template>
        </el-table-column>
        <el-table-column label="楼栋/楼层" min-width="130">
          <template #default="{ row }">{{ [row.building, row.floorNo].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column prop="locationDesc" label="位置" min-width="150" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column label="开放时间" min-width="140">
          <template #default="{ row }">{{ timeText(row.openTime) }} - {{ timeText(row.closeTime) }}</template>
        </el-table-column>
        <el-table-column label="开放" width="90">
          <template #default="{ row }"><StatusTag :value="row.openStatus ? 'NORMAL' : 'DISABLED'" /></template>
        </el-table-column>
        <el-table-column label="审批" width="90">
          <template #default="{ row }">{{ row.needApproval ? '需要' : '无需' }}</template>
        </el-table-column>
        <el-table-column label="定位签到" width="110">
          <template #default="{ row }">{{ row.needLocationCheck ? '启用' : '关闭' }}</template>
        </el-table-column>
        <el-table-column prop="managerName" label="负责人" width="110" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.openStatus ? 'warning' : 'success'" @click="toggleOpen(row)">
              {{ row.openStatus ? '关闭' : '开放' }}
            </el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑空间' : '新增空间'" width="760px">
      <el-form :model="form" label-position="top">
        <div class="form-grid three">
          <el-form-item label="空间名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="空间类型">
            <el-select v-model="form.spaceType">
              <el-option v-for="type in spaceTypes" :key="type" :label="spaceTypeMap[type]" :value="type" />
            </el-select>
          </el-form-item>
          <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="楼栋">
            <el-select v-model="buildingChoice" placeholder="请选择楼栋" @change="syncBuildingFromChoice">
              <el-option v-for="building in buildings" :key="building" :label="building" :value="building" />
              <el-option label="其他" value="__OTHER__" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="buildingChoice === '__OTHER__'" label="新楼栋名称">
            <el-input v-model="customBuilding" placeholder="请输入新楼栋名称" @input="syncBuildingFromChoice" />
          </el-form-item>
          <el-form-item label="楼层">
            <el-input-number
              v-model="form.floorNo"
              :min="1"
              :step="1"
              :precision="0"
              placeholder="请输入数字，例如 1、2、10"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="位置描述"><el-input v-model="form.locationDesc" /></el-form-item>
          <el-form-item label="开放时间"><el-time-picker v-model="form.openTime" value-format="HH:mm:ss" /></el-form-item>
          <el-form-item label="关闭时间"><el-time-picker v-model="form.closeTime" value-format="HH:mm:ss" /></el-form-item>
          <el-form-item label="签到范围（米）"><el-input-number v-model="form.allowedRadiusMeter" :min="1" style="width: 100%" /></el-form-item>
          <el-form-item label="签到纬度"><el-input v-model="form.latitude" /></el-form-item>
          <el-form-item label="签到经度"><el-input v-model="form.longitude" /></el-form-item>
          <el-form-item label="负责人"><el-input v-model="form.managerName" /></el-form-item>
          <el-form-item label="负责人电话"><el-input v-model="form.managerPhone" /></el-form-item>
          <el-form-item label="是否开放"><el-switch v-model="form.openStatus" /></el-form-item>
          <el-form-item label="是否需要审批"><el-switch v-model="form.needApproval" /></el-form-item>
          <el-form-item label="是否需要定位签到"><el-switch v-model="form.needLocationCheck" /></el-form-item>
        </div>
        <el-form-item label="使用须知"><el-input v-model="form.usageNotice" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { spaceTypeMap } from '../config/statusMap'

const spaceTypes = ['STUDY_ROOM', 'SEMINAR_ROOM', 'CLASSROOM', 'LAB_SEAT', 'PUBLIC_AREA']
const rows = ref([])
const buildings = ref([])
const dialogVisible = ref(false)
const buildingChoice = ref('__OTHER__')
const customBuilding = ref('')
const filters = reactive({ building: 'ALL', spaceType: '' })
const form = reactive(defaultForm())

function defaultForm() {
  return {
    id: null,
    name: '',
    spaceType: 'STUDY_ROOM',
    building: '',
    floorNo: '',
    locationDesc: '',
    capacity: 0,
    openTime: '08:00:00',
    closeTime: '22:00:00',
    openStatus: true,
    needApproval: false,
    needLocationCheck: true,
    latitude: '',
    longitude: '',
    allowedRadiusMeter: 50,
    managerName: '',
    managerPhone: '',
    usageNotice: '',
    description: ''
  }
}

function resetForm(data = {}) {
  Object.assign(form, defaultForm(), data)
  form.floorNo = data.floorNo ? Number(data.floorNo) || 1 : 1
  syncBuildingChoice(form.building)
}

function timeText(value) {
  return String(value || '').slice(0, 5)
}

async function load() {
  const params = {}
  if (filters.spaceType) params.spaceType = filters.spaceType
  if (filters.building && filters.building !== 'ALL') params.building = filters.building
  rows.value = await api.get('/admin/spaces', { params })
}

async function loadBuildings() {
  buildings.value = await api.get('/admin/buildings')
}

async function selectBuilding(building) {
  filters.building = building
  await load()
}

function syncBuildingChoice(building) {
  if (building && buildings.value.includes(building)) {
    buildingChoice.value = building
    customBuilding.value = ''
    return
  }
  buildingChoice.value = '__OTHER__'
  customBuilding.value = building || ''
}

function syncBuildingFromChoice() {
  form.building = buildingChoice.value === '__OTHER__' ? customBuilding.value.trim() : buildingChoice.value
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  resetForm({ ...row })
  dialogVisible.value = true
}

async function save() {
  syncBuildingFromChoice()
  const payload = {
    ...form,
    floorNo: form.floorNo ? String(form.floorNo) : ''
  }
  if (payload.id) {
    await api.put(`/admin/spaces/${payload.id}`, payload)
  } else {
    await api.post('/admin/spaces', payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadBuildings()
  await load()
}

async function toggleOpen(row) {
  await api.post(`/admin/spaces/${row.id}/${row.openStatus ? 'close' : 'open'}`)
  ElMessage.success(row.openStatus ? '已关闭空间' : '已开放空间')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除空间“${row.name}”？`)
  await api.delete(`/admin/spaces/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  await loadBuildings()
  await load()
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
</style>
