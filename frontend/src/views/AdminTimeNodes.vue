<template>
  <div class="es-page">
    <PageHeader page-key="timeNodeManage" />
    <AppCard title="时间节点表单">
      <div class="form-grid">
        <el-form-item label="时间"><el-time-picker v-model="form.timeValue" value-format="HH:mm:ss" /></el-form-item>
        <el-form-item label="状态"><el-checkbox v-model="form.enabled">启用</el-checkbox></el-form-item>
        <div class="form-action"><el-button type="primary" @click="save">保存</el-button></div>
      </div>
    </AppCard>
    <AppCard title="时间节点列表">
      <el-table :data="rows" @row-click="edit">
        <el-table-column prop="timeValue" label="时间" />
        <el-table-column prop="sortOrder" label="排序" />
        <el-table-column label="启用"><template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template></el-table-column>
        <el-table-column label="操作"><template #default="{ row }"><el-button size="small" type="danger" @click.stop="remove(row.id)">删除</el-button></template></el-table-column>
      </el-table>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'

const rows = ref([])
const form = reactive({ id: null, timeValue: '08:00:00', enabled: true })
async function load() { rows.value = (await api.get('/admin/time-nodes')).sort((a, b) => String(a.timeValue).localeCompare(String(b.timeValue))) }
function edit(row) { Object.assign(form, row) }
async function save() { form.id ? await api.put(`/admin/time-nodes/${form.id}`, form) : await api.post('/admin/time-nodes', form); Object.assign(form, { id: null, timeValue: '08:00:00', enabled: true }); await load() }
async function remove(id) { await api.delete(`/admin/time-nodes/${id}`); await load() }
onMounted(load)
</script>
