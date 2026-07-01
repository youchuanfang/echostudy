import {
  Calendar,
  Clock,
  Cpu,
  DataAnalysis,
  Grid,
  House,
  List,
  OfficeBuilding,
  Service,
  Tickets,
  User,
  Warning,
  WarningFilled
} from '@element-plus/icons-vue'

export const pageConfigs = {
  studentHome: { section: '学生端', icon: House, title: '学生首页', subtitle: '查看今日预约、公告消息和常用入口' },
  spaceReserve: { section: '学生端', icon: Calendar, title: '空间预约', subtitle: '按空间类型、日期和时间选择座位或工位' },
  reservation: { section: '学生端', icon: Calendar, title: '空间预约', subtitle: '按空间类型、日期和时间选择座位或工位' },
  myReservation: { section: '学生端', icon: Tickets, title: '我的预约', subtitle: '查看预约状态，并进行取消、签到、暂离、返座和结束使用' },
  aiTask: { section: '学生端', icon: Cpu, title: 'AI 智能推荐', subtitle: '根据偏好和空间座位情况，自动匹配并创建预约任务' },
  studentRepairs: { section: '学生端', icon: Service, title: '资源报修', subtitle: '提交空间或座位故障报修，查看处理进度' },
  studentNotifications: { section: '学生端', icon: List, title: '我的消息', subtitle: '查看预约、审批、报修和公告等系统消息' },
  studentStatistics: { section: '学生端', icon: DataAnalysis, title: '学习统计', subtitle: '查看学习时长、预约次数和常用空间' },
  violation: { section: '学生端', icon: Warning, title: '违规记录', subtitle: '查看违规原因、累计次数和封禁状态' },
  adminDashboard: { section: '管理端', icon: DataAnalysis, title: '后台首页', subtitle: '查看今日预约、审批、报修和运营概览' },
  userManage: { section: '管理端', icon: User, title: '用户管理', subtitle: '管理学生账号、封禁状态和启用状态' },
  adminRegister: { section: '管理端', icon: User, title: '注册新管理员', subtitle: '创建管理员账号并设置是否允许继续注册管理员' },
  spaceManage: { section: '管理端', icon: OfficeBuilding, title: '空间资源管理', subtitle: '维护校园空间类型、开放状态、审批规则和签到范围' },
  roomManage: { section: '管理端', icon: OfficeBuilding, title: '空间资源管理', subtitle: '维护校园空间类型、开放状态、审批规则和签到范围' },
  seatManage: { section: '管理端', icon: Grid, title: '座位/工位管理', subtitle: '以网格方式管理座位、故障状态和可用状态' },
  timeNodeManage: { section: '管理端', icon: Clock, title: '时间节点管理', subtitle: '维护学生线上预约可选择的开始和结束时间节点' },
  approvalManage: { section: '管理端', icon: Tickets, title: '预约审批管理', subtitle: '处理需要审批的空间预约申请' },
  adminRepairs: { section: '管理端', icon: Service, title: '报修管理', subtitle: '受理、处理和跟踪校园空间资源报修工单' },
  adminAnnouncements: { section: '管理端', icon: List, title: '公告管理', subtitle: '发布、置顶和维护面向学生的系统公告' },
  adminNotifications: { section: '管理端', icon: List, title: '消息管理', subtitle: '查看系统自动生成的全站消息记录' },
  adminConfigs: { section: '管理端', icon: Service, title: '规则配置', subtitle: '维护预约、签到、暂离、封禁、AI、审批和报修规则' },
  adminOperationLogs: { section: '管理端', icon: List, title: '操作日志', subtitle: '查看管理员关键操作审计记录' },
  adminStatistics: { section: '管理端', icon: DataAnalysis, title: '统计分析', subtitle: '查看空间、预约、报修、审批和学习运营数据' },
  offlineReservation: { section: '管理端', icon: Service, title: '线下代预约', subtitle: '工作人员可为已注册学生创建自由时间段预约' },
  reservationManage: { section: '管理端', icon: List, title: '预约记录管理', subtitle: '查询和处理所有预约记录' },
  violationManage: { section: '管理端', icon: WarningFilled, title: '违规记录管理', subtitle: '查看学生违规原因、累计次数和封禁记录' },
  aiTaskManage: { section: '管理端', icon: Cpu, title: 'AI 任务管理', subtitle: '查看全部 AI 智能推荐任务和执行结果' }
}

export function getPageConfig(pageKey) {
  return pageConfigs[pageKey] || pageConfigs.studentHome
}
