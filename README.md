# EchoStudy 校园自习空间预约系统

EchoStudy 是一个前后端分离的校园自习空间预约系统，面向学生和管理员两类用户，覆盖空间检索、座位/工位预约、定位签到、暂离返座、审批、线下代预约、违规封禁、报修、公告通知、规则配置和运营统计等流程。

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat-square)
![Vue](https://img.shields.io/badge/Vue-3.x-42b883?style=flat-square)
![Vite](https://img.shields.io/badge/Vite-5.x-646CFF?style=flat-square)
![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-CC2927?style=flat-square)

> [!NOTE]
> 本 README 以课程项目说明为目标：让没有读过代码的人先理解系统功能、模块边界、关键接口和运行方式。更细的接口参数、截图材料和报告正文可在此基础上继续补充。

## 项目定位

校园自习空间常见问题包括座位占用不透明、预约冲突难处理、签到和暂离规则难执行、资源故障反馈分散、管理员缺少运营统计。EchoStudy 将这些流程拆成学生端和管理端两个工作台：

- 学生端关注“找空间、约座位、签到使用、查看消息和记录”。
- 管理端关注“维护资源、审批预约、处理异常、配置规则和查看统计”。
- 后端使用统一 REST API、JWT 鉴权、定时任务和 SQL Server 持久化来串联业务。

## 功能模块

| 模块 | 使用角色 | 已实现功能 | 主要代码证据 |
| --- | --- | --- | --- |
| 认证与账号 | 学生、管理员 | 学生注册、管理员登录、管理员创建新管理员、按用户名/手机号/学号登录、预留验证码式找回密码 | `AuthController`, `AuthServiceImpl`, `JwtInterceptor` |
| 空间资源 | 学生、管理员 | 楼栋筛选、空间列表、空间详情、空间类型、开放/关闭、座位/工位维护、批量生成座位 | `StudentResourceController`, `AdminSpaceController`, `AdminRoomController`, `AdminSeatController` |
| 在线预约 | 学生 | 按日期、空间、时间段查看座位网格，创建线上预约，查看个人预约 | `StudentReservationController`, `ReservationServiceImpl` |
| 预约生命周期 | 学生、管理员 | 取消、定位签到、暂离、返座、结束；管理员可取消或结束预约 | `ReservationServiceImpl`, `LocationUtils` |
| 审批与线下代预约 | 管理员 | 审批需要审核的空间预约，为学生创建线下预约，校验资源和用户时间冲突 | `AdminApprovalController`, `AdminReservationController` |
| 违规与封禁 | 学生、管理员 | 首次签到超时、暂离未返、预约结束未签到等违规记录；达到阈值后封禁，到期恢复 | `ViolationController`, `ViolationServiceImpl`, `ReservationMaintenanceTask` |
| 规则评分式自动预约 | 学生、管理员 | 学生提交偏好任务；系统按偏好空间、插座、靠窗等规则为可用座位打分并尝试预约 | `AiReservationController`, `AiReservationServiceImpl.score` |
| 报修管理 | 学生、管理员 | 学生提交空间/座位报修，管理员受理、处理中、驳回、完成，并可联动座位故障或空间开放状态 | `StudentRepairController`, `AdminRepairController`, `RepairServiceImpl` |
| 公告与消息 | 学生、管理员 | 管理员发布、置顶、停用公告；学生查看公告和系统消息，支持已读/全部已读 | `AdminAnnouncementController`, `StudentAnnouncementController`, `NotificationController` |
| 系统配置与日志 | 管理员 | 维护签到、暂离、封禁、审批、报修、自动预约等规则，记录配置变更和关键操作 | `AdminConfigController`, `AdminOperationLogController`, `ConfigServiceImpl` |
| 统计分析 | 学生、管理员 | 学生学习统计；管理员查看空间、报修、审批、学习排行等运营统计 | `StudentStatisticsController`, `AdminStatisticsController`, `AdminDashboardController` |

## 业务流程

```mermaid
flowchart LR
  A["学生登录/注册"] --> B["筛选楼栋与空间"]
  B --> C{"空间是否按座位/工位预约"}
  C -- 是 --> D["查看座位网格并选择座位"]
  C -- 否 --> E["填写整间空间预约信息"]
  D --> F{"是否需要审批"}
  E --> F
  F -- 否 --> G["生成预约"]
  F -- 是 --> H["管理员审批"]
  H -- 通过 --> G
  H -- 驳回 --> I["学生收到审批消息"]
  G --> J["定位签到"]
  J --> K["使用中/暂离/返座/结束"]
  K --> L["统计与记录沉淀"]
  K --> M["超时定时任务处理违规"]
```

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.3.5, Spring MVC, MyBatis Plus 3.5.9, JWT, Spring Task, Lombok, Knife4j 4.5.0, Maven |
| 前端 | Vue 3, Vite 5, Vue Router, Element Plus, Axios |
| 数据库 | SQL Server |
| 接口文档 | Knife4j / OpenAPI，后端启动后访问 `http://localhost:8080/doc.html` |

## 系统结构

```text
EchoStudy/
├─ backend/                         # Spring Boot 后端
│  ├─ pom.xml                       # Maven 依赖与 Java 版本
│  └─ src/main/
│     ├─ java/com/echostudy/
│     │  ├─ controller/             # REST API 控制器
│     │  ├─ service/impl/           # 核心业务实现
│     │  ├─ mapper/                 # MyBatis Plus Mapper
│     │  ├─ entity/                 # 数据表实体
│     │  ├─ dto/                    # 请求对象
│     │  ├─ vo/                     # 响应对象
│     │  ├─ security/               # JWT、拦截器、用户上下文
│     │  ├─ task/                   # 定时维护任务
│     │  └─ config/                 # Web、预约规则和初始化配置
│     └─ resources/
│        ├─ application.yml         # 本地后端配置，请按本机数据库修改
│        └─ db/                     # SQL Server 建表、升级和种子数据脚本
├─ frontend/                        # Vue 3 + Vite 前端
│  ├─ package.json                  # npm 脚本和前端依赖
│  └─ src/
│     ├─ views/                     # 登录、学生端、管理端页面
│     ├─ components/                # 布局、卡片、状态标签等组件
│     ├─ config/                    # 页面标题、菜单、状态映射
│     └─ styles/                    # 主题、布局和组件样式
└─ docs/                            # 旧版运行说明、API 摘要和阶段记录
```

## 关键接口

后端接口统一返回：

```json
{ "code": 200, "message": "success", "data": {} }
```

除登录、学生注册、找回密码验证码和重置密码外，业务接口需要携带：

```text
Authorization: Bearer <token>
```

### 认证接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/auth/register` | 学生注册 |
| POST | `/api/auth/admin/register` | 有权限的管理员创建新管理员 |
| POST | `/api/auth/login` | 学生或管理员登录 |
| POST | `/api/auth/forgot-password/code` | 预留验证码式找回密码，当前返回测试验证码说明 |
| POST | `/api/auth/forgot-password/reset` | 使用验证码重置密码 |
| GET | `/api/auth/me` | 获取当前登录用户 |

### 学生端关键接口

| 模块 | 方法与路径 | 用途 |
| --- | --- | --- |
| 空间资源 | `GET /api/student/buildings` | 获取可筛选楼栋 |
| 空间资源 | `GET /api/student/spaces?building=` | 查询空间列表 |
| 空间资源 | `GET /api/student/spaces/{id}` | 查看空间详情 |
| 空间资源 | `GET /api/student/time-nodes` | 获取可预约时间节点 |
| 座位网格 | `GET /api/student/seats/layout?roomId=&date=&startTime=&endTime=` | 查看指定时间段座位占用 |
| 在线预约 | `POST /api/student/reservations/online` | 创建线上预约 |
| 我的预约 | `GET /api/student/reservations/my` | 查看个人预约 |
| 预约操作 | `POST /api/student/reservations/{id}/cancel` | 取消预约 |
| 预约操作 | `POST /api/student/reservations/{id}/sign-in` | 定位签到 |
| 预约操作 | `POST /api/student/reservations/{id}/leave` | 暂离 |
| 预约操作 | `POST /api/student/reservations/{id}/return` | 返座 |
| 预约操作 | `POST /api/student/reservations/{id}/finish` | 结束预约 |
| 自动预约 | `POST /api/student/ai-tasks` | 创建规则评分式自动预约任务 |
| 自动预约 | `GET /api/student/ai-tasks/my` | 查看我的自动预约任务 |
| 报修 | `POST /api/student/repairs` | 提交报修 |
| 报修 | `GET /api/student/repairs/my` | 查看我的报修 |
| 消息 | `GET /api/student/announcements` | 查看公告 |
| 消息 | `GET /api/student/notifications` | 查看消息 |
| 统计 | `GET /api/student/statistics/learning` | 查看学习统计 |
| 违规 | `GET /api/student/violations/my` | 查看我的违规记录 |

### 管理端关键接口

| 模块 | 方法与路径 | 用途 |
| --- | --- | --- |
| 首页 | `GET /api/admin/dashboard` | 后台概览数据 |
| 用户 | `GET /api/admin/users` | 用户列表 |
| 用户 | `PUT /api/admin/users/{id}` | 更新用户资料 |
| 用户 | `POST /api/admin/users/{id}/ban` / `unban` | 封禁或解封 |
| 用户 | `POST /api/admin/users/{id}/disable` / `enable` | 禁用或启用 |
| 空间 | `GET /api/admin/buildings` | 获取楼栋 |
| 空间 | `GET /api/admin/spaces` | 空间列表 |
| 空间 | `POST /api/admin/spaces` / `PUT /api/admin/spaces/{id}` | 新增或编辑空间 |
| 空间 | `POST /api/admin/spaces/{id}/open` / `close` | 开放或关闭空间 |
| 座位 | `GET /api/admin/seats?roomId=` | 座位列表 |
| 座位 | `POST /api/admin/seats/batch-generate` | 批量生成座位/工位 |
| 时间节点 | `GET /api/admin/time-nodes` | 时间节点列表 |
| 线下代预约 | `POST /api/admin/reservations/offline` | 管理员代学生预约 |
| 预约记录 | `GET /api/admin/reservations` | 查看预约记录 |
| 审批 | `GET /api/admin/approvals` | 待审批/审批记录 |
| 审批 | `POST /api/admin/approvals/{id}/approve` / `reject` | 通过或驳回预约 |
| 报修 | `GET /api/admin/repairs` | 报修列表 |
| 报修 | `POST /api/admin/repairs/{id}/accept` / `process` / `reject` / `finish` | 处理报修 |
| 公告 | `GET /api/admin/announcements` | 公告列表 |
| 公告 | `POST /api/admin/announcements` / `PUT /api/admin/announcements/{id}` | 新增或编辑公告 |
| 公告 | `POST /api/admin/announcements/{id}/publish` / `disable` / `pin` / `unpin` | 发布、停用、置顶、取消置顶 |
| 规则配置 | `GET /api/admin/configs` | 查看系统规则 |
| 规则配置 | `PUT /api/admin/configs/{key}` | 修改系统规则 |
| 操作日志 | `GET /api/admin/operation-logs` | 查看管理员操作日志 |
| 统计分析 | `GET /api/admin/statistics/overview` | 运营统计概览 |
| 统计分析 | `GET /api/admin/statistics/spaces` / `repairs` / `approvals` / `learning-rank` | 空间、报修、审批和学习排行统计 |
| AI 任务 | `GET /api/admin/ai-tasks` | 查看自动预约任务 |
| 违规 | `GET /api/admin/violations` | 查看违规记录 |

## 核心设计与报告素材

这些点适合后期写课程报告或答辩时展开。这里仅列出证据位置和可讲角度，不替代完整报告。

| 报告角度 | 可以怎么讲 | 代码证据 |
| --- | --- | --- |
| 前后端分离 | 前端通过 Vite 代理 `/api` 到后端，后端提供 REST API，登录后用 JWT 维护用户身份 | `frontend/vite.config.js`, `frontend/src/api.js`, `JwtInterceptor` |
| 角色隔离 | 学生接口以 `/api/student` 开头，管理员接口以 `/api/admin` 开头，拦截器按角色阻止越权访问 | `JwtInterceptor`, `frontend/src/router.js` |
| 预约冲突控制 | 同一用户或同一资源在有效状态下不能出现时间重叠；座位型空间和整间空间使用不同冲突判断 | `ReservationServiceImpl.createReservation`, `hasSeatConflict`, `hasRoomConflict` |
| 空间类型差异 | `STUDY_ROOM`、`PUBLIC_AREA`、`LAB_SEAT` 按座位/工位预约，`SEMINAR_ROOM`、`CLASSROOM` 按整间预约 | `ReservationServiceImpl`, `v2_seed_spaces.sql` |
| 定位签到 | 签到时计算用户位置与空间坐标距离，超出允许半径则拒绝签到 | `ReservationServiceImpl.signIn`, `LocationUtils` |
| 定时维护 | 每 60 秒处理首次签到超时、暂离返座超时、预约到期、封禁恢复和自动预约任务 | `ReservationMaintenanceTask` |
| 规则配置 | 签到时限、暂离时长、封禁阈值、AI/审批/报修开关等从系统配置读取 | `ConfigServiceImpl`, `ReservationServiceImpl`, `ViolationServiceImpl` |
| 规则评分式自动预约 | 对候选座位按偏好空间、插座、靠窗加分，选择得分最高且无冲突的座位创建预约 | `AiReservationServiceImpl.score`, `executeOne` |
| 报修联动资源状态 | 管理员处理报修时可标记座位故障/恢复，或关闭/重新开放空间 | `RepairServiceImpl.applyResourceLinkage` |
| 通知闭环 | 审批、报修、违规、封禁、自动预约结果会写入通知，学生端统一查看 | `NotificationController`, `ReservationServiceImpl`, `RepairServiceImpl`, `ViolationServiceImpl` |

## 数据库与脚本

数据库类型为 SQL Server。脚本位于 `backend/src/main/resources/db/`：

| 脚本 | 作用 |
| --- | --- |
| `schema.sql` | 创建基础表、基础账号、基础自习室、座位和时间节点 |
| `v2_upgrade.sql` | 补充 V2 功能需要的字段和表，例如审批、报修、公告、通知、配置、日志、暂离记录 |
| `v2_seed_spaces.sql` | 补充多类型空间、楼栋、座位/工位数据，并处理整间预约空间不维护座位的问题 |

> [!IMPORTANT]
> `application.yml` 中的数据库连接和 JWT 配置应按本机环境调整。公开提交或部署时，不建议把真实生产账号、密码或密钥写入仓库。

## 快速开始

### 环境要求

- JDK 17
- Maven
- Node.js 和 npm
- SQL Server

### 初始化数据库

1. 在 SQL Server 中创建数据库，例如 `echo_study`。
2. 按本机 SQL Server 账号修改 `backend/src/main/resources/application.yml`。
3. 按顺序执行：

```text
backend/src/main/resources/db/schema.sql
backend/src/main/resources/db/v2_upgrade.sql
backend/src/main/resources/db/v2_seed_spaces.sql
```

### 启动后端

```powershell
cd backend
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 启动前端

```powershell
cd frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

### 演示账号

当前初始化器和登录页使用的演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `test123456` |
| 学生 | `student` | `test123456` |

如果数据库中已有旧数据，实际密码以数据库记录为准。

## 验证记录

当前仓库没有独立测试用例目录。以下命令已在本地验证：

| 日期 | 命令 | 结果 |
| --- | --- | --- |
| 2026-07-01 | 在 `backend/` 执行 `mvn test` | 成功；Maven 显示没有测试源可运行 |
| 2026-07-01 | 在 `frontend/` 执行 `npm run build` | 成功；Vite 输出 chunk 体积提示和第三方注释提示，不影响产物生成 |

可复跑命令：

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm run build
```

## 后期报告建议

写课程报告时，可以按下面结构展开：

1. **需求背景**：校园自习空间预约、签到、暂离、违规、报修和管理统计的痛点。
2. **系统角色与功能**：学生端、管理端分别负责什么，用“功能模块”表作为目录。
3. **系统架构**：Vue 前端、Spring Boot 后端、SQL Server 数据库、JWT 鉴权和定时任务。
4. **数据库设计**：围绕 `users`、`study_room`、`seat`、`reservation`、`violation_record`、`repair_record`、`notification`、`system_config` 展开。
5. **关键业务实现**：预约冲突判断、定位签到、定时维护、规则配置、规则评分式自动预约。
6. **测试与验证**：记录后端构建、前端构建、登录、预约、签到、报修、审批等人工演示步骤。
7. **项目特色与不足**：特色写“规则配置化、预约生命周期闭环、规则评分式自动预约”；不足写“缺少自动化测试、缺少截图/部署材料、配置仍需本地化处理”。

## 已知限制

- 仓库未包含 `LICENSE` 文件，许可证状态未声明。
- 当前没有独立自动化测试用例目录，主要依赖构建验证和人工流程验证。
- 仓库未包含正式截图或在线演示链接；课程展示时建议补充登录、学生预约、管理员审批、统计分析等页面截图。
- `docs/API.md` 和 `docs/RUN.md` 是旧版摘要，若作为提交材料使用，需要按本 README 和当前代码同步更新。
