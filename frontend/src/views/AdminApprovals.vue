<template>
  <div class="es-page">
    <PageHeader page-key="approvalManage" />

    <AppCard title="筛选条件">
      <div class="form-grid">
        <el-form-item label="空间类型">
          <el-select v-model="filters.spaceType" clearable placeholder="全部类型">
            <el-option v-for="type in spaceTypes" :key="type" :label="spaceTypeMap[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期"><el-date-picker v-model="filters.date" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="申请人"><el-input v-model="filters.applicant" placeholder="用户名或姓名" /></el-form-item>
        <div class="form-action"><el-button type="primary" @click="load">搜索</el-button></div>
      </div>
    </AppCard>

    <AppCard title="待审批列表">
      <el-table :data="rows">
        <el-table-column prop="realName" label="申请人" min-width="110">
          <template #default="{ row }">{{ row.realName || row.username }}</template>
        </el-table-column>
        <el-table-column prop="roomName" label="空间名称" min-width="140" />
        <el-table-column label="空间类型" width="120">
          <template #default="{ row }">{{ spaceTypeMap[row.spaceType] || row.spaceType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reserveDate" label="日期" width="120" />
        <el-table-column label="时间段" min-width="120">
          <template #default="{ row }">{{ timeText(row.startTime) }} - {{ timeText(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="预约单位" width="110">
          <template #default="{ row }">{{ row.seatNo || '整间空间' }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="160" show-overflow-tooltip />
        <el-table-column prop="participantCount" label="人数" width="80" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="createTime" label="提交时间" min-width="160" />
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="success" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" @click="openReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="detailDialog" title="审批详情" width="640px">
      <div v-if="current" class="detail-grid">
        <div><span>申请人</span><strong>{{ current.realName || current.username }}</strong></div>
        <div><span>空间</span><strong>{{ current.roomName }}</strong></div>
        <div><span>空间类型</span><strong>{{ spaceTypeMap[current.spaceType] || current.spaceType }}</strong></div>
        <div><span>预约单位</span><strong>{{ current.seatNo || '整间空间' }}</strong></div>
        <div><span>日期</span><strong>{{ current.reserveDate }}</strong></div>
        <div><span>时间</span><strong>{{ timeText(current.startTime) }} - {{ timeText(current.endTime) }}</strong></div>
        <div><span>参与人数</span><strong>{{ current.participantCount || '-' }}</strong></div>
        <div><span>联系电话</span><strong>{{ current.contactPhone || '-' }}</strong></div>
        <div class="detail-grid__wide"><span>申请用途</span><strong>{{ current.purpose || '-' }}</strong></div>
        <div class="detail-grid__wide"><span>冲突检测</span><strong>审批通过时系统会再次检测资源和用户时间冲突</strong></div>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
        <el-button type="success" @click="approve(current)">通过</el-button>
        <el-button type="danger" @click="openReject(current)">驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialog" title="驳回预约" width="460px">
      <el-form label-position="top">
        <el-form-item label="驳回原因"><el-input v-model="rejectReason" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" @click="reject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { spaceTypeMap } from '../config/statusMap'

const spaceTypes = ['STUDY_ROOM', 'SEMINAR_ROOM', 'CLASSROOM', 'LAB_SEAT', 'PUBLIC_AREA']
const rows = ref([])
const current = ref(null)
const detailDialog = ref(false)
const rejectDialog = ref(false)
const rejectReason = ref('')
const filters = reactive({ spaceType: '', date: '', applicant: '' })

function timeText(value) {
  return String(value || '').slice(0, 5)
}

async function load() {
  rows.value = await api.get('/admin/approvals', {
    params: {
      spaceType: filters.spaceType || undefined,
      date: filters.date || undefined,
      applicant: filters.applicant || undefined
    }
  })
}

async function openDetail(row) {
  current.value = await api.get(`/admin/approvals/${row.id}`)
  detailDialog.value = true
}

async function approve(row) {
  if (!row) return
  await api.post(`/admin/approvals/${row.id}/approve`)
  ElMessage.success('审批通过')
  detailDialog.value = false
  await load()
}

function openReject(row) {
  current.value = row
  rejectReason.value = ''
  rejectDialog.value = true
}

async function reject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await api.post(`/admin/approvals/${current.value.id}/reject`, { rejectReason: rejectReason.value })
  ElMessage.success('已驳回')
  rejectDialog.value = false
  detailDialog.value = false
  await load()
}

onMounted(load)
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
