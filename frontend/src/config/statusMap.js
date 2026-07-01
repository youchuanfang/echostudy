export const statusMap = {
  PENDING: { text: '待处理', type: 'warning' },
  SUCCESS: { text: '执行成功', type: 'success' },
  FAILED: { text: '执行失败', type: 'danger' },
  PENDING_APPROVAL: { text: '待审批', type: 'warning' },
  RESERVED: { text: '已预约', type: 'blue' },
  USING: { text: '使用中', type: 'success' },
  LEAVE: { text: '暂离中', type: 'warning' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'info' },
  VIOLATED: { text: '已违规', type: 'danger' },
  REJECTED: { text: '已驳回', type: 'danger' },
  EXPIRED: { text: '已过期', type: 'info' },
  ACCEPTED: { text: '已受理', type: 'blue' },
  PROCESSING: { text: '处理中', type: 'purple' },
  DONE: { text: '已完成', type: 'success' },
  DRAFT: { text: '草稿', type: 'info' },
  PUBLISHED: { text: '已发布', type: 'success' },
  DISABLED: { text: '已停用', type: 'info' },
  NORMAL: { text: '正常', type: 'success' },
  BANNED: { text: '封禁中', type: 'danger' },
  AVAILABLE: { text: '可预约', type: 'success' },
  MINE: { text: '我的预约', type: 'purple' },
  FAULTY: { text: '故障', type: 'danger' },
  STUDY_ROOM: { text: '自习室', type: 'blue' },
  SEMINAR_ROOM: { text: '研讨室', type: 'purple' },
  CLASSROOM: { text: '空教室', type: 'success' },
  LAB_SEAT: { text: '实验室工位', type: 'warning' },
  PUBLIC_AREA: { text: '公共学习区', type: 'info' },
  SPACE: { text: '空间级报修', type: 'blue' },
  SEAT: { text: '座位/工位级报修', type: 'purple' },
  SOCKET: { text: '插座故障', type: 'warning' },
  CHAIR: { text: '座椅损坏', type: 'warning' },
  DESK: { text: '桌面损坏', type: 'warning' },
  LIGHT: { text: '照明问题', type: 'warning' },
  AIR_CONDITIONER: { text: '空调问题', type: 'warning' },
  NETWORK: { text: '网络问题', type: 'warning' },
  ACCESS_CONTROL: { text: '门禁问题', type: 'warning' },
  HYGIENE: { text: '卫生问题', type: 'warning' },
  OTHER: { text: '其他问题', type: 'info' },
  SYSTEM: { text: '系统消息', type: 'info' },
  RULE: { text: '规则公告', type: 'blue' },
  MAINTENANCE: { text: '维修公告', type: 'warning' },
  EXAM_WEEK: { text: '考试周公告', type: 'purple' },
  CLOSED: { text: '临时关闭公告', type: 'danger' },
  RESERVATION: { text: '预约消息', type: 'blue' },
  APPROVAL: { text: '审批消息', type: 'warning' },
  VIOLATION: { text: '违规消息', type: 'danger' },
  BAN: { text: '封禁消息', type: 'danger' },
  AI_TASK: { text: 'AI任务消息', type: 'purple' },
  REPAIR: { text: '报修消息', type: 'warning' },
  ANNOUNCEMENT: { text: '公告消息', type: 'success' }
}

export const reservationStatusMap = {
  PENDING_APPROVAL: '待审批',
  RESERVED: '已预约',
  USING: '使用中',
  LEAVE: '暂离中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  VIOLATED: '已违规',
  REJECTED: '已驳回',
  EXPIRED: '已过期'
}

export const repairStatusMap = {
  PENDING: '待处理',
  ACCEPTED: '已受理',
  PROCESSING: '处理中',
  DONE: '已完成',
  REJECTED: '已驳回',
  CANCELLED: '已取消'
}

export const repairLevelMap = {
  SPACE: '空间级报修',
  SEAT: '座位/工位级报修'
}

export const repairTypeMap = {
  SOCKET: '插座故障',
  CHAIR: '座椅损坏',
  DESK: '桌面损坏',
  LIGHT: '照明问题',
  AIR_CONDITIONER: '空调问题',
  NETWORK: '网络问题',
  ACCESS_CONTROL: '门禁问题',
  HYGIENE: '卫生问题',
  OTHER: '其他问题'
}

export const announcementStatusMap = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  DISABLED: '已停用'
}

export const announcementTypeMap = {
  SYSTEM: '系统公告',
  RULE: '规则公告',
  MAINTENANCE: '维修公告',
  SPACE: '空间公告',
  EXAM_WEEK: '考试周公告',
  CLOSED: '临时关闭公告'
}

export const spaceTypeMap = {
  STUDY_ROOM: '自习室',
  SEMINAR_ROOM: '研讨室',
  CLASSROOM: '空教室',
  LAB_SEAT: '实验室工位',
  PUBLIC_AREA: '公共学习区'
}

export const notificationTypeMap = {
  RESERVATION: '预约消息',
  APPROVAL: '审批消息',
  VIOLATION: '违规消息',
  BAN: '封禁消息',
  AI_TASK: 'AI任务消息',
  REPAIR: '报修消息',
  ANNOUNCEMENT: '公告消息',
  SYSTEM: '系统消息'
}

export const configValueTypeMap = {
  STRING: '文本',
  NUMBER: '数字',
  BOOLEAN: '开关'
}

export const operationTypeMap = {
  新增空间: '新增空间',
  修改空间: '修改空间',
  删除空间: '删除空间',
  开放空间: '开放空间',
  关闭空间: '关闭空间',
  审批预约: '审批预约',
  驳回预约: '驳回预约',
  处理报修: '处理报修',
  发布公告: '发布公告',
  停用公告: '停用公告',
  修改系统规则: '修改系统规则'
}

export function getStatusMeta(value) {
  return statusMap[value] || { text: value || '-', type: 'info' }
}

export function statusText(value) {
  return getStatusMeta(value).text
}

export const violationTypeMap = {
  FIRST_SIGN_TIMEOUT: '首次签到超时',
  LEAVE_RETURN_TIMEOUT: '暂离未按时返座',
  AI_FIRST_SIGN_TIMEOUT: 'AI 预约首次签到超时',
  AI_LEAVE_RETURN_TIMEOUT: 'AI 预约暂离超时'
}

export function violationTypeText(value) {
  return violationTypeMap[value] || value || '-'
}
