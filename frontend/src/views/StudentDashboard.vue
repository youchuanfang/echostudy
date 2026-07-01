<template>
  <div class="es-page">
    <PageHeader page-key="studentHome" />

    <AppCard title="账号状态">
      <el-descriptions :column="4" border>
        <el-descriptions-item label="账号">{{ me.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ me.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><StatusTag :value="me.status" /></el-descriptions-item>
        <el-descriptions-item label="违规次数">{{ me.violationCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="封禁结束">{{ me.banEndTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="未读消息">{{ unreadCount }}</el-descriptions-item>
      </el-descriptions>
    </AppCard>

    <AppCard title="快捷入口">
      <div class="quick-grid">
        <el-button type="primary" @click="router.push('/student/reserve')">空间预约</el-button>
        <el-button @click="router.push('/student/reservations')">我的预约</el-button>
        <el-button @click="router.push('/student/ai')">AI 智能推荐</el-button>
        <el-button @click="router.push('/student/repairs')">资源报修</el-button>
        <el-button @click="router.push('/student/notifications')">我的消息</el-button>
      </div>
    </AppCard>

    <AppCard title="今日预约">
      <el-table :data="today">
        <el-table-column prop="roomName" label="空间" />
        <el-table-column prop="reserveDate" label="日期" />
        <el-table-column label="时间段"><template #default="{ row }">{{ timeText(row.startTime) }} - {{ timeText(row.endTime) }}</template></el-table-column>
        <el-table-column label="状态"><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column>
      </el-table>
    </AppCard>

    <AppCard title="最新公告">
      <el-table :data="announcements">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="置顶" width="80"><template #default="{ row }">{{ row.pinned ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="createTime" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="110"><template #default="{ row }"><el-button size="small" @click="showAnnouncement(row)">查看</el-button></template></el-table-column>
      </el-table>
    </AppCard>

    <el-dialog v-model="announcementDialog" title="公告详情" width="620px">
      <h3>{{ currentAnnouncement?.title }}</h3>
      <p class="announcement-content">{{ currentAnnouncement?.content }}</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import AppCard from '../components/AppCard.vue'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'

const router = useRouter()
const me = ref({})
const reservations = ref([])
const announcements = ref([])
const unreadCount = ref(0)
const announcementDialog = ref(false)
const currentAnnouncement = ref(null)
const today = computed(() => reservations.value.filter((r) => r.reserveDate === new Date().toISOString().slice(0, 10)))

function timeText(value) {
  return String(value || '').slice(0, 5)
}

function showAnnouncement(row) {
  currentAnnouncement.value = row
  announcementDialog.value = true
}

onMounted(async () => {
  me.value = await api.get('/auth/me')
  const [reservationRows, announcementRows, unread] = await Promise.all([
    api.get('/student/reservations/my'),
    api.get('/student/announcements'),
    api.get('/student/notifications/unread-count')
  ])
  reservations.value = reservationRows
  announcements.value = announcementRows.slice(0, 6)
  unreadCount.value = unread
})
</script>

<style scoped>
.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.announcement-content {
  white-space: pre-wrap;
  color: var(--es-text-secondary);
  line-height: 1.8;
}
</style>
