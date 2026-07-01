import { createRouter, createWebHistory } from 'vue-router'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import StudentDashboard from './views/StudentDashboard.vue'
import StudentReserve from './views/StudentReserve.vue'
import StudentReservations from './views/StudentReservations.vue'
import StudentAi from './views/StudentAi.vue'
import StudentViolations from './views/StudentViolations.vue'
import StudentRepairs from './views/StudentRepairs.vue'
import StudentNotifications from './views/StudentNotifications.vue'
import StudentStatistics from './views/StudentStatistics.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import AdminUsers from './views/AdminUsers.vue'
import AdminRooms from './views/AdminRooms.vue'
import AdminSeats from './views/AdminSeats.vue'
import AdminTimeNodes from './views/AdminTimeNodes.vue'
import AdminOffline from './views/AdminOffline.vue'
import AdminReservations from './views/AdminReservations.vue'
import AdminViolations from './views/AdminViolations.vue'
import AdminAi from './views/AdminAi.vue'
import AdminApprovals from './views/AdminApprovals.vue'
import AdminRepairs from './views/AdminRepairs.vue'
import AdminAnnouncements from './views/AdminAnnouncements.vue'
import AdminNotifications from './views/AdminNotifications.vue'
import AdminConfigs from './views/AdminConfigs.vue'
import AdminOperationLogs from './views/AdminOperationLogs.vue'
import AdminStatistics from './views/AdminStatistics.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/student', component: StudentDashboard, meta: { role: 'STUDENT', pageKey: 'studentHome' } },
  { path: '/student/reserve', component: StudentReserve, meta: { role: 'STUDENT', pageKey: 'spaceReserve' } },
  { path: '/student/reservations', component: StudentReservations, meta: { role: 'STUDENT', pageKey: 'myReservation' } },
  { path: '/student/ai', component: StudentAi, meta: { role: 'STUDENT', pageKey: 'aiTask' } },
  { path: '/student/repairs', component: StudentRepairs, meta: { role: 'STUDENT', pageKey: 'studentRepairs' } },
  { path: '/student/notifications', component: StudentNotifications, meta: { role: 'STUDENT', pageKey: 'studentNotifications' } },
  { path: '/student/statistics', component: StudentStatistics, meta: { role: 'STUDENT', pageKey: 'studentStatistics' } },
  { path: '/student/violations', component: StudentViolations, meta: { role: 'STUDENT', pageKey: 'violation' } },
  { path: '/admin', component: AdminDashboard, meta: { role: 'ADMIN', pageKey: 'adminDashboard' } },
  { path: '/admin/users', component: AdminUsers, meta: { role: 'ADMIN', pageKey: 'userManage' } },
  { path: '/admin/rooms', component: AdminRooms, meta: { role: 'ADMIN', pageKey: 'spaceManage' } },
  { path: '/admin/seats', component: AdminSeats, meta: { role: 'ADMIN', pageKey: 'seatManage' } },
  { path: '/admin/time-nodes', component: AdminTimeNodes, meta: { role: 'ADMIN', pageKey: 'timeNodeManage' } },
  { path: '/admin/approvals', component: AdminApprovals, meta: { role: 'ADMIN', pageKey: 'approvalManage' } },
  { path: '/admin/offline', component: AdminOffline, meta: { role: 'ADMIN', pageKey: 'offlineReservation' } },
  { path: '/admin/reservations', component: AdminReservations, meta: { role: 'ADMIN', pageKey: 'reservationManage' } },
  { path: '/admin/repairs', component: AdminRepairs, meta: { role: 'ADMIN', pageKey: 'adminRepairs' } },
  { path: '/admin/announcements', component: AdminAnnouncements, meta: { role: 'ADMIN', pageKey: 'adminAnnouncements' } },
  { path: '/admin/notifications', component: AdminNotifications, meta: { role: 'ADMIN', pageKey: 'adminNotifications' } },
  { path: '/admin/configs', component: AdminConfigs, meta: { role: 'ADMIN', pageKey: 'adminConfigs' } },
  { path: '/admin/operation-logs', component: AdminOperationLogs, meta: { role: 'ADMIN', pageKey: 'adminOperationLogs' } },
  { path: '/admin/statistics', component: AdminStatistics, meta: { role: 'ADMIN', pageKey: 'adminStatistics' } },
  { path: '/admin/violations', component: AdminViolations, meta: { role: 'ADMIN', pageKey: 'violationManage' } },
  { path: '/admin/ai', component: AdminAi, meta: { role: 'ADMIN', pageKey: 'aiTaskManage' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.role && !user) return '/login'
  if (to.meta.role && user.role !== to.meta.role) return user.role === 'ADMIN' ? '/admin' : '/student'
})

export default router
