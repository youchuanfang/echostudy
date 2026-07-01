<template>
  <div class="es-page">
    <PageHeader page-key="adminRepairs" />

    <div class="metric-grid">
      <div class="metric-card"><h3>待处理</h3><strong>{{ stats.PENDING }}</strong></div>
      <div class="metric-card"><h3>已受理</h3><strong>{{ stats.ACCEPTED }}</strong></div>
      <div class="metric-card"><h3>处理中</h3><strong>{{ stats.PROCESSING }}</strong></div>
      <div class="metric-card"><h3>已完成</h3><strong>{{ stats.DONE }}</strong></div>
      <div class="metric-card"><h3>已驳回</h3><strong>{{ stats.REJECTED }}</strong></div>
    </div>

    <AppCard title="筛选条件">
      <div class="form-grid">
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable>
            <el-option v-for="status in repairStatuses" :key="status" :label="repairStatusMap[status]" :value="status" />
          </el-select>
        </el-form-item>
        <el-form-item label="报修级别">
          <el-select v-model="filters.repairLevel" clearable>
            <el-option v-for="level in repairLevels" :key="level" :label="repairLevelMap[level]" :value="level" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型">
          <el-select v-model="filters.repairType" clearable>
            <el-option v-for="type in repairTypes" :key="type" :label="repairTypeMap[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="空间">
          <el-select v-model="filters.roomId" clearable filterable>
            <el-option v-for="space in spaces" :key="space.id" :label="space.name" :value="space.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="filters.startDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="filters.endDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="关键词"><el-input v-model="filters.keyword" placeholder="学生、空间、描述" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="load">搜索</el-button></div>
        <div class="form-action"><el-button @click="resetFilters">重置</el-button></div>
      </div>
    </AppCard>

    <AppCard title="报修表格">
      <el-table :data="rows">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column label="学生" min-width="120"><template #default="{ row }">{{ row.realName || row.username }}</template></el-table-column>
        <el-table-column prop="roomName" label="空间" min-width="140" />
        <el-table-column label="报修对象" width="110"><template #default="{ row }">{{ row.seatNo || '-' }}</template></el-table-column>
        <el-table-column label="级别" width="130"><template #default="{ row }">{{ repairLevelMap[row.repairLevel] }}</template></el-table-column>
        <el-table-column label="故障类型" width="120"><template #default="{ row }">{{ repairTypeMap[row.repairType] }}</template></el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column prop="adminReply" label="管理员回复" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="310" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="primary" @click="openHandle(row, 'accept')">受理</el-button>
            <el-button v-if="row.status === 'ACCEPTED'" size="small" @click="openHandle(row, 'process')">处理中</el-button>
            <el-button v-if="row.status === 'PENDING' || row.status === 'ACCEPTED'" size="small" type="danger" @click="openHandle(row, 'reject')">驳回</el-button>
            <el-button v-if="row.status === 'ACCEPTED' || row.status === 'PROCESSING'" size="small" type="success" @click="openHandle(row, 'finish')">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="detailDialog" title="报修详情" width="640px">
      <div v-if="current" class="detail-grid">
        <div><span>学生</span><strong>{{ current.realName || current.username }}</strong></div>
        <div><span>空间</span><strong>{{ current.roomName }}</strong></div>
        <div><span>报修对象</span><strong>{{ current.seatNo || '-' }}</strong></div>
        <div><span>级别</span><strong>{{ repairLevelMap[current.repairLevel] }}</strong></div>
        <div><span>故障类型</span><strong>{{ repairTypeMap[current.repairType] }}</strong></div>
        <div><span>状态</span><strong>{{ repairStatusMap[current.status] }}</strong></div>
        <div class="detail-grid__wide"><span>问题描述</span><strong>{{ current.description }}</strong></div>
        <div class="detail-grid__wide"><span>管理员回复</span><strong>{{ current.adminReply || '-' }}</strong></div>
      </div>
    </el-dialog>

    <el-dialog v-model="handleDialog" :title="handleTitle" width="520px">
      <el-form label-position="top">
        <el-form-item label="处理说明/回复">
          <el-input v-model="handleForm.adminReply" type="textarea" :rows="4" :placeholder="replyPlaceholder" />
        </el-form-item>
        <el-checkbox v-if="current?.repairLevel === 'SEAT' && (action === 'accept' || action === 'process')" v-model="handleForm.markSeatFaulty">
          同时将座位/工位标记为故障
        </el-checkbox>
        <el-checkbox v-if="current?.repairLevel === 'SEAT' && action === 'finish'" v-model="handleForm.recoverSeat">
          同时恢复座位/工位可用
        </el-checkbox>
        <el-checkbox v-if="current?.repairLevel === 'SPACE' && (action === 'accept' || action === 'process')" v-model="handleForm.closeSpace">
          同时临时关闭该空间
        </el-checkbox>
        <el-checkbox v-if="current?.repairLevel === 'SPACE' && action === 'finish'" v-model="handleForm.reopenSpace">
          同时重新开放该空间
        </el-checkbox>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认</el-button>
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
import { repairLevelMap, repairStatusMap, repairTypeMap } from '../config/statusMap'

