# EchoStudy

EchoStudy 是一个前后端分离的校园自习空间预约系统，用于管理自习室、座位/工位、预约、签到、暂离、违规、报修、公告和后台运营数据。

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat-square)
![Vue](https://img.shields.io/badge/Vue-3.x-42b883?style=flat-square)
![Vite](https://img.shields.io/badge/Vite-5.x-646CFF?style=flat-square)
![SQL Server](https://img.shields.io/badge/Database-SQL%20Server-CC2927?style=flat-square)

> [!NOTE]
> 这是一个源代码运行型项目。仓库没有提供已发布的软件包、在线演示地址或截图资源，因此 README 只描述代码中已经存在的功能和本地运行方式。

## 功能概览

- **学生端预约**：按日期、空间、时间节点查看座位网格，发起线上预约，查看我的预约。
- **预约生命周期**：支持取消、定位签到、暂离、返座、结束预约，以及超时后的定时维护处理。
- **空间与座位管理**：管理员可维护空间/自习室、座位/工位、开放状态、故障状态和时间节点。
- **审批与线下代预约**：支持需要审批的空间预约，以及管理员为学生创建线下预约。
- **违规与封禁**：记录首次签到超时、暂离未返等违规，并通过定时任务处理预约释放和封禁恢复。
- **AI 自动预约任务**：学生可提交带偏好的自动预约任务，系统按空间、插座、靠窗等条件为可用座位打分并尝试创建预约。
- **报修、公告与消息**：学生提交资源报修、查看消息；管理员处理报修、发布公告、查看通知记录。
- **后台运营**：包含用户管理、规则配置、操作日志、统计分析、预约记录和 AI 任务管理等页面。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.3.5, Spring MVC, MyBatis Plus, JWT, Spring Task, Lombok, Knife4j, Maven |
| 前端 | Vue 3, Vite 5, Vue Router, Element Plus, Axios |
| 数据库 | SQL Server |
| API 文档 | Knife4j / OpenAPI, 默认地址 `http://localhost:8080/doc.html` |

## 快速开始

### 1. 准备环境

- JDK 17
- Maven
- Node.js 和 npm
- SQL Server

### 2. 初始化数据库

1. 在 SQL Server 中创建数据库，例如 `echo_study`。
2. 根据本机数据库账号修改 [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml)。
3. 按顺序执行数据库脚本：

```text
backend/src/main/resources/db/schema.sql
backend/src/main/resources/db/v2_upgrade.sql
backend/src/main/resources/db/v2_seed_spaces.sql
```

其中 `schema.sql` 创建基础表和演示数据，`v2_upgrade.sql` 补齐审批、报修、公告、消息、配置、日志等模块所需表结构，`v2_seed_spaces.sql` 用于补充更多空间示例数据。

### 3. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

后端默认监听：

```text
http://localhost:8080
```

### 4. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端开发服务默认监听：

```text
http://localhost:5173
```

Vite 已配置 `/api` 代理到 `http://localhost:8080`。

### 5. 演示账号

当前代码中的初始化器和前端登录页默认使用以下演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `test123456` |
| 学生 | `student` | `test123456` |

如果数据库中已有旧数据，实际密码以当前数据库记录为准。

## 常用脚本

### 后端

```powershell
cd backend
mvn spring-boot:run
mvn test
```

### 前端

```powershell
cd frontend
npm run dev
npm run build
npm run preview
```

## 项目结构

```text
EchoStudy/
├─ backend/                 # Spring Boot 后端
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/echostudy/
│     │  ├─ controller/     # REST API
│     │  ├─ service/        # 业务服务
│     │  ├─ mapper/         # MyBatis Plus Mapper
│     │  ├─ entity/         # 数据实体
│     │  ├─ dto/            # 请求对象
│     │  ├─ vo/             # 响应对象
│     │  ├─ security/       # JWT 与请求拦截
│     │  └─ task/           # 定时维护任务
│     └─ resources/
│        ├─ application.yml
│        └─ db/             # SQL Server 初始化/升级脚本
├─ frontend/                # Vue 3 + Vite 前端
│  ├─ package.json
│  └─ src/
│     ├─ views/             # 学生端与管理端页面
│     ├─ components/        # 通用布局与组件
│     ├─ config/            # 页面、状态等前端配置
│     └─ styles/            # 主题与组件样式
└─ docs/                    # 运行说明、接口说明与 V2 进度记录
```

## 主要入口

- 前端路由：`frontend/src/router.js`
- 前端 API 封装：`frontend/src/api.js`
- 后端启动类：`backend/src/main/java/com/echostudy/EchoStudyApplication.java`
- 后端配置：`backend/src/main/resources/application.yml`
- 数据库脚本：`backend/src/main/resources/db/`
- 接口摘要：[docs/API.md](docs/API.md)
- 运行说明：[docs/RUN.md](docs/RUN.md)

## 运行与验证

本仓库当前没有独立的自动化测试目录。提交前可使用以下命令做基础验证：

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm run build
```

## 已知限制

- 仓库未包含 `LICENSE` 文件，许可证状态未声明。
- 仓库未包含产品截图或在线演示链接；如需展示界面效果，建议补充前端关键页面截图。
- 数据库连接信息写在 `application.yml` 中，部署或公开使用前应改为环境变量、外部配置或本地覆盖配置。
