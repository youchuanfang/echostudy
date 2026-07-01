# EchoStudy API

所有接口返回统一结构：

```json
{ "code": 200, "message": "success", "data": {} }
```

除登录和注册外，请求需要携带：

```text
Authorization: Bearer <token>
```

## 认证

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## 学生端

- `GET /api/student/rooms`
- `GET /api/student/time-nodes`
- `GET /api/student/seats/layout?roomId=&date=&startTime=&endTime=`
- `POST /api/student/reservations/online`
- `GET /api/student/reservations/my`
- `POST /api/student/reservations/{id}/cancel`
- `POST /api/student/reservations/{id}/sign-in`
- `POST /api/student/reservations/{id}/leave`
- `POST /api/student/reservations/{id}/return`
- `POST /api/student/reservations/{id}/finish`
- `GET /api/student/violations/my`
- `POST /api/student/ai-tasks`
- `GET /api/student/ai-tasks/my`

## 管理员端

- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}`
- `POST /api/admin/users/{id}/ban`
- `POST /api/admin/users/{id}/unban`
- `POST /api/admin/users/{id}/disable`
- `POST /api/admin/users/{id}/enable`
- `GET /api/admin/rooms`
- `POST /api/admin/rooms`
- `PUT /api/admin/rooms/{id}`
- `DELETE /api/admin/rooms/{id}`
- `GET /api/admin/seats?roomId=`
- `POST /api/admin/seats`
- `PUT /api/admin/seats/{id}`
- `DELETE /api/admin/seats/{id}`
- `POST /api/admin/seats/batch-generate`
- `PUT /api/admin/seats/{id}/status`
- `GET /api/admin/time-nodes`
- `POST /api/admin/time-nodes`
- `PUT /api/admin/time-nodes/{id}`
- `DELETE /api/admin/time-nodes/{id}`
- `GET /api/admin/seats/layout?roomId=&date=&startTime=&endTime=`
- `POST /api/admin/reservations/offline`
- `GET /api/admin/reservations`
- `POST /api/admin/reservations/{id}/cancel`
- `POST /api/admin/reservations/{id}/finish`
- `GET /api/admin/violations`
- `GET /api/admin/ai-tasks`
