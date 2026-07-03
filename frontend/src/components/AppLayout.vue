<template>
  <div class="es-layout">
    <aside class="es-sidebar">
      <div class="es-brand">
        <el-icon><Reading /></el-icon>
        <span>EchoStudy</span>
      </div>
      <nav class="es-menu">
        <div class="es-menu-group">{{ roleTitle }}</div>
        <div
          v-for="item in visibleMenus"
          :key="item.path"
          class="es-menu-item"
          :class="{ 'is-active': isMenuActive(item.path) }"
          @click="router.push(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </div>
      </nav>
      <div class="es-sidebar-illustration"></div>
    </aside>
    <main class="es-main">
      <header class="es-topbar">
        <div class="es-breadcrumb">
          <el-icon><Menu /></el-icon>
          <span>{{ roleTitle }}</span>
          <span>/</span>
          <span>{{ config.title }}</span>
        </div>
        <div class="es-topbar-actions">
          <button class="notification-button" type="button" title="查看消息" @click="openNotifications">
            <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
              <el-icon size="20"><Bell /></el-icon>
            </el-badge>
          </button>
          <div class="es-user-badge">
            <span class="es-avatar">{{ avatarText }}</span>
            <span>{{ user?.realName || user?.username }}</span>
            <span class="es-role-pill">{{ user?.role === 'ADMIN' ? '管理员' : '学生' }}</span>
          </div>
          <el-button plain type="primary" @click="logout">退出登录</el-button>
        </div>
      </header>
      <section class="es-content">
        <slot />
      </section>
      <footer class="es-footer">© 2026 EchoStudy 校园学习空间运营与信用治理平台</footer>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Calendar,
  Clock,
  Cpu,
  DataAnalysis,
  Grid,
  House,
  List,
  Menu,
  OfficeBuilding,
  Reading,
  Service,
  Tickets,
  User,
  Warning,
  WarningFilled
} from '@element-plus/icons-vue'
import api from '../api'
import { getPageConfig } from '../config/themeConfig'

const route = useRoute()
const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const config = computed(() => getPageConfig(route.meta.pageKey))
const avatarText = computed(() => (user.value?.realName || user.value?.username || 'E').slice(0, 1))
const unreadCount = ref(0)
let unreadTimer = null

const currentRole = computed(() => {
  const role = user.value?.role || localStorage.getItem('role') || ''
  if (role) return role.toUpperCase()
  return route.path.startsWith('/admin') ? 'ADMIN' : 'STUDENT'
})

const roleTitle = computed(() => (currentRole.value === 'ADMIN' ? '管理端' : '学生端'))
const visibleMenus = computed(() => (currentRole.value === 'ADMIN' ? adminMenus : studentMenus))

function isMenuActive(path) {
  if (route.path === path) return true
  if (path === '/admin' || path === '/student') return route.path === path
  return route.path.startsWith(path + '/')
}

const studentMenus = [
  { path: '/student', label: '学生首页', icon: House },
  { path: '/student/reserve', label: '空间预约', icon: Calendar },
  { path: '/student/reservations', label: '我的预约', icon: Tickets },
  { path: '/student/ai', label: 'AI 智能推荐', icon: Cpu },
  { path: '/student/repairs', label: '资源报修', icon: Service },
  { path: '/student/notifications', label: '我的消息', icon: List },
  { path: '/student/statistics', label: '学习统计', icon: DataAnalysis },
  { path: '/student/violations', label: '信用与违规', icon: Warning }
]

const adminMenus = [
  { path: '/admin', label: '后台首页', icon: DataAnalysis },
  { path: '/admin/users', label: '用户与信用', icon: User },
  { path: '/admin/register-admin', label: '注册新管理员', icon: User },
  { path: '/admin/rooms', label: '空间资源', icon: OfficeBuilding },
  { path: '/admin/seats', label: '座位/工位', icon: Grid },
  { path: '/admin/time-nodes', label: '时间节点', icon: Clock },
  { path: '/admin/approvals', label: '预约审批', icon: Tickets },
  { path: '/admin/offline', label: '线下代预约', icon: Service },
  { path: '/admin/reservations', label: '预约记录', icon: List },
  { path: '/admin/repairs', label: '报修管理', icon: Service },
  { path: '/admin/announcements', label: '公告管理', icon: List },
  { path: '/admin/notifications', label: '消息管理', icon: List },
  { path: '/admin/violations', label: '信用治理', icon: WarningFilled },
  { path: '/admin/ai', label: 'AI 任务', icon: Cpu },
  { path: '/admin/configs', label: '规则配置', icon: Service },
  { path: '/admin/operation-logs', label: '操作日志', icon: List },
  { path: '/admin/statistics', label: '统计分析', icon: DataAnalysis }
]

async function loadUnreadCount() {
  if (!user.value) {
    unreadCount.value = 0
    return
  }
  if (currentRole.value === 'ADMIN') {
    const rows = await api.get('/admin/notifications', { params: { readStatus: false } })
    unreadCount.value = Array.isArray(rows) ? rows.length : 0
    return
  }
  unreadCount.value = await api.get('/student/notifications/unread-count')
}

function openNotifications() {
  router.push(currentRole.value === 'ADMIN' ? '/admin/notifications' : '/student/notifications')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  unreadCount.value = 0
  router.push('/login')
}

onMounted(() => {
  loadUnreadCount().catch(() => {})
  unreadTimer = window.setInterval(() => {
    loadUnreadCount().catch(() => {})
  }, 30000)
})

onUnmounted(() => {
  if (unreadTimer) {
    window.clearInterval(unreadTimer)
  }
})

watch(() => route.fullPath, () => {
  loadUnreadCount().catch(() => {})
})
</script>

<style scoped>
.notification-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  color: var(--es-text-secondary);
  background: transparent;
  cursor: pointer;
}

.notification-button:hover {
  color: var(--es-primary);
  background: #eef8f6;
}
</style>
