<template>
  <div class="es-page">
    <PageHeader page-key="adminConfigs" />

    <AppCard title="系统规则">
      <el-table :data="rows">
        <el-table-column prop="configKey" label="配置键" min-width="220" />
        <el-table-column label="当前值" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.valueType === 'BOOLEAN'" :type="row.configValue === 'true' ? 'success' : 'info'">
              {{ row.configValue === 'true' ? '启用' : '关闭' }}
            </el-tag>
            <span v-else>{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90"><template #default="{ row }">{{ configValueTypeMap[row.valueType] || row.valueType }}</template></el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" />
        <el-table-column prop="updateAdminName" label="修改人" width="120" />
        <el-table-column prop="updateTime" label="修改时间" min-width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button size="small" type="primary" @click="openEdit(row)">修改</el-button></template>
        </el-table-column>
      </el-table>
    </AppCard>

    <AppCard title="配置修改记录">
      <el-table :data="logs">
        <el-table-column prop="configKey" label="配置键" min-width="220" />
        <el-table-column prop="oldValue" label="旧值" />
        <el-table-column prop="newValue" label="新值" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column prop="createTime" label="时间" min-width="160" />
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" title="修改配置" width="460px">
      <el-form label-position="top">
        <el-form-item label="配置键"><el-input v-model="current.configKey" disabled /></el-form-item>
        <el-form-item label="配置值">
          <el-switch v-if="current.valueType === 'BOOLEAN'" v-model="boolValue" />
          <el-input-number v-else-if="current.valueType === 'NUMBER'" v-model="numberValue" style="width: 100%" />
          <el-input v-else v-model="editValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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

const rows = ref([])
const logs = ref([])
const dialogVisible = ref(false)
const current = ref({})
const editValue = ref('')
const boolValue = ref(false)
const numberValue = ref(0)

async function load() {
  rows.value = await api.get('/admin/configs')
  logs.value = await api.get('/admin/config-logs')
}

function openEdit(row) {
  current.value = row
  editValue.value = row.configValue
  boolValue.value = row.configValue === 'true'
  numberValue.value = Number(row.configValue || 0)
  dialogVisible.value = true
}

async function save() {
  const value = current.value.valueType === 'BOOLEAN'
    ? String(boolValue.value)
    : current.value.valueType === 'NUMBER'
      ? String(numberValue.value)
      : editValue.value
  await api.put(`/admin/configs/${current.value.configKey}`, { configValue: value })
  ElMessage.success('配置已更新')
  dialogVisible.value = false
  await load()
}

onMounted(load)
</script>
