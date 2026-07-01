# EchoStudy 校园自习室精准座位预约系统

EchoStudy 是一个前后端分离的校园自习室座位预约系统，支持学生线上预约、管理员线下代预约、座位网格可视化、定位签到、暂离返座、违规自动释放、自动封禁和规则匹配式 AI 自动预约。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring MVC、MyBatis Plus、SQL Server、JWT、Spring Task、Lombok、Knife4j、Maven
- 前端：Vue3、Vite、Element Plus、Axios、Vue Router、localStorage、CSS Grid/Flex
- 数据库：SQL Server，脚本位于 `backend/src/main/resources/db/schema.sql`

## 功能模块

- 认证：学生注册登录、管理员登录、JWT 鉴权
- 学生端：自习室预约、座位网格、我的预约、定位签到、暂离、返座、结束、取消、AI 自动预约、违规记录
- 管理端：后台统计、用户管理、自习室管理、座位管理、时间节点管理、线下代预约、预约记录、违规记录、AI 任务管理
- 自动任务：首次签到超时、暂离超时、预约到期、封禁恢复、AI 任务执行

## 目录结构

```text
EchoStudy
├── backend
│   ├── pom.xml
│   └── src/main
├── frontend
│   ├── package.json
│   └── src
├── docs
│   ├── README.md
│   ├── API.md
│   └── RUN.md
└── README.md
```

## 运行方式

详细步骤见 `docs/RUN.md`。

默认账号：

- 管理员：admin / 123456
- 学生：student / 123456
