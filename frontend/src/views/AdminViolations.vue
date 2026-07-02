<template>
  <div class="es-page">
    <PageHeader page-key="violationManage" />

    <div class="metric-grid">
      <div class="metric-card">
        <h3>违规记录</h3>
        <strong>{{ rows.length }}</strong>
      </div>
      <div class="metric-card">
        <h3>待处理申诉</h3>
        <strong>{{ pendingAppeals.length }}</strong>
      </div>
      <div class="metric-card">
        <h3>已处理申诉</h3>
        <strong>{{ handledAppeals.length }}</strong>
      </div>
    </div>

    <AppCard title="违规申诉处理">
      <template #extra>
        <el-select v-model="appealStatus" style="width: 150px" @change="loadAppeals">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </template>
      <el-table :data="appeals" empty-text="暂无申诉记录">
        <el-table-column prop="createTime" label="提交时间" min-width="160" />
        <el-table-column label="学生" min-width="150">
          <template #default="{ row }">{{ row.realName || row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column prop="violationId" label="违规ID" width="100" />
        <el-table-column prop="reason" label="申诉说明" min-width="260" show-overflow-tooltip />
        <el-table-column prop="evidence" label="补充材料" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column prop="reviewReply" label="处理说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'approve')">通过</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 'PENDING'" @click="openReview(row, 'reject')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <AppCard title="全部违规记录">
      <el-table :data="rows" empty-text="暂无违规记录">
        <el-table-column prop="createTime" label="时间" min-width="160" />
        <el-table-column label="学生" min-width="150">
          <template #default="{ row }">{{ row.realName || row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column prop="reservationId" label="预约ID" width="100" />
        <el-table-column label="类型" min-width="150">
          <template #default="{ row }">{{ violationTypeText(row.violationType) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="260" show-overflow-tooltip />
        <el-table-column prop="creditDeductPoints" label="扣分" width="90" />
        <el-table-column prop="violationCountSnapshot" label="累计" width="90" />
        <el-table-column label="申诉" width="120">
          <template #default="{ row }">
            <StatusTag v-if="row.appealStatus" :value="row.appealStatus" />
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" :title="reviewAction === 'approve' ? '通过申诉' : '驳回申诉'" width="520px">
      <el-form label-position="top">
        <el-form-item label="申诉说明">
          <el-input :model-value="current.reason" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="reviewReply"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            :placeholder="reviewAction === 'approve' ? '可填写通过原因或线下核实情况' : '请说明驳回原因，学生会在消息中看到'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="reviewAction === 'approve' ? 'success' : 'danger'" :loading="saving" @click="submitReview">
          {{ reviewAction === 'approve' ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { violationTypeText } from '../config/statusMap'

const rows = ref([])
const appeals = ref([])
const allAppealsForMetrics = ref([])
const appealStatus = ref('PENDING')
const dialogVisible = ref(false)
const current = ref({})
const reviewAction = ref('approve')
const reviewReply = ref('')
const saving = ref(false)

const pendingAppeals = computed(() => allAppealsForMetrics.value.filter((item) => item.status === 'PENDING'))
const handledAppeals = computed(() => allAppealsForMetrics.value.filter((item) => item.status !== 'PENDING'))

async function load() {
  rows.value = await api.get('/admin/violations')
  await loadAppeals()
}

async function loadAppeals() {
  const params = appealStatus.value ? { status: appealStatus.value } : {}
  const [filtered, allRows] = await Promise.all([
    api.get('/admin/violation-appeals', { params }),
    api.get('/admin/violation-appeals')
  ])
  appeals.value = filtered
  allAppealsForMetrics.value = allRows
}

function openReview(row, action) {
  current.value = row
  reviewAction.value = action
  reviewReply.value = ''
  dialogVisible.value = true
}

async function submitReview() {
  if (reviewAction.value === 'reject' && !reviewReply.value.trim()) {
    ElMessage.warning('驳回申诉时请填写处理说明')
    return
  }
  saving.value = true
  try {
    await api.post(`/admin/violation-appeals/${current.value.id}/${reviewAction.value}`, {
      reviewReply: reviewReply.value
    })
    ElMessage.success(reviewAction.value === 'approve' ? '已通过申诉' : '已驳回申诉')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
