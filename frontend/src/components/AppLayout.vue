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
          <el-icon size="20"><Bell /></el-icon>
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
      <footer class="es-footer">© 2026 EchoStudy 校园智慧空间资源预约与运营管理系统</footer>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
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
import { getPageConfig } from '../config/themeConfig'

const route = useRoute()
const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))
const config = computed(() => getPageConfig(route.meta.pageKey))
const avatarText = computed(() => (user.value?.realName || user.value?.username || 'E').slice(0, 1))

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
  { path: '/student/violations', label: '违规记录', icon: Warning }
]

const adminMenus = [
  { path: '/admin', label: '后台首页', icon: DataAnalysis },
  { path: '/admin/users', label: '用户管理', icon: User },
  { path: '/admin/rooms', label: '空间资源管理', icon: OfficeBuilding },
  { path: '/admin/seats', label: '座位/工位管理', icon: Grid },
  { path: '/admin/time-nodes', label: '时间节点管理', icon: Clock },
  { path: '/admin/approvals', label: '预约审批管理', icon: Tickets },
  { path: '/admin/offline', label: '线下代预约', icon: Service },
  { path: '/admin/reservations', label: '预约记录管理', icon: List },
  { path: '/admin/repairs', label: '报修管理', icon: Service },
  { path: '/admin/announcements', label: '公告管理', icon: List },
  { path: '/admin/notifications', label: '消息管理', icon: List },
  { path: '/admin/violations', label: '违规记录管理', icon: WarningFilled },
  { path: '/admin/ai', label: 'AI 任务管理', icon: Cpu },
  { path: '/admin/configs', label: '规则配置', icon: Service },
  { path: '/admin/operation-logs', label: '操作日志', icon: List },
  { path: '/admin/statistics', label: '统计分析', icon: DataAnalysis }
]

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>
