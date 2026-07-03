<template>
  <div class="es-page">
    <PageHeader page-key="adminConfigs" />

    <AppCard title="系统规则">
      <el-table :data="rows" empty-text="暂无配置">
        <el-table-column label="规则" min-width="230">
          <template #default="{ row }">
            <div class="rule-name">{{ ruleMeta(row.configKey).name }}</div>
            <div class="rule-key">{{ row.configKey }}</div>
          </template>
        </el-table-column>
        <el-table-column label="当前设置" min-width="180">
          <template #default="{ row }">
            <el-tag v-if="row.valueType === 'BOOLEAN'" :type="row.configValue === 'true' ? 'success' : 'info'">
              {{ row.configValue === 'true' ? '启用' : '关闭' }}
            </el-tag>
            <span v-else>{{ valueText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ configValueTypeMap[row.valueType] || row.valueType }}</template>
        </el-table-column>
        <el-table-column label="说明" min-width="330">
          <template #default="{ row }">{{ ruleMeta(row.configKey).description }}</template>
        </el-table-column>
        <el-table-column prop="updateAdminName" label="修改人" width="120" />
        <el-table-column prop="updateTime" label="修改时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="openEdit(row)">修改</el-button></template>
        </el-table-column>
      </el-table>
    </AppCard>

    <AppCard title="配置修改记录">
      <el-table :data="logs" empty-text="暂无修改记录">
        <el-table-column label="规则" min-width="230">
          <template #default="{ row }">{{ ruleMeta(row.configKey).name }}</template>
        </el-table-column>
        <el-table-column label="旧值">
          <template #default="{ row }">{{ logValueText(row.configKey, row.oldValue) }}</template>
        </el-table-column>
        <el-table-column label="新值">
          <template #default="{ row }">{{ logValueText(row.configKey, row.newValue) }}</template>
        </el-table-column>
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" title="修改规则" width="460px">
      <el-form label-position="top">
        <el-form-item label="规则">
          <el-input :model-value="ruleMeta(current.configKey).name" disabled />
        </el-form-item>
        <el-form-item label="说明">
          <el-input :model-value="ruleMeta(current.configKey).description" disabled />
        </el-form-item>
        <el-form-item :label="`设置值${ruleMeta(current.configKey).unit ? '（' + ruleMeta(current.configKey).unit + '）' : ''}`">
          <el-switch v-if="current.valueType === 'BOOLEAN'" v-model="boolValue" active-text="启用" inactive-text="关闭" />
          <el-input-number
            v-else-if="current.valueType === 'NUMBER'"
            v-model="numberValue"
            :min="0"
            :step="ruleMeta(current.configKey).storedAsMinutes ? 0.5 : 1"
            style="width: 100%"
          />
          <el-input v-else v-model="editValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
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
import { configValueTypeMap } from '../config/statusMap'

const configMeta = {
  ai_task_enabled: { name: 'AI 自动预约', description: '是否允许学生创建 AI 自动预约任务' },
  approval_enabled: { name: '预约审批流程', description: '是否启用需要审批空间的管理员审批流程' },
  ban_days: { name: '封禁天数', description: '学生被封禁后持续多少天', unit: '天' },
  ban_threshold: { name: '封禁违规次数', description: '学生累计达到多少次违规后触发封禁', unit: '次' },
  default_location_radius_meter: { name: '默认签到范围', description: '新建空间默认允许签到的定位半径', unit: '米' },
  first_sign_deadline_minutes: { name: '首次签到期限', description: '预约开始后，学生需要在多少分钟内完成首次签到', unit: '分钟' },
  grace_minutes: { name: '超时宽限时间', description: '系统处理超时释放或结束前保留的宽限分钟数', unit: '分钟' },
  leave_max_minutes: { name: '暂离最长时间', description: '学生单次暂离座位允许的最长分钟数', unit: '分钟' },
  location_check_enabled: { name: '定位签到校验', description: '签到时是否校验学生当前位置在空间允许范围内' },
  online_max_duration_enabled: { name: '线上预约时长限制', description: '是否限制学生线上一次预约的最长时长' },
  online_max_duration_minutes: { name: '线上一次预约最长时长', description: '学生线上一次预约最多可预约多少小时；保存时系统会换算为分钟', unit: '小时', storedAsMinutes: true },
  repair_enabled: { name: '报修流程', description: '是否允许学生提交空间或座位/工位报修' },
  credit_initial_score: { name: '初始信用分', description: '新学生账号默认信用分', unit: '分' },
  credit_max_score: { name: '信用分上限', description: '申诉通过或人工调整时可恢复到的最高分', unit: '分' },
  credit_min_score: { name: '信用分下限', description: '系统自动扣分后的最低信用分', unit: '分' },
  credit_low_threshold: { name: '低信用预约阈值', description: '信用分低于该值时限制继续预约', unit: '分' },
  credit_first_sign_timeout_deduct: { name: '未按时签到扣分', description: '首次签到超时生成违规时扣除的信用分；管理员可在这里修改后续扣分值', unit: '分' },
  credit_leave_return_timeout_deduct: { name: '暂离未返扣分', description: '暂离超时未返座生成违规时扣除的信用分；管理员可在这里修改后续扣分值', unit: '分' },
  low_credit_reservation_block_enabled: { name: '低信用限制预约', description: '是否在学生信用分低于阈值时阻止创建新预约' },
  violation_appeal_enabled: { name: '违规申诉流程', description: '是否允许学生对违规记录提交申诉' }
}

const rows = ref([])
const logs = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const current = ref({})
const editValue = ref('')
const boolValue = ref(false)
const numberValue = ref(0)

async function load() {
  rows.value = await api.get('/admin/configs')
  logs.value = await api.get('/admin/config-logs')
}

function ruleMeta(key) {
  return configMeta[key] || { name: key || '-', description: '系统规则配置' }
}

function valueText(row) {
  return formatValue(row.configKey, row.configValue)
}

function logValueText(key, value) {
  return formatValue(key, value)
}

function formatValue(key, value) {
  if (value === null || value === undefined || value === '') return '-'
  if (value === 'true') return '启用'
  if (value === 'false') return '关闭'
  const meta = ruleMeta(key)
  const number = Number(value)
  if (meta.storedAsMinutes && Number.isFinite(number)) {
    return `${trimNumber(number / 60)} 小时`
  }
  if (!meta.unit) return value
  return `${value} ${meta.unit}`
}

function openEdit(row) {
  current.value = row
  editValue.value = row.configValue
  boolValue.value = row.configValue === 'true'
  const meta = ruleMeta(row.configKey)
  const rawNumber = Number(row.configValue || 0)
  numberValue.value = meta.storedAsMinutes ? rawNumber / 60 : rawNumber
  dialogVisible.value = true
}

async function save() {
  const meta = ruleMeta(current.value.configKey)
  const value = current.value.valueType === 'BOOLEAN'
    ? String(boolValue.value)
    : current.value.valueType === 'NUMBER'
      ? String(meta.storedAsMinutes ? Math.round(Number(numberValue.value || 0) * 60) : numberValue.value)
      : editValue.value
  saving.value = true
  try {
    await api.put(`/admin/configs/${current.value.configKey}`, { configValue: value })
    ElMessage.success('配置已更新')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function trimNumber(value) {
  return Number.isInteger(value) ? String(value) : value.toFixed(2).replace(/\.?0+$/, '')
}

onMounted(load)
</script>

<style scoped>
.rule-name {
  font-weight: 800;
  color: var(--es-text-main);
}

.rule-key {
  margin-top: 4px;
  color: var(--es-text-secondary);
  font-size: 12px;
}
</style>
