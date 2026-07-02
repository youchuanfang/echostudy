<template>
  <div class="es-page">
    <PageHeader page-key="violation" />

    <div class="metric-grid">
      <div class="metric-card">
        <h3>当前信用分</h3>
        <strong>{{ profile?.creditScore ?? '-' }}</strong>
      </div>
      <div class="metric-card">
        <h3>累计违规次数</h3>
        <strong>{{ profile?.violationCount ?? 0 }}</strong>
      </div>
      <div class="metric-card">
        <h3>账号状态</h3>
        <StatusTag :value="profile?.status" />
      </div>
    </div>

    <AppCard title="我的违规记录">
      <el-table :data="rows" empty-text="暂无违规记录">
        <el-table-column prop="createTime" label="时间" min-width="160" />
        <el-table-column label="类型" min-width="150">
          <template #default="{ row }">{{ violationTypeText(row.violationType) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="260" show-overflow-tooltip />
        <el-table-column prop="creditDeductPoints" label="扣分" width="90" />
        <el-table-column prop="violationCountSnapshot" label="累计次数" width="110" />
        <el-table-column label="申诉状态" width="120">
          <template #default="{ row }">
            <StatusTag v-if="row.appealStatus" :value="row.appealStatus" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="Boolean(row.appealId)"
              @click="openAppeal(row)"
            >
              {{ row.appealId ? '已申诉' : '提交申诉' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <AppCard title="申诉记录">
      <el-table :data="appeals" empty-text="暂无申诉记录">
        <el-table-column prop="createTime" label="提交时间" min-width="160" />
        <el-table-column prop="violationId" label="违规ID" width="100" />
        <el-table-column prop="reason" label="申诉说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column prop="reviewReply" label="处理说明" min-width="240" show-overflow-tooltip />
        <el-table-column prop="reviewTime" label="处理时间" min-width="160" />
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" title="提交违规申诉" width="520px">
      <el-form label-position="top">
        <el-form-item label="违规类型">
          <el-input :model-value="violationTypeText(current.violationType)" disabled />
        </el-form-item>
        <el-form-item label="申诉说明">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请说明为什么认为该违规记录需要复核，例如已到场但定位失败、设备异常、管理员线下确认等"
          />
        </el-form-item>
        <el-form-item label="补充材料">
          <el-input
            v-model="form.evidence"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="可填写截图说明、线下沟通记录、现场情况等；没有材料可留空"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { violationTypeText } from '../config/statusMap'

const rows = ref([])
const appeals = ref([])
const profile = ref(null)
const dialogVisible = ref(false)
const saving = ref(false)
const current = ref({})
const form = ref({ reason: '', evidence: '' })

async function load() {
  const [me, violations, appealRows] = await Promise.all([
    api.get('/auth/me'),
    api.get('/student/violations/my'),
    api.get('/student/violation-appeals/my')
  ])
  profile.value = me
  localStorage.setItem('user', JSON.stringify(me))
  rows.value = violations
  appeals.value = appealRows
}

function openAppeal(row) {
  current.value = row
  form.value = { reason: '', evidence: '' }
  dialogVisible.value = true
}

async function submitAppeal() {
  if (!form.value.reason.trim()) {
    ElMessage.warning('请填写申诉说明')
    return
  }
  saving.value = true
  try {
    await api.post(`/student/violations/${current.value.id}/appeal`, {
      reason: form.value.reason,
      evidence: form.value.evidence
    })
    ElMessage.success('申诉已提交，请等待管理员处理')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
