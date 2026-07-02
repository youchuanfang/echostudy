<template>
  <div class="es-page">
    <PageHeader page-key="userManage" />

    <AppCard title="用户列表">
      <el-table :data="rows" empty-text="暂无用户">
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="studentNo" label="学号" min-width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ row.role === 'ADMIN' ? '管理员' : '学生' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column label="信用分" width="110">
          <template #default="{ row }">
            <el-tag :type="creditTagType(row.creditScore)">{{ row.creditScore ?? 100 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="violationCount" label="违规次数" width="110" />
        <el-table-column prop="banEndTime" label="封禁结束" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openCredit(row)">调整信用</el-button>
            <el-button size="small" @click="post(row.id, 'ban')">封禁</el-button>
            <el-button size="small" @click="post(row.id, 'unban')">解封</el-button>
            <el-button size="small" @click="post(row.id, 'disable')">禁用</el-button>
            <el-button size="small" @click="post(row.id, 'enable')">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" title="调整信用分" width="460px">
      <el-form label-position="top">
        <el-form-item label="用户">
          <el-input :model-value="current.realName || current.username" disabled />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input-number v-model="creditForm.creditScore" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input
            v-model="creditForm.reason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="例如线下核实后恢复、老师要求人工修正、误操作纠正等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCredit">保存</el-button>
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

const rows = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const current = ref({})
const creditForm = ref({ creditScore: 100, reason: '' })

async function load() {
  rows.value = await api.get('/admin/users')
}

async function post(id, action) {
  await api.post(`/admin/users/${id}/${action}`)
  ElMessage.success('用户状态已更新')
  await load()
}

function openCredit(row) {
  current.value = row
  creditForm.value = { creditScore: row.creditScore ?? 100, reason: '' }
  dialogVisible.value = true
}

async function saveCredit() {
  saving.value = true
  try {
    await api.put(`/admin/users/${current.value.id}/credit-score`, creditForm.value)
    ElMessage.success('信用分已更新')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

function creditTagType(score) {
  const value = score ?? 100
  if (value < 60) return 'danger'
  if (value < 80) return 'warning'
  return 'success'
}

onMounted(load)
</script>
