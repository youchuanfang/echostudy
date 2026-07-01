<template>
  <div class="es-page">
    <PageHeader page-key="studentNotifications" />

    <AppCard title="消息筛选">
      <div class="toolbar">
        <el-tag type="warning">未读 {{ unreadCount }}</el-tag>
        <el-select v-model="filters.type" clearable placeholder="消息类型" style="width: 180px" @change="load">
          <el-option v-for="type in notificationTypes" :key="type" :label="notificationTypeMap[type]" :value="type" />
        </el-select>
        <el-button type="primary" @click="readAll">全部已读</el-button>
      </div>
    </AppCard>

    <AppCard title="消息列表">
      <div class="message-list">
        <div v-for="item in rows" :key="item.id" class="message-item" :class="{ unread: !item.readStatus }">
          <div class="message-item__head">
            <strong>{{ item.title }}</strong>
            <div>
              <StatusTag :value="item.type" />
              <el-tag :type="item.readStatus ? 'info' : 'danger'" size="small">{{ item.readStatus ? '已读' : '未读' }}</el-tag>
            </div>
          </div>
          <p>{{ item.content }}</p>
          <div class="message-item__foot">
            <span>{{ item.createTime }}</span>
            <el-button v-if="!item.readStatus" size="small" @click="readOne(item)">标记已读</el-button>
          </div>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import { notificationTypeMap } from '../config/statusMap'

const notificationTypes = ['RESERVATION', 'APPROVAL', 'VIOLATION', 'BAN', 'AI_TASK', 'REPAIR', 'ANNOUNCEMENT', 'SYSTEM']
const rows = ref([])
const unreadCount = ref(0)
const filters = reactive({ type: '' })

async function load() {
  rows.value = await api.get('/student/notifications', { params: { type: filters.type || undefined } })
  unreadCount.value = await api.get('/student/notifications/unread-count')
}

async function readOne(item) {
  await api.post(`/student/notifications/${item.id}/read`)
  await load()
}

async function readAll() {
  await api.post('/student/notifications/read-all')
  ElMessage.success('已全部标记为已读')
  await load()
}

onMounted(load)
</script>

<style scoped>
.message-list {
  display: grid;
  gap: 14px;
}

.message-item {
  padding: 16px;
  border: 1px solid var(--es-border);
  border-radius: var(--es-radius-md);
  background: #ffffff;
}

.message-item.unread {
  border-color: #fecdd3;
  background: #fffafa;
}

.message-item__head,
.message-item__foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.message-item p {
  margin: 10px 0;
  color: var(--es-text-secondary);
}

.message-item__foot {
  color: var(--es-text-secondary);
  font-size: 13px;
}
</style>