const repairStatuses = ['PENDING', 'ACCEPTED', 'PROCESSING', 'DONE', 'REJECTED', 'CANCELLED']
const repairLevels = ['SPACE', 'SEAT']
const repairTypes = ['SOCKET', 'CHAIR', 'DESK', 'LIGHT', 'AIR_CONDITIONER', 'NETWORK', 'ACCESS_CONTROL', 'HYGIENE', 'OTHER']
const rows = ref([])
const spaces = ref([])
const current = ref(null)
const detailDialog = ref(false)
const handleDialog = ref(false)
const action = ref('')
const filters = reactive(defaultFilters())
const handleForm = reactive(defaultHandleForm())

const stats = computed(() => {
  const seed = { PENDING: 0, ACCEPTED: 0, PROCESSING: 0, DONE: 0, REJECTED: 0 }
  rows.value.forEach((row) => {
    if (seed[row.status] !== undefined) seed[row.status] += 1
  })
  return seed
})

const handleTitle = computed(() => ({
  accept: '受理报修',
  process: '标记处理中',
  reject: '驳回报修',
  finish: '完成报修'
}[action.value] || '处理报修'))

const replyPlaceholder = computed(() => (action.value === 'reject' ? '驳回必须填写原因' : action.value === 'finish' ? '完成必须填写处理说明' : '可填写给学生的处理说明'))

function defaultFilters() {
  return { status: '', repairLevel: '', repairType: '', roomId: null, startDate: '', endDate: '', keyword: '' }
}

function defaultHandleForm() {
  return { adminReply: '', markSeatFaulty: false, recoverSeat: false, closeSpace: false, reopenSpace: false }
}

async function loadSpaces() {
  spaces.value = await api.get('/admin/spaces')
}

async function load() {
  rows.value = await api.get('/admin/repairs', {
    params: {
      status: filters.status || undefined,
      repairLevel: filters.repairLevel || undefined,
      repairType: filters.repairType || undefined,
      roomId: filters.roomId || undefined,
      startDate: filters.startDate || undefined,
      endDate: filters.endDate || undefined,
      keyword: filters.keyword || undefined
    }
  })
}

function resetFilters() {
  Object.assign(filters, defaultFilters())
  load()
}

async function openDetail(row) {
  current.value = await api.get(`/admin/repairs/${row.id}`)
  detailDialog.value = true
}

function openHandle(row, nextAction) {
  current.value = row
  action.value = nextAction
  Object.assign(handleForm, defaultHandleForm())
  handleDialog.value = true
}

async function submitHandle() {
  if ((action.value === 'reject' || action.value === 'finish') && !handleForm.adminReply.trim()) {
    ElMessage.warning(action.value === 'reject' ? '请填写驳回原因' : '请填写处理说明')
    return
  }
  await api.post(`/admin/repairs/${current.value.id}/${action.value}`, handleForm)
  ElMessage.success('处理成功')
  handleDialog.value = false
  await load()
}

onMounted(async () => {
  await Promise.all([loadSpaces(), load()])
})
</script>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-grid > div {
  padding: 12px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-sm);
  background: #f8fbfd;
}

.detail-grid span {
  display: block;
  color: var(--es-text-secondary);
  font-size: 12px;
  margin-bottom: 6px;
}

.detail-grid strong {
  color: var(--es-text-main);
}

.detail-grid__wide {
  grid-column: 1 / -1;
}
</style>
