<template>
  <div class="es-page">
    <PageHeader page-key="adminAnnouncements" />

    <AppCard title="公告筛选">
      <div class="toolbar">
        <el-select v-model="filters.type" clearable placeholder="公告类型" style="width: 180px" @change="load">
          <el-option v-for="type in announcementTypes" :key="type" :label="announcementTypeMap[type]" :value="type" />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="公告状态" style="width: 160px" @change="load">
          <el-option v-for="status in announcementStatuses" :key="status" :label="announcementStatusMap[status]" :value="status" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增公告</el-button>
      </div>
    </AppCard>

    <AppCard title="公告列表">
      <el-table :data="rows">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="类型" width="120"><template #default="{ row }">{{ announcementTypeMap[row.type] }}</template></el-table-column>
        <el-table-column label="置顶" width="80"><template #default="{ row }">{{ row.pinned ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
        <el-table-column prop="createTime" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="action(row, 'publish')">发布</el-button>
            <el-button size="small" type="warning" @click="action(row, 'disable')">停用</el-button>
            <el-button size="small" @click="action(row, row.pinned ? 'unpin' : 'pin')">{{ row.pinned ? '取消置顶' : '置顶' }}</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '新增公告'" width="680px">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="form.type">
            <el-option v-for="type in announcementTypes" :key="type" :label="announcementTypeMap[type]" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告状态">
          <el-select v-model="form.status">
            <el-option v-for="status in announcementStatuses" :key="status" :label="announcementStatusMap[status]" :value="status" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶"><el-switch v-model="form.pinned" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
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
import { announcementStatusMap, announcementTypeMap } from '../config/statusMap'

const announcementTypes = ['SYSTEM', 'RULE', 'MAINTENANCE', 'SPACE', 'EXAM_WEEK', 'CLOSED']
const announcementStatuses = ['DRAFT', 'PUBLISHED', 'DISABLED']
const rows = ref([])
const dialogVisible = ref(false)
const filters = reactive({ type: '', status: '' })
const form = reactive(defaultForm())

function defaultForm() {
  return { id: null, title: '', content: '', type: 'SYSTEM', pinned: false, status: 'DRAFT' }
}

function resetForm(data = {}) {
  Object.assign(form, defaultForm(), data)
}

async function load() {
  rows.value = await api.get('/admin/announcements', {
    params: { type: filters.type || undefined, status: filters.status || undefined }
  })
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
  if (form.id) {
    await api.put(`/admin/announcements/${form.id}`, form)
  } else {
    await api.post('/admin/announcements', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function action(row, actionName) {
  await api.post(`/admin/announcements/${row.id}/${actionName}`)
  ElMessage.success('操作成功')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”？`)
  await api.delete(`/admin/announcements/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>
